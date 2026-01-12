package com.gym.fitconnectpro.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.gym.fitconnectpro.database.DatabaseHelper;
import com.gym.fitconnectpro.models.TrainerDailyGoal;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DailyGoalDAO {
    private static final String TAG = "DailyGoalDAO";
    private DatabaseHelper dbHelper;

    public DailyGoalDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public boolean setDailyGoals(TrainerDailyGoal goal) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long result = -1;

        try {
            // Check if goal already exists for this date
            String checkSql = "SELECT id FROM trainer_daily_goals WHERE member_id = ? AND goal_date = ?";
            Cursor cursor = db.rawQuery(checkSql, new String[]{String.valueOf(goal.getMemberId()), goal.getGoalDate()});

            ContentValues values = new ContentValues();
            values.put("trainer_id", goal.getTrainerId());
            values.put("member_id", goal.getMemberId());
            values.put("goal_date", goal.getGoalDate());
            values.put("workout_duration", goal.getWorkoutDuration());
            values.put("calorie_target", goal.getCalorieTarget());
            values.put("water_intake_ml", goal.getWaterIntakeMl());
            values.put("calorie_limit", goal.getCalorieLimit());
            values.put("protein_target", goal.getProteinTarget());
            values.put("carbs_target", goal.getCarbsTarget());
            values.put("fats_target", goal.getFatsTarget());
            values.put("special_instructions", goal.getSpecialInstructions());

            if (cursor.moveToFirst()) {
                // Update existing goal
                int existingGoalId = cursor.getInt(0);
                values.put("created_at", getCurrentDateTime()); // Ensure updated timestamp if needed or ignore
                result = db.update("trainer_daily_goals", values, "id = ?", new String[]{String.valueOf(existingGoalId)});
                Log.d(TAG, "Updated daily goal for member: " + goal.getMemberId());
            } else {
                // Insert new goal
                values.put("created_at", getCurrentDateTime());
                result = db.insert("trainer_daily_goals", null, values);
                Log.d(TAG, "Inserted daily goal for member: " + goal.getMemberId());
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error setting daily goals", e);
        }

        return result != -1;
    }

    public boolean setGoalsForWeek(TrainerDailyGoal baseGoal, int days) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        boolean success = true;

        try {
            LocalDate startDate = LocalDate.parse(baseGoal.getGoalDate());
            
            for (int i = 0; i < days; i++) {
                TrainerDailyGoal dayGoal = new TrainerDailyGoal();
                dayGoal.setTrainerId(baseGoal.getTrainerId());
                dayGoal.setMemberId(baseGoal.getMemberId());
                dayGoal.setGoalDate(startDate.plusDays(i).toString());
                dayGoal.setWorkoutDuration(baseGoal.getWorkoutDuration());
                dayGoal.setCalorieTarget(baseGoal.getCalorieTarget());
                dayGoal.setWaterIntakeMl(baseGoal.getWaterIntakeMl());
                dayGoal.setCalorieLimit(baseGoal.getCalorieLimit());
                dayGoal.setProteinTarget(baseGoal.getProteinTarget());
                dayGoal.setCarbsTarget(baseGoal.getCarbsTarget());
                dayGoal.setFatsTarget(baseGoal.getFatsTarget());
                dayGoal.setSpecialInstructions(baseGoal.getSpecialInstructions());

                if (!setDailyGoalsInsideTransaction(db, dayGoal)) {
                    success = false;
                    break;
                }
            }

            if (success) {
                db.setTransactionSuccessful();
                Log.d(TAG, "Weekly goals set successfully");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting weekly goals", e);
            success = false;
        } finally {
            db.endTransaction();
        }
        return success;
    }

    // Helper method for transaction reuse
    private boolean setDailyGoalsInsideTransaction(SQLiteDatabase db, TrainerDailyGoal goal) {
        long result = -1;
        try {
            String checkSql = "SELECT id FROM trainer_daily_goals WHERE member_id = ? AND goal_date = ?";
            Cursor cursor = db.rawQuery(checkSql, new String[]{String.valueOf(goal.getMemberId()), goal.getGoalDate()});

            ContentValues values = new ContentValues();
            values.put("trainer_id", goal.getTrainerId());
            values.put("member_id", goal.getMemberId());
            values.put("goal_date", goal.getGoalDate());
            values.put("workout_duration", goal.getWorkoutDuration());
            values.put("calorie_target", goal.getCalorieTarget());
            values.put("water_intake_ml", goal.getWaterIntakeMl());
            values.put("calorie_limit", goal.getCalorieLimit());
            values.put("protein_target", goal.getProteinTarget());
            values.put("carbs_target", goal.getCarbsTarget());
            values.put("fats_target", goal.getFatsTarget());
            values.put("special_instructions", goal.getSpecialInstructions());

            if (cursor.moveToFirst()) {
                int existingGoalId = cursor.getInt(0);
                result = db.update("trainer_daily_goals", values, "id = ?", new String[]{String.valueOf(existingGoalId)});
            } else {
                values.put("created_at", getCurrentDateTime());
                result = db.insert("trainer_daily_goals", null, values);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error inside transaction", e);
            return false;
        }
        return result != -1;
    }

    public TrainerDailyGoal getGoalsForMemberDate(int memberId, String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        TrainerDailyGoal goal = null;

        try {
            String sql = "SELECT * FROM trainer_daily_goals WHERE member_id = ? AND goal_date = ?";
            Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(memberId), date});

            if (cursor.moveToFirst()) {
                goal = new TrainerDailyGoal();
                goal.setTrainerGoalId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                goal.setTrainerId(cursor.getInt(cursor.getColumnIndexOrThrow("trainer_id")));
                goal.setMemberId(cursor.getInt(cursor.getColumnIndexOrThrow("member_id")));
                goal.setGoalDate(cursor.getString(cursor.getColumnIndexOrThrow("goal_date")));
                goal.setWorkoutDuration(cursor.getInt(cursor.getColumnIndexOrThrow("workout_duration")));
                goal.setCalorieTarget(cursor.getInt(cursor.getColumnIndexOrThrow("calorie_target")));
                goal.setWaterIntakeMl(cursor.getInt(cursor.getColumnIndexOrThrow("water_intake_ml")));
                goal.setCalorieLimit(cursor.getInt(cursor.getColumnIndexOrThrow("calorie_limit")));
                goal.setProteinTarget(cursor.getInt(cursor.getColumnIndexOrThrow("protein_target")));
                goal.setCarbsTarget(cursor.getInt(cursor.getColumnIndexOrThrow("carbs_target")));
                goal.setFatsTarget(cursor.getInt(cursor.getColumnIndexOrThrow("fats_target")));
                goal.setSpecialInstructions(cursor.getString(cursor.getColumnIndexOrThrow("special_instructions")));
                goal.setCreatedDate(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error fetching daily goals", e);
        }
        return goal;
    }

    private String getCurrentDateTime() {
        return java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
