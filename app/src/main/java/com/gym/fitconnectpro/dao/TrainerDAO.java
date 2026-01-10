package com.gym.fitconnectpro.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.gym.fitconnectpro.database.DatabaseHelper;
import com.gym.fitconnectpro.database.entities.Trainer;

import org.mindrot.jbcrypt.BCrypt;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TrainerDAO {
    private static final String TAG = "TrainerDAO";
    private DatabaseHelper dbHelper;
    private SimpleDateFormat dateFormat;

    public TrainerDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    }

    /**
     * Register a new trainer
     * @param trainer Trainer object with all details
     * @return true if successful, false otherwise
     */
    public boolean registerTrainer(Trainer trainer) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();

            // Check if username already exists
            if (isUsernameExists(trainer.getUsername())) {
                Log.e(TAG, "Username already exists: " + trainer.getUsername());
                return false;
            }

            // Hash the password using BCrypt
            String hashedPassword;
            try {
                // Use the password set in the entity (mapped from setPasswordHash)
                hashedPassword = BCrypt.hashpw(trainer.getPassword(), BCrypt.gensalt());
            } catch (Exception e) {
                Log.e(TAG, "Password Hashing Failed", e);
                return false;
            }

            db.beginTransaction();

            try {
                // 1. Insert into Users table
                ContentValues userValues = new ContentValues();
                userValues.put("username", trainer.getUsername());
                userValues.put("password", hashedPassword);
                userValues.put("user_type", "TRAINER");
                userValues.put("email", trainer.getEmail());
                userValues.put("phone", trainer.getPhone());
                userValues.put("status", trainer.getStatus() != null ? trainer.getStatus() : "ACTIVE");

                long userId = db.insert("users", null, userValues);

                if (userId == -1) {
                    Log.e(TAG, "Failed to create User account.");
                    return false;
                }

                // 2. Insert into Trainers table
                ContentValues trainerValues = new ContentValues();
                trainerValues.put("user_id", userId);
                trainerValues.put("full_name", trainer.getFullName());
                trainerValues.put("specialization", trainer.getSpecialization());
                trainerValues.put("experience_years", trainer.getExperienceYears());
                trainerValues.put("certification", trainer.getCertification());
                trainerValues.put("salary", trainer.getSalary());
                trainerValues.put("status", trainer.getStatus() != null ? trainer.getStatus() : "ACTIVE");

                long trainerId = db.insert("trainers", null, trainerValues);

                if (trainerId != -1) {
                    db.setTransactionSuccessful();
                    Log.i(TAG, "Trainer registered successfully: " + trainer.getUsername());
                    return true;
                } else {
                    Log.e(TAG, "Failed to create Trainer profile.");
                    return false;
                }

            } finally {
                db.endTransaction();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error registering trainer: " + e.getMessage(), e);
            return false;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    /**
     * Check if username already exists
     * Public method used by Activity
     */
    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.query("users", new String[]{"id"}, "username = ?", new String[]{username}, null, null, null);
            return cursor != null && cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking username: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) cursor.close();
        }
    }
}
