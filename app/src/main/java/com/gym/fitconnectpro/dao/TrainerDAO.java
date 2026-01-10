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
import java.util.ArrayList;
import java.util.List;
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

            if (isUsernameExists(trainer.getUsername())) {
                Log.e(TAG, "Username already exists: " + trainer.getUsername());
                return false;
            }

            String hashedPassword;
            try {
                hashedPassword = BCrypt.hashpw(trainer.getPassword(), BCrypt.gensalt());
            } catch (Exception e) {
                Log.e(TAG, "Password Hashing Failed", e);
                return false;
            }

            db.beginTransaction();

            try {
                ContentValues userValues = new ContentValues();
                userValues.put("username", trainer.getUsername());
                userValues.put("password", hashedPassword);
                userValues.put("user_type", "TRAINER");
                userValues.put("email", trainer.getEmail());
                userValues.put("phone", trainer.getPhone());
                userValues.put("status", trainer.getStatus() != null ? trainer.getStatus() : "ACTIVE");

                long userId = db.insert("users", null, userValues);

                if (userId == -1) {
                    return false;
                }

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

    /**
     * Get all trainers
     */
    public List<Trainer> getAllTrainers() {
        List<Trainer> trainers = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            // Join users table to get username, email, phone if needed slightly updated, but simpler to select from trainers joined with users is better
            // Ideally we need id from trainers table
            String query = "SELECT t.*, u.username, u.email, u.phone FROM trainers t " +
                           "JOIN users u ON t.user_id = u.id " +
                           "ORDER BY t.created_at DESC";
                           
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    trainers.add(cursorToTrainer(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting all trainers", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        return trainers;
    }

    /**
     * Get trainer by ID
     */
    public Trainer getTrainerById(int trainerId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        Trainer trainer = null;

        try {
            db = dbHelper.getReadableDatabase();
            String query = "SELECT t.*, u.username, u.email, u.phone FROM trainers t " +
                           "JOIN users u ON t.user_id = u.id " +
                           "WHERE t.id = ?";
            
            cursor = db.rawQuery(query, new String[]{String.valueOf(trainerId)});

            if (cursor != null && cursor.moveToFirst()) {
                trainer = cursorToTrainer(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting trainer by id", e);
        } finally {
             if (cursor != null) cursor.close();
        }
        return trainer;
    }

    /**
     * Get assigned clients count
     */
    public int getAssignedClientsCount(int trainerId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int count = 0;

        try {
            db = dbHelper.getReadableDatabase();
            String query = "SELECT COUNT(*) FROM trainer_assignments WHERE trainer_id = ? AND status = 'ACTIVE'";
            cursor = db.rawQuery(query, new String[]{String.valueOf(trainerId)});

            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting assigned clients count", e);
        } finally {
             if (cursor != null) cursor.close();
        }
        return count;
    }

    /**
     * Update trainer
     */
    public boolean updateTrainer(Trainer trainer) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            
            try {
                // Update trainers table
                ContentValues trainerValues = new ContentValues();
                trainerValues.put("full_name", trainer.getFullName());
                trainerValues.put("specialization", trainer.getSpecialization());
                trainerValues.put("experience_years", trainer.getExperienceYears());
                trainerValues.put("certification", trainer.getCertification());
                trainerValues.put("salary", trainer.getSalary());
                trainerValues.put("status", trainer.getStatus());
                
                int tRows = db.update("trainers", trainerValues, "id = ?", new String[]{String.valueOf(trainer.getTrainerId())});
                
                // Update users table (email, phone, status)
                ContentValues userValues = new ContentValues();
                userValues.put("email", trainer.getEmail());
                userValues.put("phone", trainer.getPhone());
                userValues.put("status", trainer.getStatus()); // Sync status
                
                int uRows = db.update("users", userValues, "id = ?", new String[]{String.valueOf(trainer.getUserId())});
                
                if (tRows > 0 || uRows > 0) {
                     db.setTransactionSuccessful();
                     return true;
                }
                return false;
                
            } finally {
                db.endTransaction();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating trainer", e);
            return false;
        } finally {
            if (db != null && db.isOpen()) db.close();
        }
    }
    
    /**
     * Delete trainer
     */
    public boolean deleteTrainer(int trainerId) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            // Due to foreign keys ON DELETE CASCADE, deleting from users will remove from trainers
            // But we need the user_id first. Or we can just delete from trainers and users separately/cascade.
            
            // First get user_id to delete the user account too
            int userId = -1;
            Cursor cursor = db.query("trainers", new String[]{"user_id"}, "id = ?", new String[]{String.valueOf(trainerId)}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                userId = cursor.getInt(0);
                cursor.close();
            }
            
            if (userId != -1) {
                // Delete user (Cascades to trainer)
                return db.delete("users", "id = ?", new String[]{String.valueOf(userId)}) > 0;
            } else {
                // Fallback: delete from trainers directly if user link is broken
                 return db.delete("trainers", "id = ?", new String[]{String.valueOf(trainerId)}) > 0;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error deleting trainer", e);
            return false;
        } finally {
             if (db != null && db.isOpen()) db.close();
        }
    }

    private Trainer cursorToTrainer(Cursor cursor) {
        Trainer trainer = new Trainer();
        trainer.setTrainerId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        trainer.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        trainer.setFullName(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
        trainer.setSpecialization(cursor.getString(cursor.getColumnIndexOrThrow("specialization")));
        trainer.setExperienceYears(cursor.getInt(cursor.getColumnIndexOrThrow("experience_years")));
        trainer.setCertification(cursor.getString(cursor.getColumnIndexOrThrow("certification")));
        trainer.setSalary(cursor.getDouble(cursor.getColumnIndexOrThrow("salary")));
        trainer.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
        
        // Joined columns
        try {
            trainer.setUsername(cursor.getString(cursor.getColumnIndexOrThrow("username")));
            trainer.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            trainer.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
        } catch (IllegalArgumentException e) {
            // Columns might not be present if not joined
        }
        
        return trainer;
    }
}
