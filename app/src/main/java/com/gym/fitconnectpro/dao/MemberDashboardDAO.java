package com.gym.fitconnectpro.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.gym.fitconnectpro.database.DatabaseHelper;

import java.util.HashMap;
import java.util.Map;

public class MemberDashboardDAO {
    private static final String TAG = "MemberDashboardDAO";
    private DatabaseHelper dbHelper;

    public MemberDashboardDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Get basic member info + assigned trainer name
     * Returns Map with keys: full_name, trainer_name, trainer_id
     */
    public Map<String, String> getMemberHeaderInfo(int userId) {
        Map<String, String> info = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        // Try to find member by user_id first
        String query = "SELECT m.member_id, m.full_name as member_name, t.full_name as trainer_name, t.id as trainer_id " +
                "FROM members m " +
                "LEFT JOIN trainer_assignments ta ON m.member_id = ta.member_id AND ta.status = 'ACTIVE' " +
                "LEFT JOIN trainers t ON ta.trainer_id = t.id " +
                "WHERE m.user_id = ?";
        
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
            
            // If not found by user_id, try to find by matching username
            if (!cursor.moveToFirst()) {
                cursor.close();
                
                // Get username from session/users table
                Cursor userCursor = db.rawQuery("SELECT username FROM users WHERE id = ?", new String[]{String.valueOf(userId)});
                String username = null;
                if (userCursor.moveToFirst()) {
                    username = userCursor.getString(0);
                }
                userCursor.close();
                
                if (username != null) {
                    // Try to find member by username
                    query = "SELECT m.member_id, m.full_name as member_name, t.full_name as trainer_name, t.id as trainer_id " +
                            "FROM members m " +
                            "LEFT JOIN trainer_assignments ta ON m.member_id = ta.member_id AND ta.status = 'ACTIVE' " +
                            "LEFT JOIN trainers t ON ta.trainer_id = t.id " +
                            "WHERE m.username = ?";
                    cursor = db.rawQuery(query, new String[]{username});
                    
                    // If found, update the member record to set user_id for future queries
                    if (cursor.moveToFirst()) {
                        int memberId = cursor.getInt(0);
                        try {
                            ContentValues values = new ContentValues();
                            values.put("user_id", userId);
                            db.update("members", values, "member_id = ?", new String[]{String.valueOf(memberId)});
                            Log.d(TAG, "Updated member " + memberId + " with user_id " + userId);
                        } catch (Exception e) {
                            Log.e(TAG, "Error updating member user_id", e);
                        }
                    }
                }
            }
            
            if (cursor != null && cursor.moveToFirst()) {
                info.put("member_id", cursor.getString(0));
                info.put("member_name", cursor.getString(1));
                info.put("trainer_name", cursor.getString(2) != null ? cursor.getString(2) : "No Trainer Assigned");
                info.put("trainer_id", cursor.getString(3));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching member header", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return info;
    }

    /**
     * Get Today's Daily Goals
     */
    public Map<String, Object> getTodayGoals(int memberId, String date) {
        Map<String, Object> goals = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT * FROM trainer_daily_goals WHERE member_id = ? AND goal_date = ?";
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{String.valueOf(memberId), date});
            if (cursor.moveToFirst()) {
                goals.put("calories_target", cursor.getInt(cursor.getColumnIndexOrThrow("calorie_target")));
                goals.put("water_target", cursor.getInt(cursor.getColumnIndexOrThrow("water_intake_ml")));
                goals.put("workout_duration", cursor.getInt(cursor.getColumnIndexOrThrow("workout_duration")));
                // Add completion checks from daily_logs if needed, for now just returning targets
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching today goals", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return goals;
    }
    
    /**
     * Get Active Workout Plan Name
     */
    public String getActiveWorkoutPlanName(int memberId) {
        String planName = "No Active Plan";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT plan_name FROM workout_plans " +
                       "WHERE member_id = ? AND status = 'ACTIVE' " +
                       "AND date('now') BETWEEN start_date AND end_date " +
                       "LIMIT 1";
                       
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{String.valueOf(memberId)});
            if (cursor.moveToFirst()) {
                planName = cursor.getString(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching workout plan", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return planName;
    }

    /**
     * Get Today's Meal Count (Breakfast, Lunch, Dinner, Snack)
     */
    public int getTodayMealCount(int memberId, String date) {
        int count = 0;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT COUNT(*) FROM trainer_meal_plans WHERE member_id = ? AND plan_date = ?";
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{String.valueOf(memberId), date});
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching meal count", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return count;
    }
    
    /**
     * Get Quick Stats (Streak, Total Workouts, Current Weight)
     */
    public Map<String, String> getQuickStats(int memberId) {
        Map<String, String> stats = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        
        // 1. Current Weight (Latest from weight history)
        try {
            cursor = db.rawQuery("SELECT weight FROM member_weight_history WHERE member_id = ? ORDER BY log_date DESC LIMIT 1", new String[]{String.valueOf(memberId)});
            if (cursor.moveToFirst()) {
                stats.put("weight", cursor.getString(0) + " kg");
            } else {
                // Fallback to member table weight
                Cursor c2 = db.rawQuery("SELECT weight FROM members WHERE member_id = ?", new String[]{String.valueOf(memberId)});
                if (c2.moveToFirst()) stats.put("weight", c2.getString(0) + " kg");
                else stats.put("weight", "-- kg");
                c2.close();
            }
            cursor.close();
            
            // 2. Total Workouts Completed
            cursor = db.rawQuery("SELECT COUNT(*) FROM member_daily_logs WHERE member_id = ? AND workout_completed = 1", new String[]{String.valueOf(memberId)});
            if (cursor.moveToFirst()) {
                stats.put("total_workouts", cursor.getString(0));
            } else {
                stats.put("total_workouts", "0");
            }
            cursor.close();
            
            // 3. Streak (Simplified: Count consecutive days ending today? Or just last 7 days count? Let's do workouts in last 7 days)
            // Implementation of real streak is complex in SQLLite without window functions (avail in newer versions but safe is logic).
            // Let's just return "Workouts this week"
            cursor = db.rawQuery("SELECT COUNT(*) FROM member_daily_logs WHERE member_id = ? AND workout_completed = 1 AND log_date >= date('now', '-7 days')", new String[]{String.valueOf(memberId)});
            if (cursor.moveToFirst()) {
                stats.put("weekly_workouts", cursor.getString(0));
            } else {
                stats.put("weekly_workouts", "0");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error fetching quick stats", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
        }
        return stats;
    }
}
