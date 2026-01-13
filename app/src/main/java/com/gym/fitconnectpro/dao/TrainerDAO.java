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
import java.util.Date;
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
            // Database closed by helper or kept open for singleton
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
     * Get trainer by user ID
     */
    public Trainer getTrainerByUserId(int userId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        Trainer trainer = null;

        try {
            db = dbHelper.getReadableDatabase();
            
            Log.d(TAG, "Fetching trainer for user_id: " + userId);
            
            String query = "SELECT t.id, t.user_id, t.full_name, t.specialization, " +
                           "t.experience_years, t.certification, t.salary, t.status, " +
                           "u.username, u.email, u.phone " +
                           "FROM trainers t " +
                           "JOIN users u ON t.user_id = u.id " +
                           "WHERE t.user_id = ?";
            
            cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

            if (cursor != null && cursor.moveToFirst()) {
                trainer = new Trainer();
                trainer.setTrainerId(getIntFromCursor(cursor, "id"));
                trainer.setUserId(getIntFromCursor(cursor, "user_id"));
                trainer.setFullName(getStringFromCursor(cursor, "full_name"));
                trainer.setSpecialization(getStringFromCursor(cursor, "specialization"));
                trainer.setExperienceYears(getIntFromCursor(cursor, "experience_years"));
                trainer.setCertification(getStringFromCursor(cursor, "certification"));
                trainer.setSalary(getDoubleFromCursor(cursor, "salary"));
                trainer.setStatus(getStringFromCursor(cursor, "status"));
                
                // Joined columns - might be missing
                String username = getStringFromCursor(cursor, "username");
                if (username != null) trainer.setUsername(username);
                
                String email = getStringFromCursor(cursor, "email");
                if (email != null) trainer.setEmail(email);
                
                String phone = getStringFromCursor(cursor, "phone");
                if (phone != null) trainer.setPhone(phone);
                
                Log.d(TAG, "Trainer found: " + trainer.getFullName());
            } else {
                Log.e(TAG, "No trainer found for user_id: " + userId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting trainer by user id", e);
            e.printStackTrace();
        } finally {
             if (cursor != null) cursor.close();
        }
        return trainer;
    }

    private String getStringFromCursor(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        if (index != -1) {
            return cursor.getString(index);
        }
        return null;
    }

    private int getIntFromCursor(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        if (index != -1) {
            return cursor.getInt(index);
        }
        return 0;
    }
    
    private double getDoubleFromCursor(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        if (index != -1) {
            return cursor.getDouble(index);
        }
        return 0.0;
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
            // Keep DB open
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
             // Keep DB open
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

    /**
     * Get list of available trainers (all active trainers)
     * @return List of trainers
     */
    public List<Trainer> getAvailableTrainers() {
        List<Trainer> trainers = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            
            String query = "SELECT t.*, u.username, u.email, u.phone " +
                          "FROM trainers t " +
                          "INNER JOIN users u ON t.user_id = u.id " +
                          "WHERE t.status = 'ACTIVE' " +
                          "ORDER BY t.full_name ASC";
            
            cursor = db.rawQuery(query, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Trainer trainer = cursorToTrainer(cursor);
                    // Get current client count
                    int clientCount = getTrainerCurrentClientCount(trainer.getTrainerId());
                    trainer.setAssignedClientsCount(clientCount);
                    trainers.add(trainer);
                } while (cursor.moveToNext());
            }
            
            Log.d(TAG, "Retrieved " + trainers.size() + " available trainers");
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting available trainers", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        
        return trainers;
    }

    /**
     * Get current active client count for a trainer
     * @param trainerId Trainer ID
     * @return Number of active assignments
     */
    public int getTrainerCurrentClientCount(int trainerId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int count = 0;

        try {
            db = dbHelper.getReadableDatabase();
            String query = "SELECT COUNT(*) FROM trainer_assignments " +
                          "WHERE trainer_id = ? AND status = 'ACTIVE'";
            cursor = db.rawQuery(query, new String[]{String.valueOf(trainerId)});

            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting trainer client count", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        
        return count;
    }

    /**
     * Assign a trainer to a member
     * @param trainerId Trainer ID
     * @param memberId Member ID
     * @param assignedDate Assignment start date
     * @return true if successful, false otherwise
     */
    public boolean assignTrainerToMember(int trainerId, int memberId, String assignedDate) {
        SQLiteDatabase db = null;
        
        try {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            
            Log.d(TAG, "=== assignTrainerToMember START ===");
            Log.d(TAG, "Trainer ID: " + trainerId + ", Member ID: " + memberId);
            
            // Check if member already has an active assignment
            Cursor existingCursor = db.rawQuery(
                "SELECT id, trainer_id FROM trainer_assignments WHERE member_id = ? AND status = 'ACTIVE'",
                new String[]{String.valueOf(memberId)}
            );
            
            if (existingCursor != null && existingCursor.moveToFirst()) {
                int existingAssignmentId = existingCursor.getInt(0);
                int existingTrainerId = existingCursor.getInt(1);
                
                Log.d(TAG, "Found existing ACTIVE assignment: ID=" + existingAssignmentId + ", TrainerID=" + existingTrainerId);
                
                // Update existing assignment to COMPLETED
                ContentValues updateValues = new ContentValues();
                updateValues.put("status", "COMPLETED");
                updateValues.put("updated_at", dateFormat.format(new Date()));
                
                int rowsUpdated = db.update("trainer_assignments", updateValues, 
                                 "member_id = ? AND status = 'ACTIVE'", 
                                 new String[]{String.valueOf(memberId)});
                
                Log.d(TAG, "Completed " + rowsUpdated + " previous assignment(s) for member: " + memberId);
            } else {
                Log.d(TAG, "No existing ACTIVE assignment found for member: " + memberId);
            }
            if (existingCursor != null) existingCursor.close();
            
            // Create new assignment
            ContentValues values = new ContentValues();
            values.put("member_id", memberId);
            values.put("trainer_id", trainerId);
            values.put("assigned_date", assignedDate);
            values.put("status", "ACTIVE");
            values.put("created_at", dateFormat.format(new Date()));
            values.put("updated_at", dateFormat.format(new Date()));
            
            long result = db.insert("trainer_assignments", null, values);
            
            if (result != -1) {
                db.setTransactionSuccessful();
                Log.d(TAG, "Successfully assigned trainer " + trainerId + " to member " + memberId + " (new assignment ID: " + result + ")");
                return true;
            } else {
                Log.e(TAG, "Failed to insert new assignment");
            }
            
            return false;
            
        } catch (Exception e) {
            Log.e(TAG, "Error assigning trainer to member", e);
            return false;
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
    }

    /**
     * Get list of members assigned to a trainer
     * @param trainerId Trainer ID
     * @return List of member IDs
     */
    /**
     * Get list of members assigned to a trainer
     * @param trainerId Trainer ID
     * @return List of member IDs
     */
    public List<Integer> getAssignedMembersForTrainer(int trainerId) {
        List<Integer> memberIds = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            String query = "SELECT member_id FROM trainer_assignments " +
                          "WHERE trainer_id = ? AND status = 'ACTIVE'";
            cursor = db.rawQuery(query, new String[]{String.valueOf(trainerId)});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    memberIds.add(cursor.getInt(0));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting assigned members", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        
        return memberIds;
    }

    /**
     * Get list of ClientDetails (Members) assigned to a trainer
     * @param trainerId Trainer ID
     * @return List of Member objects
     */
    public List<com.gym.fitconnectpro.database.entities.Member> getMyAssignedClients(int trainerId) {
        List<com.gym.fitconnectpro.database.entities.Member> clients = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            String query = "SELECT m.member_id, m.full_name, m.email, m.phone, m.date_of_birth, " +
                           "m.gender, m.height, m.weight, m.membership_type, m.membership_fee, " +
                           "m.membership_start_date, m.membership_end_date, m.medical_notes, " +
                           "m.emergency_contact, m.username, m.status, m.registration_date " +
                           "FROM members m " +
                           "JOIN trainer_assignments ta ON m.member_id = ta.member_id " +
                           "WHERE ta.trainer_id = ? AND ta.status = 'ACTIVE' " +
                           "ORDER BY ta.assigned_date DESC";

            cursor = db.rawQuery(query, new String[]{String.valueOf(trainerId)});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    clients.add(cursorToMember(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting assigned clients", e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return clients;
    }

    /**
     * Get client details by Member ID
     * @param memberId Member ID
     * @return Member object
     */
    public com.gym.fitconnectpro.database.entities.Member getClientDetails(int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        com.gym.fitconnectpro.database.entities.Member member = null;

        try {
            db = dbHelper.getReadableDatabase();
            String query = "SELECT * FROM members WHERE member_id = ?";
            
            cursor = db.rawQuery(query, new String[]{String.valueOf(memberId)});

            if (cursor != null && cursor.moveToFirst()) {
                member = cursorToMember(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting client details", e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return member;
    }

    private com.gym.fitconnectpro.database.entities.Member cursorToMember(Cursor cursor) {
        com.gym.fitconnectpro.database.entities.Member member = new com.gym.fitconnectpro.database.entities.Member();
        try {
            int idIndex = cursor.getColumnIndex("member_id");
            if (idIndex != -1) {
                member.setMemberId(cursor.getInt(idIndex));
            } else {
                // Try 'id' or '_id' fallback
                idIndex = cursor.getColumnIndex("id");
                if (idIndex != -1) member.setMemberId(cursor.getInt(idIndex));
                else {
                    idIndex = cursor.getColumnIndex("_id");
                    if (idIndex != -1) member.setMemberId(cursor.getInt(idIndex));
                }
            }
            
            member.setFullName(getStringSafe(cursor, "full_name"));
            member.setEmail(getStringSafe(cursor, "email"));
            member.setPhone(getStringSafe(cursor, "phone"));
            member.setDateOfBirth(getStringSafe(cursor, "date_of_birth"));
            member.setGender(getStringSafe(cursor, "gender"));
            // Safe parsing for doubles if needed, but getDouble handles column index check
            member.setHeight(getDoubleSafe(cursor, "height"));
            member.setWeight(getDoubleSafe(cursor, "weight"));
            member.setMembershipType(getStringSafe(cursor, "membership_type"));
            member.setMembershipFee(getDoubleSafe(cursor, "membership_fee"));
            member.setMembershipStartDate(getStringSafe(cursor, "membership_start_date"));
            member.setMembershipEndDate(getStringSafe(cursor, "membership_end_date"));
            member.setMedicalNotes(getStringSafe(cursor, "medical_notes"));
            member.setEmergencyContact(getStringSafe(cursor, "emergency_contact"));
            member.setUsername(getStringSafe(cursor, "username"));
            member.setStatus(getStringSafe(cursor, "status"));
            member.setCreatedAt(getStringSafe(cursor, "registration_date"));
        } catch (Exception e) {
            Log.e(TAG, "Error parsing member", e);
        }
        return member;
    }

    private String getStringSafe(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        if (index != -1) {
            String val = cursor.getString(index);
            return val != null ? val : "";
        }
        return "";
    }

    private double getDoubleSafe(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        return (index != -1) ? cursor.getDouble(index) : 0.0;
    }
}
