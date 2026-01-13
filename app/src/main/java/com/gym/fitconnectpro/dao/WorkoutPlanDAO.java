package com.gym.fitconnectpro.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.gym.fitconnectpro.database.DatabaseHelper;
import com.gym.fitconnectpro.database.entities.Exercise;
import com.gym.fitconnectpro.database.entities.PlanExercise;
import com.gym.fitconnectpro.database.entities.WorkoutPlan;

import java.util.ArrayList;
import java.util.List;

public class WorkoutPlanDAO {
    private static final String TAG = "WorkoutPlanDAO";
    private DatabaseHelper dbHelper;

    public WorkoutPlanDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Get all available exercises from the library
     * @return List of Exercise objects
     */
    public List<Exercise> getAllExercises() {
        List<Exercise> exercises = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            String query = "SELECT * FROM exercises ORDER BY name ASC";
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Exercise exercise = new Exercise();
                    exercise.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                    exercise.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                    exercise.setMuscleGroup(cursor.getString(cursor.getColumnIndexOrThrow("muscle_group")));
                    exercise.setEquipment(cursor.getString(cursor.getColumnIndexOrThrow("equipment")));
                    exercise.setDifficulty(cursor.getString(cursor.getColumnIndexOrThrow("difficulty")));
                    exercise.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description"))); // Assuming column exists as per Helper
                    
                    exercises.add(exercise);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting exercises", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return exercises;
    }

    /**
     * Create a new workout plan with exercises
     * @param plan The workout plan details
     * @param exercises List of exercises to include in the plan
     * @return true if successful
     */
    public boolean createWorkoutPlan(WorkoutPlan plan, List<PlanExercise> exercises) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();

            // 1. Insert Workout Plan Header
            ContentValues planValues = new ContentValues();
            planValues.put("trainer_id", plan.getTrainerId());
            planValues.put("member_id", plan.getMemberId());
            planValues.put("plan_name", plan.getPlanName());
            planValues.put("start_date", plan.getStartDate());
            planValues.put("end_date", plan.getEndDate());
            planValues.put("status", "ACTIVE"); // Default to ACTIVE

            long planId = db.insert("workout_plans", null, planValues);

            if (planId == -1) {
                Log.e(TAG, "Failed to insert workout plan header");
                return false;
            }

            // 2. Insert Plan Exercises
            int orderIndex = 0;
            for (PlanExercise ex : exercises) {
                ContentValues exValues = new ContentValues();
                exValues.put("plan_id", planId);
                exValues.put("exercise_id", ex.getExerciseId());
                exValues.put("sets", ex.getSets());
                exValues.put("reps", ex.getReps());
                exValues.put("weight_kg", ex.getWeightKg());
                exValues.put("rest_seconds", ex.getRestSeconds());
                exValues.put("notes", ex.getNotes());
                exValues.put("order_index", orderIndex++);

                long exId = db.insert("plan_exercises", null, exValues);
                if (exId == -1) {
                    Log.e(TAG, "Failed to insert plan exercise: " + ex.getExerciseId());
                    return false; // Rollback
                }
            }

