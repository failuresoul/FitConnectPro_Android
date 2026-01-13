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
}
