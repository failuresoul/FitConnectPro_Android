package com.gym.fitconnectpro.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.gym.fitconnectpro.database.DatabaseHelper;
import com.gym.fitconnectpro.models.DailyLog;
import com.gym.fitconnectpro.models.ProgressReport;
import com.gym.fitconnectpro.models.WeightLog;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProgressDAO {
    private static final String TAG = "ProgressDAO";
    private DatabaseHelper dbHelper;

    public ProgressDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Get aggregated client progress statistics for a date range
     */
    public Map<String, Object> getClientProgress(int memberId, String startDate, String endDate) {
        Map<String, Object> stats = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            // 1. Calculate Workout Completion Rate
            String workoutQuery = "SELECT AVG(workout_completed) * 100 as completion_rate, " +
                    "COUNT(*) as total_days " +
                    "FROM member_daily_logs " +
                    "WHERE member_id = ? AND log_date BETWEEN ? AND ?";
            
            cursor = db.rawQuery(workoutQuery, new String[]{String.valueOf(memberId), startDate, endDate});
            if (cursor.moveToFirst()) {
                stats.put("workoutCompletionRate", cursor.getDouble(0));
            } else {
                stats.put("workoutCompletionRate", 0.0);
            }
            cursor.close();

            // 2. Count Meals Logged (approximation based on calories entries vs expected)
            // Just counting total logs where calories > 0
            String mealsQuery = "SELECT COUNT(*) FROM member_daily_logs " +
                    "WHERE member_id = ? AND log_date BETWEEN ? AND ? AND calories_consumed > 0";
            
            cursor = db.rawQuery(mealsQuery, new String[]{String.valueOf(memberId), startDate, endDate});
            if (cursor.moveToFirst()) {
                stats.put("mealsLoggedCount", cursor.getInt(0));
            } else {
                stats.put("mealsLoggedCount", 0);
            }
            cursor.close();

            // 3. Water Intake Compliance (e.g. > 2000ml is goal)
            // Hardcoded goal of 2000ml for now
            String waterQuery = "SELECT COUNT(*) FROM member_daily_logs " +
                    "WHERE member_id = ? AND log_date BETWEEN ? AND ? AND water_intake_ml >= 2000";
            
            cursor = db.rawQuery(waterQuery, new String[]{String.valueOf(memberId), startDate, endDate});
            int waterDays = 0;
            if (cursor.moveToFirst()) {
                waterDays = cursor.getInt(0);
            }
            stats.put("waterComplianceDays", waterDays);
            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, "Error fetching stats", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        
        return stats;
    }

    public List<WeightLog> getWeightHistory(int memberId, String startDate, String endDate) {
        List<WeightLog> logs = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT * FROM member_weight_history " +
                "WHERE member_id = ? AND log_date BETWEEN ? AND ? " +
                "ORDER BY log_date ASC";
        
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{String.valueOf(memberId), startDate, endDate});
            if (cursor.moveToFirst()) {
                do {
                    WeightLog log = new WeightLog();
                    log.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                    log.setMemberId(memberId);
                    log.setWeight(cursor.getDouble(cursor.getColumnIndexOrThrow("weight")));
                    log.setLogDate(cursor.getString(cursor.getColumnIndexOrThrow("log_date")));
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
        values.put("workout_completion_rate", report.getWorkoutCompletionRate());
        values.put("meals_logged_count", report.getMealsLoggedCount());
        values.put("water_compliance_rate", report.getWaterComplianceRate());
        values.put("weight_change", report.getWeightChange());
        values.put("trainer_feedback", report.getTrainerFeedback());
        values.put("status", "SENT");

        long id = db.insert("member_progress_reports", null, values);
        return id != -1;
    }
}
