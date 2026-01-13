package com.gym.fitconnectpro.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.gym.fitconnectpro.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.gym.fitconnectpro.models.WeightLog;
import com.gym.fitconnectpro.models.ProgressReport;
import android.content.ContentValues;

public class ProgressDAO {

    private static final String TAG = "ProgressDAO";
    private DatabaseHelper dbHelper;

    public ProgressDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    // --- WEIGHT PROGRESS (Tab 1) ---

    // Return map of Date String -> Weight Double
    public Map<String, Double> getWeightProgressData(int memberId) {
        Map<String, Double> data = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            // Assuming table 'member_weight_history' with columns 'date_recorded' and 'weight'
            // Using raw query names for safety based on typical schema
            String query = "SELECT date_recorded, weight FROM member_weight_history WHERE member_id = ? ORDER BY date_recorded ASC";
            cursor = db.rawQuery(query, new String[]{String.valueOf(memberId)});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String date = cursor.getString(0);
                    double weight = cursor.getDouble(1);
                    data.put(date, weight);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching weight data", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return data; // Note: HashMap is unordered, but we can sort in UI or use LinkedHashMap if order is critical in map. 
        // Better to return List of Entry objects for charting really.
    }
    
    // Better method for Charting: List of custom objects or simple pairs
    public List<WeightRecord> getWeightRecords(int memberId) {
        List<WeightRecord> records = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            String query = "SELECT date_recorded, weight FROM member_weight_history WHERE member_id = ? ORDER BY date_recorded ASC";
            cursor = db.rawQuery(query, new String[]{String.valueOf(memberId)});
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    records.add(new WeightRecord(cursor.getString(0), cursor.getDouble(1)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching weight records", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return records;
    }
    
    public static class WeightRecord {
        public String date;
        public double weight;
        public WeightRecord(String date, double weight) { this.date = date; this.weight = weight; }
    }


    // --- WORKOUT STATS (Tab 2) ---

    public Map<String, Object> getWorkoutStatistics(int memberId) {
        Map<String, Object> stats = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        
        try {
            // Total Workouts
            cursor = db.rawQuery("SELECT COUNT(*) FROM workout_logs WHERE member_id = ?", new String[]{String.valueOf(memberId)});
            if (cursor.moveToFirst()) stats.put("total_workouts", cursor.getInt(0));
            cursor.close();

            // Total Calories
            cursor = db.rawQuery("SELECT SUM(calories_burned) FROM workout_logs WHERE member_id = ?", new String[]{String.valueOf(memberId)});
            if (cursor.moveToFirst()) stats.put("total_calories", cursor.getInt(0));
            cursor.close();

            // Total Duration (Active Time)
            cursor = db.rawQuery("SELECT SUM(duration_minutes) FROM workout_logs WHERE member_id = ?", new String[]{String.valueOf(memberId)});
            if (cursor.moveToFirst()) stats.put("total_duration", cursor.getInt(0));
            cursor.close();
            
        } catch (Exception e) {
            Log.e(TAG, "Error fetching workout stats", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
        }
        return stats;
    }
    
    // For Pie Chart: Workout Type Distribution
    // Assuming workout_logs has a 'workout_type' or we join with 'workouts' table? 
    // Checking schema: likely workout_logs -> workout_id -> name/type
    // For now, simpler approximation or empty if column mismatch.


    // --- NUTRITION STATS (Tab 3) ---

    public Map<String, Double> getNutritionStats(int memberId) {
        Map<String, Double> stats = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        
        try {
            // Average Daily Calories (from member_daily_logs)
            String query = "SELECT AVG(calories_consumed) FROM member_daily_logs WHERE member_id = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(memberId)});
            if (cursor.moveToFirst()) stats.put("avg_calories", cursor.getDouble(0));
            cursor.close();
            
            // To get Macro distribution, we'd need more granular data. 
            // To get Macro distribution, we aggregate from member_meals (actual logs)
            // Assuming member_meals has total_protein, total_carbs, total_fats columns
            String macroQuery = "SELECT AVG(total_protein), AVG(total_carbs), AVG(total_fats) FROM member_meals WHERE member_id = ?";
            Cursor macroCursor = db.rawQuery(macroQuery, new String[]{String.valueOf(memberId)});
            if (macroCursor.moveToFirst()) {
                stats.put("avg_protein", macroCursor.getDouble(0));
                stats.put("avg_carbs", macroCursor.getDouble(1));
                stats.put("avg_fat", macroCursor.getDouble(2));
            }
            macroCursor.close();
            
        } catch (Exception e) {
             Log.e(TAG, "Error fetching nutrition stats", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
        }
        return stats;
    }

    // --- TRAINER CLIENT PROGRESS ---
    
    public Map<String, Object> getClientProgress(int memberId, String startDate, String endDate) {
        Map<String, Object> stats = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            // 1. Weight Change
            // Get start weight (first record >= startDate)
            String startWeightQuery = "SELECT weight FROM member_weight_history WHERE member_id = ? AND date_recorded >= ? ORDER BY date_recorded ASC LIMIT 1";
            cursor = db.rawQuery(startWeightQuery, new String[]{String.valueOf(memberId), startDate});
            double startWeight = 0;
            if (cursor.moveToFirst()) startWeight = cursor.getDouble(0);
            cursor.close();

            // Get end weight (last record <= endDate)
            String endWeightQuery = "SELECT weight FROM member_weight_history WHERE member_id = ? AND date_recorded <= ? ORDER BY date_recorded DESC LIMIT 1";
            cursor = db.rawQuery(endWeightQuery, new String[]{String.valueOf(memberId), endDate});
            double endWeight = 0;
            if (cursor.moveToFirst()) endWeight = cursor.getDouble(0);
            cursor.close();
            
            stats.put("start_weight", startWeight);
            stats.put("current_weight", endWeight);
            stats.put("weight_change", endWeight - startWeight);

            // 2. Workout Stats
            String workoutQuery = "SELECT COUNT(*), SUM(duration_minutes), SUM(calories_burned) FROM workout_logs WHERE member_id = ? AND date BETWEEN ? AND ?";
            cursor = db.rawQuery(workoutQuery, new String[]{String.valueOf(memberId), startDate, endDate});
            if (cursor.moveToFirst()) {
                stats.put("workouts_completed", cursor.getInt(0));
                stats.put("total_minutes", cursor.getInt(1));
                stats.put("calories_burned", cursor.getInt(2));
            }
            cursor.close();
            
            // 3. Attendance (Days present)
             String attendanceQuery = "SELECT COUNT(DISTINCT check_in) FROM attendance WHERE member_id = ? AND check_in BETWEEN ? AND ?";
             // Note: Check-in format might be datetime, allowing simple string compare if ISO8601
             cursor = db.rawQuery(attendanceQuery, new String[]{String.valueOf(memberId), startDate, endDate});
             if (cursor.moveToFirst()) {
                 stats.put("days_present", cursor.getInt(0));
             }

        } catch (Exception e) {
            Log.e(TAG, "Error fetching client progress", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
        }
        
        return stats;
    }

    public List<WeightLog> getWeightHistory(int memberId, String startDate, String endDate) {
        List<WeightLog> logs = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            String query = "SELECT weight, date_recorded FROM member_weight_history WHERE member_id = ? AND date_recorded BETWEEN ? AND ? ORDER BY date_recorded ASC";
            cursor = db.rawQuery(query, new String[]{String.valueOf(memberId), startDate, endDate});
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    WeightLog log = new WeightLog();
                    log.setMemberId(memberId);
                    log.setWeight(cursor.getDouble(0));
                    log.setLogDate(cursor.getString(1));
                    logs.add(log);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching weight history", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return logs;
    }

    public boolean saveWeeklyReport(ProgressReport report) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put("trainer_id", report.getTrainerId());
        values.put("member_id", report.getMemberId());
        values.put("report_start_date", report.getReportStartDate());
        values.put("report_end_date", report.getReportEndDate());
        values.put("generated_date", report.getGeneratedDate());
        values.put("workout_completion_rate", report.getWorkoutCompletionRate());
        values.put("meals_logged_count", report.getMealsLoggedCount());
        values.put("water_compliance_rate", report.getWaterComplianceRate());
        values.put("weight_change", report.getWeightChange());
        values.put("trainer_feedback", report.getTrainerFeedback());
        values.put("status", report.getStatus());
        
        long id = db.insert("member_progress_reports", null, values);
        return id != -1;
    }
}
