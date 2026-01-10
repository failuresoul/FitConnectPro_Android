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
     * Register a new member
     * @param member Member object with all details
     * @return "SUCCESS" if successful, error message otherwise
     */
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
     * @param db SQLiteDatabase instance
     * @param username Username to check
     * @return true if exists, false otherwise
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
            // Do NOT close db here, as it's passed from caller
        }
    }

    /**
     * Get all members
     * @return List of all members
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
                    member.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
                    member.setRegistrationDate(cursor.getString(cursor.getColumnIndexOrThrow("registration_date")));

                    members.add(member);
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
     * Update member information
     * @param member Member object with updated details
     * @return true if update successful, false otherwise
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
     * @param memberId Member ID to delete
     * @return true if deletion successful, false otherwise
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
}
