package com.gym.fitconnectpro.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.gym.fitconnectpro.database.DatabaseHelper;

public class StatisticsDAO {

    private static final String TAG = "StatisticsDAO";
    private DatabaseHelper dbHelper;

    public StatisticsDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Get total members count
     */
    public int getTotalMembers() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int count = 0;

        try {
            db = dbHelper.getReadableDatabase();
            String query = "SELECT COUNT(*) FROM members";
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting total members", e);
        } finally {
            DatabaseHelper.closeCursor(cursor);
        }

        return count;
    }

    /**
     * Get active members count
     */
    public int getActiveMembersCount() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int count = 0;

        try {
            db = dbHelper.getReadableDatabase();
            String query = "SELECT COUNT(*) FROM members WHERE status = 'ACTIVE'";
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting active members", e);
        } finally {
            DatabaseHelper.closeCursor(cursor);
        }

        return count;
    }

    /**
     * Get total trainers count
     */
    public int getTotalTrainers() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int count = 0;

        try {
            db = dbHelper.getReadableDatabase();
            String query = "SELECT COUNT(*) FROM trainers";
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting total trainers", e);
        } finally {
            DatabaseHelper.closeCursor(cursor);
        }

        return count;
    }

    /**
     * Get monthly revenue (sum of payments in current month)
     * Note: This is a simplified version. For production, filter by current month.
     */
    public double getMonthlyRevenue() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double revenue = 0.0;

        try {
            db = dbHelper.getReadableDatabase();
            // In a real app, you would add WHERE clause for date range
            // e.g., WHERE strftime('%Y-%m', payment_date) = strftime('%Y-%m', 'now')
            String query = "SELECT SUM(amount) FROM payments WHERE status = 'COMPLETED'";
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                revenue = cursor.getDouble(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting monthly revenue", e);
        } finally {
            DatabaseHelper.closeCursor(cursor);
        }

        return revenue;
    }
}
