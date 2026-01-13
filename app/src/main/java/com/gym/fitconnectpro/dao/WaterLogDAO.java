package com.gym.fitconnectpro.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.gym.fitconnectpro.database.DatabaseHelper;
import com.gym.fitconnectpro.models.WaterLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WaterLogDAO {
    private static final String TAG = "WaterLogDAO";
    private DatabaseHelper dbHelper;

    public WaterLogDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Log water intake
     */
    public boolean logWater(int memberId, int amountMl, String time) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("member_id", memberId);
            values.put("amount_ml", amountMl);
            values.put("log_time", time);

            long id = db.insert("water_logs", null, values);
            return id != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error logging water", e);
            return false;
        }
    }

    /**
     * Get today's total water intake
     */
    public int getTodayWaterTotal(int memberId, String date) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int total = 0;

        try {
            db = dbHelper.getReadableDatabase();
            String query = "SELECT SUM(amount_ml) as total FROM water_logs " +
                          "WHERE member_id = ? AND DATE(log_time) = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(memberId), date});

            if (cursor.moveToFirst()) {
                total = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting today's water total", e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return total;
    }

    /**
     * Get today's water logs
     */
    public List<WaterLog> getTodayLogs(int memberId, String date) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<WaterLog> logs = new ArrayList<>();

        try {
            db = dbHelper.getReadableDatabase();
            String query = "SELECT * FROM water_logs " +
                          "WHERE member_id = ? AND DATE(log_time) = ? " +
                          "ORDER BY log_time DESC";
            cursor = db.rawQuery(query, new String[]{String.valueOf(memberId), date});

            while (cursor.moveToNext()) {
                WaterLog log = new WaterLog();
                log.setLogId(cursor.getInt(cursor.getColumnIndex("id")));
                log.setMemberId(cursor.getInt(cursor.getColumnIndex("member_id")));
                log.setAmountMl(cursor.getInt(cursor.getColumnIndex("amount_ml")));
                log.setLogTime(cursor.getString(cursor.getColumnIndex("log_time")));
                log.setCreatedAt(cursor.getString(cursor.getColumnIndex("created_at")));
                logs.add(log);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting today's logs", e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return logs;
    }

    /**
     * Delete water log
     */
    public boolean deleteWaterLog(int logId) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            int rows = db.delete("water_logs", "id = ?", new String[]{String.valueOf(logId)});
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting water log", e);
            return false;
        }
    }

    /**
     * Get last 7 days water intake
     */
    public Map<String, Integer> getLast7DaysWater(int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        Map<String, Integer> waterData = new HashMap<>();

        try {
            db = dbHelper.getReadableDatabase();
            String query = "SELECT DATE(log_time) as log_date, SUM(amount_ml) as total " +
                          "FROM water_logs " +
                          "WHERE member_id = ? AND DATE(log_time) >= DATE('now', '-7 days') " +
                          "GROUP BY DATE(log_time) " +
                          "ORDER BY log_date";
            cursor = db.rawQuery(query, new String[]{String.valueOf(memberId)});

            while (cursor.moveToNext()) {
                String date = cursor.getString(0);
                int total = cursor.getInt(1);
                waterData.put(date, total);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting 7 days water data", e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return waterData;
    }
}
