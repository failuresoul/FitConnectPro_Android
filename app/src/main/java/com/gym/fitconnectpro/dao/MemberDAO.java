package com.gym.fitconnectpro.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.gym.fitconnectpro.database.DatabaseHelper;
import com.gym.fitconnectpro.database.entities.Member;

import org.mindrot.jbcrypt.BCrypt;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MemberDAO {
    private static final String TAG = "MemberDAO";
    private DatabaseHelper dbHelper;
    private SimpleDateFormat dateFormat;

    public MemberDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    }

    /**
     * Register a new member and record initial payment
     * @param member Member object with all details
     * @return "SUCCESS" if successful, error message otherwise
     */
    public String registerMember(Member member) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();

            // Check if username already exists
            if (isUsernameExists(db, member.getUsername())) {
                Log.e(TAG, "Username already exists: " + member.getUsername());
                return "Username already exists: " + member.getUsername();
            }

            // Hash the password using BCrypt
            String hashedPassword;
            try {
                hashedPassword = BCrypt.hashpw(member.getPassword(), BCrypt.gensalt());
            } catch (Exception e) {
                return "Password Hashing Failed: " + e.getMessage();
            }

            db.beginTransaction(); // Start Transaction

            try {
                // Prepare Member values
                ContentValues memberValues = new ContentValues();
                memberValues.put("full_name", member.getFullName());
                memberValues.put("email", member.getEmail());
                memberValues.put("phone", member.getPhone());
                memberValues.put("date_of_birth", member.getDateOfBirth());
                memberValues.put("gender", member.getGender());
                memberValues.put("height", member.getHeight());
                memberValues.put("weight", member.getWeight());
                memberValues.put("membership_type", member.getMembershipType());
                memberValues.put("membership_fee", member.getMembershipFee());
                memberValues.put("membership_start_date", member.getMembershipStartDate());
                memberValues.put("membership_end_date", member.getMembershipEndDate());
                memberValues.put("medical_notes", member.getMedicalNotes());
                memberValues.put("emergency_contact", member.getEmergencyContact());
                memberValues.put("username", member.getUsername());
                memberValues.put("password", hashedPassword);
                memberValues.put("status", member.getStatus());
                memberValues.put("registration_date", dateFormat.format(new Date()));

                // Insert member
                long memberId = db.insert("members", null, memberValues);

                if (memberId == -1) {
                    return "Database Insert Failed (Result -1). Check constraints.";
                }

                // Prepare Payment values (Auto-record initial payment)
                ContentValues paymentValues = new ContentValues();
                paymentValues.put("member_id", memberId);
                paymentValues.put("amount", member.getMembershipFee());
                paymentValues.put("payment_date", dateFormat.format(new Date())); // Today
                paymentValues.put("payment_method", "CASH"); // Default
                paymentValues.put("status", "COMPLETED"); // Assume paid on registration

                // Insert Payment
                long paymentId = db.insert("payments", null, paymentValues);

                if (paymentId != -1) {
                    db.setTransactionSuccessful(); // Commit if both succeed
                    Log.i(TAG, "Member registered with payment: " + member.getUsername());
                    return "SUCCESS";
                } else {
                    return "Member Created but Payment Failed.";
                }

            } finally {
                db.endTransaction(); // End Transaction
            }

        } catch (Exception e) {
            Log.e(TAG, "Error registering member: " + e.getMessage(), e);
            return "Error: " + e.getMessage();
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    /**
     * Check if username already exists
     */
    private boolean isUsernameExists(SQLiteDatabase db, String username) {
        Cursor cursor = null;
        try {
            cursor = db.query("members",
                    new String[]{"member_id"},
                    "username = ?",
                    new String[]{username},
                    null, null, null);

            return cursor != null && cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking username: " + e.getMessage(), e);
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Get all members
     */
    public List<Member> getAllMembers() {
        List<Member> members = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.query("members", null, null, null, null, null, "registration_date DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    members.add(cursorToMember(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting all members: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return members;
    }

    /**
     * Search members by keyword
     */
    public List<Member> searchMembers(String keyword) {
        List<Member> memberList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            String searchPattern = "%" + keyword + "%";
            String selection = "full_name LIKE ? OR email LIKE ? OR phone LIKE ? OR username LIKE ?";
            String[] selectionArgs = {searchPattern, searchPattern, searchPattern, searchPattern};

            cursor = db.query("members", null, selection, selectionArgs, null, null, "registration_date DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    memberList.add(cursorToMember(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error searching members: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        return memberList;
    }

    /**
     * Filter members by status
     */
    public List<Member> filterMembersByStatus(String status) {
        List<Member> memberList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            String selection = null;
            String[] selectionArgs = null;

            if (!"All".equals(status)) {
                selection = "status = ?";
                selectionArgs = new String[]{status};
            }

            cursor = db.query("members", null, selection, selectionArgs, null, null, "registration_date DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    memberList.add(cursorToMember(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error filtering members: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        return memberList;
    }

    /**
     * Update member status
     */
    public boolean updateMemberStatus(int memberId, String status) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("status", status);

            int rowsAffected = db.update("members", values,
                    "member_id = ?", new String[]{String.valueOf(memberId)});
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating member status: " + e.getMessage());
            return false;
        } finally {
            if (db != null && db.isOpen()) db.close();
        }
    }

    /**
     * Update member
     */
    public boolean updateMember(Member member) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("full_name", member.getFullName());
            values.put("email", member.getEmail());
            values.put("phone", member.getPhone());
            values.put("date_of_birth", member.getDateOfBirth());
            values.put("gender", member.getGender());
            values.put("height", member.getHeight());
            values.put("weight", member.getWeight());
            values.put("membership_type", member.getMembershipType());
            values.put("membership_fee", member.getMembershipFee());
            values.put("membership_start_date", member.getMembershipStartDate());
            values.put("membership_end_date", member.getMembershipEndDate());
            values.put("medical_notes", member.getMedicalNotes());
            values.put("emergency_contact", member.getEmergencyContact());
            values.put("status", member.getStatus());

            int result = db.update("members", values, "member_id = ?",
                    new String[]{String.valueOf(member.getMemberId())});

            return result > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating member: " + e.getMessage(), e);
            return false;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    /**
     * Delete member
     */
    public boolean deleteMember(int memberId) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            int result = db.delete("members", "member_id = ?",
                    new String[]{String.valueOf(memberId)});
            return result > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting member: " + e.getMessage(), e);
            return false;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    // Helper method to convert cursor to Member object
    private Member cursorToMember(Cursor cursor) {
        Member member = new Member();
        member.setMemberId(cursor.getInt(cursor.getColumnIndexOrThrow("member_id")));
        member.setFullName(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
        member.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
        member.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
        member.setDateOfBirth(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
        member.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
        member.setHeight(cursor.getDouble(cursor.getColumnIndexOrThrow("height")));
        member.setWeight(cursor.getDouble(cursor.getColumnIndexOrThrow("weight")));
        member.setMembershipType(cursor.getString(cursor.getColumnIndexOrThrow("membership_type")));
        member.setMembershipFee(cursor.getDouble(cursor.getColumnIndexOrThrow("membership_fee")));
        member.setMembershipStartDate(cursor.getString(cursor.getColumnIndexOrThrow("membership_start_date")));
        member.setMembershipEndDate(cursor.getString(cursor.getColumnIndexOrThrow("membership_end_date")));
        member.setMedicalNotes(cursor.getString(cursor.getColumnIndexOrThrow("medical_notes")));
        member.setEmergencyContact(cursor.getString(cursor.getColumnIndexOrThrow("emergency_contact")));
        member.setUsername(cursor.getString(cursor.getColumnIndexOrThrow("username")));
        member.setPassword(cursor.getString(cursor.getColumnIndexOrThrow("password"))); // Set password if needed, but usually not for list
        member.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
        
        // Correctly handling registration_date
        try {
             String regDate = cursor.getString(cursor.getColumnIndexOrThrow("registration_date"));
             member.setCreatedAt(regDate);
        } catch (IllegalArgumentException e) {
             // Column might not exist in old schema versions, handle gracefully
             Log.w(TAG, "registration_date column not found");
        }
        
        return member;
    }

    /**
     * Get the assigned trainer ID for a member
     * @param memberId Member ID
     * @return Trainer ID if assigned, null otherwise
     */
    public Integer getAssignedTrainerId(int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            String query = "SELECT trainer_id FROM trainer_assignments " +
                          "WHERE member_id = ? AND status = 'ACTIVE' " +
                          "ORDER BY assigned_date DESC LIMIT 1";
            cursor = db.rawQuery(query, new String[]{String.valueOf(memberId)});

            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting assigned trainer", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        
        return null;
    }

    /**
     * Get list of members without active trainer assignments
     * @return List of unassigned members
     */
    public List<Member> getUnassignedMembers() {
        List<Member> members = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            
            // Get all active members first
            String selection = "status = ?";
            String[] selectionArgs = {"ACTIVE"};
            
            cursor = db.query("members", null, selection, selectionArgs, 
                            null, null, "full_name ASC");
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Member member = cursorToMember(cursor);
                    
                    // Check if member has active assignment
                    Integer trainerId = getAssignedTrainerId(member.getMemberId());
                    if (trainerId == null) {
                        members.add(member);
                    }
                } while (cursor.moveToNext());
            }
            
            Log.d(TAG, "Retrieved " + members.size() + " unassigned members");
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting unassigned members: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        
        return members;
    }

    /**
     * Get all active members
     * @return List of all active members
     */
    public List<Member> getAllActiveMembers() {
        List<Member> members = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            
            // First, let's get ALL members to see what we have
            cursor = db.query("members", null, null, null, null, null, "full_name ASC");
            
            Log.d(TAG, "Query executed, total cursor count: " + (cursor != null ? cursor.getCount() : "null"));
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    try {
                        Member member = cursorToMember(cursor);
                        
                        // Log the status value to see what's actually in the database
                        String status = member.getStatus();
                        Log.d(TAG, "Member: " + member.getFullName() + ", Status: '" + status + "'");
                        
                        // Check for ACTIVE status (case-insensitive)
                        if (status != null && status.equalsIgnoreCase("ACTIVE")) {
                            members.add(member);
                            Log.d(TAG, "Added ACTIVE member: " + member.getFullName());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing member row: " + e.getMessage(), e);
                    }
                } while (cursor.moveToNext());
            } else {
                Log.w(TAG, "No members found in database or cursor is null");
            }
            
            Log.d(TAG, "Retrieved " + members.size() + " active members out of " + 
                  (cursor != null ? cursor.getCount() : 0) + " total");
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting active members: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        
        return members;
    }
}

