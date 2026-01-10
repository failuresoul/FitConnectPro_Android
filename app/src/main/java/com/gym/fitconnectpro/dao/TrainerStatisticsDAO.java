package com.gym.fitconnectpro.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.gym.fitconnectpro.database.DatabaseHelper;
import com.gym.fitconnectpro.database.entities.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TrainerStatisticsDAO {
    
    private static final String TAG = "TrainerStatsDAO";
    private DatabaseHelper dbHelper;

    public TrainerStatisticsDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public int getMyClientsCount(int trainerId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int count = 0;
        Cursor cursor = null;
        try {
            String query = "SELECT COUNT(*) FROM trainer_assignments WHERE trainer_id = ? AND status = 'ACTIVE'";
            cursor = db.rawQuery(query, new String[]{String.valueOf(trainerId)});
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error counting clients", e);
        } finally {
            DatabaseHelper.closeCursor(cursor);
        }
        return count;
    }

    public int getTodayCompletedWorkouts(int trainerId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int count = 0;
        Cursor cursor = null;
        try {
            // Assuming we check 'workout_plans' for today's completed items? 
            // Or is there a specific 'workouts' table? 
            // The requirement says "Today's Client Workouts Completed" from "workout_plans where status='COMPLETED' and date=today".
            // Since workout_plans has start_date and end_date, maybe we count plans ending today? 
            // Or maybe plans *checked* today?
            // Let's assume based on the prompt: Status='COMPLETED' and end_date = today (or updated_at = today). 
            // Let's use `updated_at` (check `DatabaseHelper` schema... `updated_at` exists).
            // But usually statistics refer to scheduled sessions. 
            // If we look at the request: "Count from workout_plans where status='COMPLETED' and date=today"
            // I will use `updated_at` date comparison for "today".
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String today = dateFormat.format(new Date());

            String query = "SELECT COUNT(*) FROM workout_plans WHERE trainer_id = ? AND status = 'COMPLETED' AND date(updated_at) = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(trainerId), today});
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error counting completed workouts", e);
        } finally {
            DatabaseHelper.closeCursor(cursor);
        }
        return count;
    }

    public int getPendingWorkoutPlans(int trainerId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int count = 0;
        Cursor cursor = null;
        try {
            String query = "SELECT COUNT(*) FROM workout_plans WHERE trainer_id = ? AND status = 'PENDING'";
            cursor = db.rawQuery(query, new String[]{String.valueOf(trainerId)});
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error counting pending plans", e);
        } finally {
            DatabaseHelper.closeCursor(cursor);
        }
        return count;
    }

    public List<Message> getRecentMessages(int trainerId, int limit) {
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            // Join with users to get sender name
            String query = "SELECT m.*, u.username as sender_name FROM messages m " +
                           "JOIN users u ON m.sender_id = u.id " +
                           "WHERE m.receiver_id = ? " +
                           "ORDER BY m.timestamp DESC LIMIT ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(trainerId), String.valueOf(limit)});
            
            if (cursor.moveToFirst()) {
                do {
                    Message msg = new Message();
                    msg.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                    msg.setSenderId(cursor.getInt(cursor.getColumnIndexOrThrow("sender_id")));
                    msg.setReceiverId(cursor.getInt(cursor.getColumnIndexOrThrow("receiver_id")));
                    msg.setContent(cursor.getString(cursor.getColumnIndexOrThrow("content")));
                    msg.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow("timestamp")));
                    msg.setRead(cursor.getInt(cursor.getColumnIndexOrThrow("is_read")) == 1);
                    msg.setSenderName(cursor.getString(cursor.getColumnIndexOrThrow("sender_name")));
                    
                    messages.add(msg);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching recent messages", e);
        } finally {
            DatabaseHelper.closeCursor(cursor);
        }
        return messages;
    }
}
