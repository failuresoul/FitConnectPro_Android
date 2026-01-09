package com.gym.fitconnectpro.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.gym.fitconnectpro.database.DatabaseHelper;
import com.gym.fitconnectpro.models.Trainer;
import com.gym.fitconnectpro.utils.DateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TrainerDAO {

    private static final String TAG = "TrainerDAO";
    private DatabaseHelper dbHelper;

    // Table and column names
    private static final String TABLE_TRAINERS = "trainers";
    private static final String KEY_ID = "id";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_SPECIALIZATION = "specialization";
    private static final String KEY_EXPERIENCE_YEARS = "experience_years";
    private static final String KEY_CERTIFICATION = "certification";
    private static final String KEY_SALARY = "salary";
    private static final String KEY_STATUS = "status";
    private static final String KEY_CREATED_AT = "created_at";
    private static final String KEY_UPDATED_AT = "updated_at";

    public TrainerDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Get all trainers
     */
    public List<Trainer> getAllTrainers() {
        List<Trainer> trainers = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.query(TABLE_TRAINERS, null, null, null, null, null, KEY_FULL_NAME + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    trainers.add(extractTrainerFromCursor(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting all trainers", e);
        } finally {
            DatabaseHelper.closeCursor(cursor);
        }

        return trainers;
    }

    /**
     * Get trainer by ID
     */
    public Trainer getTrainerById(int trainerId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            String selection = KEY_ID + " = ?";
            String[] selectionArgs = {String.valueOf(trainerId)};

            cursor = db.query(TABLE_TRAINERS, null, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                return extractTrainerFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting trainer by ID", e);
        } finally {
            DatabaseHelper.closeCursor(cursor);
        }

        return null;
    }

    /**
     * Add new trainer
     */
    public long addTrainer(Trainer trainer) {
        SQLiteDatabase db = null;

        try {
            db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(KEY_USER_ID, trainer.getUserId());
            values.put(KEY_FULL_NAME, trainer.getFullName());
            values.put(KEY_SPECIALIZATION, trainer.getSpecialization());
            values.put(KEY_EXPERIENCE_YEARS, trainer.getExperienceYears());
            values.put(KEY_CERTIFICATION, trainer.getCertification());
            values.put(KEY_SALARY, trainer.getSalary());
            values.put(KEY_STATUS, "ACTIVE");

            long id = db.insert(TABLE_TRAINERS, null, values);

            if (id != -1) {
                Log.d(TAG, "Trainer added successfully with ID: " + id);
            }

            return id;
        } catch (Exception e) {
            Log.e(TAG, "Error adding trainer", e);
            return -1;
        }
    }

    /**
     * Update trainer
     */
    public boolean updateTrainer(Trainer trainer) {
        SQLiteDatabase db = null;

        try {
            db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(KEY_FULL_NAME, trainer.getFullName());
            values.put(KEY_SPECIALIZATION, trainer.getSpecialization());
            values.put(KEY_EXPERIENCE_YEARS, trainer.getExperienceYears());
            values.put(KEY_CERTIFICATION, trainer.getCertification());
            values.put(KEY_SALARY, trainer.getSalary());
            values.put(KEY_STATUS, trainer.getStatus());
            values.put(KEY_UPDATED_AT, DateUtil.formatSqlDateTime(new Date()));

            String whereClause = KEY_ID + " = ?";
            String[] whereArgs = {String.valueOf(trainer.getId())};

            int rowsAffected = db.update(TABLE_TRAINERS, values, whereClause, whereArgs);

            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating trainer", e);
            return false;
        }
    }

    /**
     * Delete trainer
     */
    public boolean deleteTrainer(int trainerId) {
        SQLiteDatabase db = null;

        try {
            db = dbHelper.getWritableDatabase();

            String whereClause = KEY_ID + " = ?";
            String[] whereArgs = {String.valueOf(trainerId)};

            int rowsAffected = db.delete(TABLE_TRAINERS, whereClause, whereArgs);

            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting trainer", e);
            return false;
        }
    }

    /**
     * Get active trainers count
     */
    public int getActiveTrainersCount() {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            String selection = KEY_STATUS + " = ?";
            String[] selectionArgs = {"ACTIVE"};

            cursor = db.query(TABLE_TRAINERS, new String[]{"COUNT(*)"}, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting active trainers count", e);
        } finally {
            DatabaseHelper.closeCursor(cursor);
        }

        return 0;
    }

    /**
     * Get available trainers (active with capacity)
     */
    public List<Trainer> getAvailableTrainers() {
        List<Trainer> trainers = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            String selection = KEY_STATUS + " = ?";
            String[] selectionArgs = {"ACTIVE"};

            cursor = db.query(TABLE_TRAINERS, null, selection, selectionArgs, null, null, KEY_FULL_NAME + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    trainers.add(extractTrainerFromCursor(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting available trainers", e);
        } finally {
            DatabaseHelper.closeCursor(cursor);
        }

        return trainers;
    }

    /**
     * Extract Trainer object from cursor
     */
    private Trainer extractTrainerFromCursor(Cursor cursor) {
        Trainer trainer = new Trainer();

        int idIndex = cursor.getColumnIndex(KEY_ID);
        int userIdIndex = cursor.getColumnIndex(KEY_USER_ID);
        int fullNameIndex = cursor.getColumnIndex(KEY_FULL_NAME);
        int specializationIndex = cursor.getColumnIndex(KEY_SPECIALIZATION);
        int experienceYearsIndex = cursor.getColumnIndex(KEY_EXPERIENCE_YEARS);
        int certificationIndex = cursor.getColumnIndex(KEY_CERTIFICATION);
        int salaryIndex = cursor.getColumnIndex(KEY_SALARY);
        int statusIndex = cursor.getColumnIndex(KEY_STATUS);

        trainer.setId(cursor.getInt(idIndex));
        trainer.setUserId(cursor.getInt(userIdIndex));
        trainer.setFullName(cursor.getString(fullNameIndex));
        trainer.setSpecialization(cursor.getString(specializationIndex));
        trainer.setExperienceYears(cursor.getInt(experienceYearsIndex));
        trainer.setCertification(cursor.getString(certificationIndex));
        trainer.setSalary(cursor.getDouble(salaryIndex));
        trainer.setStatus(cursor.getString(statusIndex));

        return trainer;
    }
}
