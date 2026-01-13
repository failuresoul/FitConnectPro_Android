package com.gym.fitconnectpro.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.gym.fitconnectpro.database.DatabaseHelper;
import com.gym.fitconnectpro.models.Food;
import com.gym.fitconnectpro.models.MealPlanFood;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MealLogDAO {
    private static final String TAG = "MealLogDAO";
    private DatabaseHelper dbHelper;

    public MealLogDAO(Context context) {
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
            String query = "SELECT * FROM foods WHERE name LIKE ? ORDER BY name ASC LIMIT 50";
            cursor = db.rawQuery(query, new String[]{"%" + keyword + "%"});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Food food = new Food();
                    food.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                    food.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                    food.setCalories(cursor.getInt(cursor.getColumnIndexOrThrow("calories")));
                    food.setProtein(cursor.getDouble(cursor.getColumnIndexOrThrow("protein")));
                    food.setCarbs(cursor.getDouble(cursor.getColumnIndexOrThrow("carbs")));
                    food.setFats(cursor.getDouble(cursor.getColumnIndexOrThrow("fats")));
                    food.setServingUnit(cursor.getString(cursor.getColumnIndexOrThrow("serving_unit")));
                    foods.add(food);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error searching foods", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return foods;
    }

    /**
     * Get all foods for Combo Box
     */
    public List<Food> getAllFoods() {
        List<Food> foods = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT * FROM foods ORDER BY name ASC";
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Food food = new Food();
                    food.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                    food.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                    food.setCalories(cursor.getInt(cursor.getColumnIndexOrThrow("calories")));
                    food.setProtein(cursor.getDouble(cursor.getColumnIndexOrThrow("protein")));
                    food.setCarbs(cursor.getDouble(cursor.getColumnIndexOrThrow("carbs")));
                    food.setFats(cursor.getDouble(cursor.getColumnIndexOrThrow("fats")));
                    food.setServingUnit(cursor.getString(cursor.getColumnIndexOrThrow("serving_unit")));
                    foods.add(food);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching all foods", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return foods;
    }

    /**
     * Get today's logged meals
     */
    public List<com.gym.fitconnectpro.models.MealLogEntry> getTodayMeals(int memberId, String date) {
        List<com.gym.fitconnectpro.models.MealLogEntry> meals = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT id, meal_type, meal_time, total_calories, notes FROM member_meals WHERE member_id = ? AND meal_date = ? ORDER BY id DESC";
            cursor = db.rawQuery(query, new String[]{String.valueOf(memberId), date});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    com.gym.fitconnectpro.models.MealLogEntry meal = new com.gym.fitconnectpro.models.MealLogEntry();
                    meal.setId(cursor.getInt(0));
                    meal.setMealType(cursor.getString(1));
                    meal.setMealTime(cursor.getString(2));
                    meal.setTotalCalories(cursor.getInt(3));
                    meal.setNotes(cursor.getString(4));
                    meals.add(meal);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching today's meals", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return meals;
    }


    /**
     * Log a meal with items and update daily totals
     */
    public boolean logMeal(int memberId, String date, String mealTime, String mealType, List<MealPlanFood> items, String notes) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        
        try {
            // 1. Calculate totals
            int totalCalories = 0;
            double totalProtein = 0;
            double totalCarbs = 0;
            double totalFats = 0;
            
            for (MealPlanFood item : items) {
                Food f = item.getFood();
                double qty = item.getQuantity();
                
                totalCalories += (int)(f.getCalories() * qty);
                totalProtein += f.getProtein() * qty;
                totalCarbs += f.getCarbs() * qty;
                totalFats += f.getFats() * qty;
            }
            
            // 2. Insert into member_meals
            ContentValues mealValues = new ContentValues();
            mealValues.put("member_id", memberId);
            mealValues.put("meal_type", mealType);
            mealValues.put("meal_date", date);
            mealValues.put("meal_time", mealTime);
            mealValues.put("notes", notes);
            mealValues.put("total_calories", totalCalories);
            mealValues.put("total_protein", totalProtein);
            mealValues.put("total_carbs", totalCarbs);
            mealValues.put("total_fats", totalFats);
            
            long mealId = db.insert("member_meals", null, mealValues);
            
            if (mealId == -1) {
                return false;
            }
            
            // 3. Insert items
            for (MealPlanFood item : items) {
                ContentValues itemValues = new ContentValues();
                itemValues.put("meal_id", mealId);
                itemValues.put("food_id", item.getFood().getId());
                itemValues.put("quantity", item.getQuantity());
                db.insert("member_meal_items", null, itemValues);
            }
            
            // 4. Update member_daily_logs
            updateDailyLog(db, memberId, date, totalCalories);
            
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error logging meal", e);
            return false;
        } finally {
            db.endTransaction();
        }
    }
    
    // Helper to update or create daily log
    private void updateDailyLog(SQLiteDatabase db, int memberId, String date, int addedCalories) {
        // Check if log exists
        Cursor cursor = db.rawQuery("SELECT id, calories_consumed FROM member_daily_logs WHERE member_id = ? AND log_date = ?", 
                new String[]{String.valueOf(memberId), date});
        
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            int currentCals = cursor.getInt(1);
            int newCals = currentCals + addedCalories;
            
            ContentValues values = new ContentValues();
            values.put("calories_consumed", newCals);
            db.update("member_daily_logs", values, "id = ?", new String[]{String.valueOf(id)});
        } else {
            // Create new log
            ContentValues values = new ContentValues();
            values.put("member_id", memberId);
            values.put("log_date", date);
            values.put("calories_consumed", addedCalories);
            db.insert("member_daily_logs", null, values);
        }
        
        if (cursor != null) cursor.close();
    }
}
