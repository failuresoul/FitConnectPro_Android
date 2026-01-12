package com.gym.fitconnectpro.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.gym.fitconnectpro.database.DatabaseHelper;
import com.gym.fitconnectpro.models.Food;
import com.gym.fitconnectpro.models.MealPlan;
import com.gym.fitconnectpro.models.MealPlanFood;

import java.util.ArrayList;
import java.util.List;

public class MealPlanDAO {
    private static final String TAG = "MealPlanDAO";
    private DatabaseHelper dbHelper;

    public MealPlanDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Search foods by keyword
     */
    public List<Food> searchFoods(String keyword) {
        List<Food> foods = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Simple LIKE query
            String query = "SELECT * FROM foods WHERE name LIKE ? ORDER BY name ASC LIMIT 50";
            cursor = db.rawQuery(query, new String[]{"%" + keyword + "%"});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    foods.add(cursorToFood(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error searching foods", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return foods;
    }

    public List<Food> getAllFoods() {
        return searchFoods("");
    }

    /**
     * Create a list of meal plans (typically 4 for a day: B, L, D, S)
     */
    public boolean createMealPlans(List<MealPlan> plans) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            for (MealPlan plan : plans) {
                // Delete existing plan for this member/date/type to avoid duplicates (replace logic)
                db.delete("trainer_meal_plans", "member_id = ? AND plan_date = ? AND meal_type = ?",
                        new String[]{String.valueOf(plan.getMemberId()), plan.getPlanDate(), plan.getMealType()});

                ContentValues values = new ContentValues();
                values.put("trainer_id", plan.getTrainerId());
                values.put("member_id", plan.getMemberId());
                values.put("plan_date", plan.getPlanDate());
                values.put("meal_type", plan.getMealType());
                values.put("instructions", plan.getInstructions());

                long planId = db.insert("trainer_meal_plans", null, values);

                if (planId != -1) {
                    // Insert foods
                    for (MealPlanFood mpFood : plan.getFoods()) {
                        ContentValues foodValues = new ContentValues();
                        foodValues.put("meal_plan_id", planId);
                        foodValues.put("food_id", mpFood.getFoodId());
                        foodValues.put("quantity", mpFood.getQuantity());
                        db.insert("meal_plan_foods", null, foodValues);
                    }
                }
            }
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error creating meal plans", e);
            return false;
        } finally {
            db.endTransaction();
        }
    }

    private Food cursorToFood(Cursor cursor) {
        Food food = new Food();
        food.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        food.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        food.setCalories(cursor.getInt(cursor.getColumnIndexOrThrow("calories")));
        food.setProtein(cursor.getDouble(cursor.getColumnIndexOrThrow("protein")));
        food.setCarbs(cursor.getDouble(cursor.getColumnIndexOrThrow("carbs")));
        food.setFats(cursor.getDouble(cursor.getColumnIndexOrThrow("fats")));
        food.setServingUnit(cursor.getString(cursor.getColumnIndexOrThrow("serving_unit")));
        return food;
    }

    public List<MealPlan> getMealPlans(int memberId) {
        List<MealPlan> plans = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Get recent plans (e.g. from today onwards or just all sorted by date desc)
        // Let's get generic all for now, maybe limit 20
        String query = "SELECT * FROM trainer_meal_plans WHERE member_id = ? ORDER BY plan_date DESC, created_at DESC";
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(query, new String[]{String.valueOf(memberId)});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    MealPlan plan = new MealPlan();
                    plan.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                    plan.setTrainerId(cursor.getInt(cursor.getColumnIndexOrThrow("trainer_id")));
                    plan.setMemberId(cursor.getInt(cursor.getColumnIndexOrThrow("member_id")));
                    plan.setPlanDate(cursor.getString(cursor.getColumnIndexOrThrow("plan_date")));
                    plan.setMealType(cursor.getString(cursor.getColumnIndexOrThrow("meal_type")));
                    plan.setInstructions(cursor.getString(cursor.getColumnIndexOrThrow("instructions")));
                    plan.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));

                    // Fetch foods for this plan
                    plan.setFoods(getFoodsForPlan(plan.getId()));

                    plans.add(plan);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching meal plans", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return plans;
    }

    private List<MealPlanFood> getFoodsForPlan(int planId) {
        List<MealPlanFood> foods = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Join with foods table to get names
        String query = "SELECT mpf.*, f.name, f.calories, f.protein, f.carbs, f.fats, f.serving_unit " +
                "FROM meal_plan_foods mpf " +
                "JOIN foods f ON mpf.food_id = f.id " +
                "WHERE mpf.meal_plan_id = ?";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{String.valueOf(planId)});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    MealPlanFood mpf = new MealPlanFood();
                    mpf.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                    mpf.setMealPlanId(cursor.getInt(cursor.getColumnIndexOrThrow("meal_plan_id")));
                    mpf.setFoodId(cursor.getInt(cursor.getColumnIndexOrThrow("food_id")));
                    mpf.setQuantity(cursor.getDouble(cursor.getColumnIndexOrThrow("quantity")));

                    Food f = new Food();
                    f.setId(mpf.getFoodId());
                    f.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                    f.setCalories(cursor.getInt(cursor.getColumnIndexOrThrow("calories")));
                    f.setProtein(cursor.getDouble(cursor.getColumnIndexOrThrow("protein")));
                    f.setCarbs(cursor.getDouble(cursor.getColumnIndexOrThrow("carbs")));
                    f.setFats(cursor.getDouble(cursor.getColumnIndexOrThrow("fats")));
                    f.setServingUnit(cursor.getString(cursor.getColumnIndexOrThrow("serving_unit")));

                    mpf.setFood(f);
                    foods.add(mpf);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching plan foods", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return foods;
    }
}
