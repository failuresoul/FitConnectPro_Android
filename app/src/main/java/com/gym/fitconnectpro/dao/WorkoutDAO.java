package com.gym.fitconnectpro.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.gym.fitconnectpro.database.DatabaseHelper;
import com.gym.fitconnectpro.database.entities.Workout;
import com.gym.fitconnectpro.database.entities.WorkoutLog;

import java.util.List;

public class WorkoutDAO {
    private static final String TAG = "WorkoutDAO";
    private DatabaseHelper dbHelper;

    public WorkoutDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public boolean createWorkout(Workout workout) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            if (workout.getPlanId() > 0) {
                values.put("plan_id", workout.getPlanId());
            }
            values.put("member_id", workout.getMemberId());
            if (workout.getTrainerId() > 0) {
                values.put("trainer_id", workout.getTrainerId());
            }
            values.put("session_date", workout.getSessionDate()); // Should be YYYY-MM-DD
            values.put("duration_minutes", workout.getDurationMinutes());
            values.put("calories_burned", workout.getCaloriesBurned());
            values.put("notes", workout.getNotes());

            long id = db.insert("workout_sessions", null, values);
            if (id != -1) {
                workout.setId((int) id);
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error creating workout session", e);
            return false;
        }
    }

    public boolean createWorkoutLogs(List<WorkoutLog> logs) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            
            for (WorkoutLog log : logs) {
                 ContentValues values = new ContentValues();
                 values.put("session_id", log.getSessionId());
                 values.put("exercise_id", log.getExerciseId());
                 values.put("set_number", log.getSetNumber());
                 values.put("reps", log.getReps());
                 values.put("weight", log.getWeight());
                 values.put("notes", log.getNotes());
                 
                 long res = db.insert("workout_logs", null, values);
                 if (res == -1) {
                     return false; // Rollback
                 }
            }
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
             Log.e(TAG, "Error creating workout logs", e);
             return false;
        } finally {
            if (db != null) db.endTransaction();
        }
    }

    /**
     * Get all workout sessions for a member on a specific date
     */
    public List<com.gym.fitconnectpro.models.WorkoutSession> getTodayWorkoutSessions(int memberId, String date) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<com.gym.fitconnectpro.models.WorkoutSession> sessions = new java.util.ArrayList<>();

        try {
            db = dbHelper.getReadableDatabase();
            String query = "SELECT * FROM workout_sessions WHERE member_id = ? AND session_date = ? ORDER BY id DESC";
            cursor = db.rawQuery(query, new String[]{String.valueOf(memberId), date});

            while (cursor.moveToNext()) {
                com.gym.fitconnectpro.models.WorkoutSession session = new com.gym.fitconnectpro.models.WorkoutSession();
                session.setSessionId(cursor.getInt(cursor.getColumnIndex("id")));
                session.setMemberId(cursor.getInt(cursor.getColumnIndex("member_id")));
                session.setPlanId(cursor.getInt(cursor.getColumnIndex("plan_id")));
                session.setSessionDate(cursor.getString(cursor.getColumnIndex("session_date")));
                session.setDurationMinutes(cursor.getInt(cursor.getColumnIndex("duration_minutes")));
                session.setCaloriesBurned(cursor.getInt(cursor.getColumnIndex("calories_burned")));
                session.setNotes(cursor.getString(cursor.getColumnIndex("notes")));

                // Load exercises for this session
                List<com.gym.fitconnectpro.models.WorkoutLog> exercises = getSessionLogs(session.getSessionId());
                session.setExercises(exercises);

                sessions.add(session);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting workout sessions", e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return sessions;
    }

    /**
     * Get all exercise logs for a specific workout session
     */
    public List<com.gym.fitconnectpro.models.WorkoutLog> getSessionLogs(int sessionId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<com.gym.fitconnectpro.models.WorkoutLog> logs = new java.util.ArrayList<>();

        try {
            db = dbHelper.getReadableDatabase();
            String query = "SELECT wl.*, e.name as exercise_name " +
                          "FROM workout_logs wl " +
                          "LEFT JOIN exercises e ON wl.exercise_id = e.id " +
                          "WHERE wl.session_id = ? " +
                          "ORDER BY wl.exercise_id, wl.set_number";
            cursor = db.rawQuery(query, new String[]{String.valueOf(sessionId)});

            while (cursor.moveToNext()) {
                com.gym.fitconnectpro.models.WorkoutLog log = new com.gym.fitconnectpro.models.WorkoutLog();
                log.setLogId(cursor.getInt(cursor.getColumnIndex("id")));
                log.setSessionId(cursor.getInt(cursor.getColumnIndex("session_id")));
                log.setExerciseId(cursor.getInt(cursor.getColumnIndex("exercise_id")));
                log.setExerciseName(cursor.getString(cursor.getColumnIndex("exercise_name")));
                log.setSetNumber(cursor.getInt(cursor.getColumnIndex("set_number")));
                log.setReps(cursor.getString(cursor.getColumnIndex("reps")));
                log.setWeight(cursor.getDouble(cursor.getColumnIndex("weight")));
                log.setNotes(cursor.getString(cursor.getColumnIndex("notes")));

                logs.add(log);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting session logs", e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return logs;
    }
}