            db.setTransactionSuccessful();
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error creating workout plan", e);
            return false;
        } finally {
            if (db != null) db.endTransaction();
        }
    }
    /**
     * Get all workout plans assigned to a specific member
     * @param memberId The ID of the member
     * @return List of WorkoutPlan objects
     */
    public List<WorkoutPlan> getPlansByMemberId(int memberId) {
        List<WorkoutPlan> plans = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            // Select all plans for this member, newest first
            String query = "SELECT * FROM workout_plans " +
                           "WHERE member_id = ? " +
                           "ORDER BY created_at DESC";
            cursor = db.rawQuery(query, new String[]{String.valueOf(memberId)});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    WorkoutPlan plan = new WorkoutPlan();
                    plan.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                    plan.setTrainerId(cursor.getInt(cursor.getColumnIndexOrThrow("trainer_id")));
                    plan.setMemberId(cursor.getInt(cursor.getColumnIndexOrThrow("member_id")));
                    plan.setPlanName(cursor.getString(cursor.getColumnIndexOrThrow("plan_name")));
                    plan.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow("start_date")));
                    plan.setEndDate(cursor.getString(cursor.getColumnIndexOrThrow("end_date")));
                    plan.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
                    
                    plans.add(plan);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting member plans", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return plans;
    }
    /**
     * Get exercises for a specific plan
     * @param planId The workout plan ID
     * @return List of PlanExercise objects (populated with Exercise details)
     */
    public List<PlanExercise> getPlanExercises(int planId) {
        List<PlanExercise> exercises = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            // Join plan_exercises with exercises to get key details
            String query = "SELECT pe.*, e.name, e.muscle_group FROM plan_exercises pe " +
                           "JOIN exercises e ON pe.exercise_id = e.id " +
                           "WHERE pe.plan_id = ? " +
                           "ORDER BY pe.order_index ASC";
            
            cursor = db.rawQuery(query, new String[]{String.valueOf(planId)});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    PlanExercise ex = new PlanExercise();
                    ex.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                    ex.setPlanId(cursor.getInt(cursor.getColumnIndexOrThrow("plan_id")));
                    ex.setExerciseId(cursor.getInt(cursor.getColumnIndexOrThrow("exercise_id")));
                    ex.setSets(cursor.getInt(cursor.getColumnIndexOrThrow("sets")));
                    ex.setReps(cursor.getString(cursor.getColumnIndexOrThrow("reps")));
                    ex.setWeightKg(cursor.getDouble(cursor.getColumnIndexOrThrow("weight_kg")));
                    ex.setRestSeconds(cursor.getInt(cursor.getColumnIndexOrThrow("rest_seconds")));
                    ex.setNotes(cursor.getString(cursor.getColumnIndexOrThrow("notes")));
                    
                    // Create transient Exercise object for display
                    Exercise exerciseDetails = new Exercise();
                    exerciseDetails.setId(ex.getExerciseId());
                    exerciseDetails.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                    exerciseDetails.setMuscleGroup(cursor.getString(cursor.getColumnIndexOrThrow("muscle_group")));
                    ex.setExercise(exerciseDetails);

                    exercises.add(ex);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting plan exercises", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return exercises;
    }

    public WorkoutPlan getPlanForDate(int memberId, String date) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            String query = "SELECT * FROM workout_plans " +
                           "WHERE member_id = ? " +
                           "AND start_date <= ? AND end_date >= ? " +
                           "AND status = 'ACTIVE' " +
                           "ORDER BY created_at DESC LIMIT 1";
            
            cursor = db.rawQuery(query, new String[]{String.valueOf(memberId), date, date});
            
            if (cursor != null && cursor.moveToFirst()) {
                WorkoutPlan plan = new WorkoutPlan();
                plan.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                plan.setTrainerId(cursor.getInt(cursor.getColumnIndexOrThrow("trainer_id")));
                plan.setMemberId(cursor.getInt(cursor.getColumnIndexOrThrow("member_id")));
                plan.setPlanName(cursor.getString(cursor.getColumnIndexOrThrow("plan_name")));
                // Try catch for new columns
                try {
                    plan.setFocusArea(cursor.getString(cursor.getColumnIndexOrThrow("focus_area")));
                    plan.setInstructions(cursor.getString(cursor.getColumnIndexOrThrow("instructions")));
                } catch (IllegalArgumentException e) {
                   // Columns might not exist in old version cursor? 
                   // No, query SELECT * should return them if DB upgraded.
                   // But safe to ignore if null.
                }
                plan.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow("start_date")));
                plan.setEndDate(cursor.getString(cursor.getColumnIndexOrThrow("end_date")));
                plan.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
                return plan;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting plan for date: " + date, e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public boolean updatePlanStatus(int planId, String status) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("status", status);
            
            int rows = db.update("workout_plans", values, "id = ?", new String[]{String.valueOf(planId)});
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating plan status", e);
            return false;
        }
    }
}
