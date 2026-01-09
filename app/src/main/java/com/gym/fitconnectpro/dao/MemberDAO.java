package com.gym.fitconnectpro.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.gym.fitconnectpro.database.DatabaseHelper;
import com.gym.fitconnectpro.models.Member;
import com.gym.fitconnectpro.utils.DateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MemberDAO {

    private static final String TAG = "MemberDAO";
    private DatabaseHelper dbHelper;

    // Table and column names
    private static final String TABLE_MEMBERS = "members";
    private static final String KEY_ID = "id";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_DATE_OF_BIRTH = "date_of_birth";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_EMERGENCY_CONTACT = "emergency_contact";
    private static final String KEY_MEDICAL_CONDITIONS = "medical_conditions";
    private static final String KEY_JOIN_DATE = "join_date";
    private static final String KEY_STATUS = "status";
    private static final String KEY_CREATED_AT = "created_at";
    private static final String KEY_UPDATED_AT = "updated_at";

    public MemberDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
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
            cursor = db.query(TABLE_MEMBERS, null, null, null, null, null, KEY_FULL_NAME + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    members.add(extractMemberFromCursor(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting all members", e);
        } finally {
            DatabaseHelper.closeCursor(cursor);
        }

        return members;
    }

    /**
     * Get member by ID
     */
    public Member getMemberById(int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            String selection = KEY_ID + " = ?";
            String[] selectionArgs = {String.valueOf(memberId)};

            cursor = db.query(TABLE_MEMBERS, null, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                return extractMemberFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting member by ID", e);
        } finally {
            DatabaseHelper.closeCursor(cursor);
        }

        return null;
    }

    /**
     * Add new member
     */
    public long addMember(Member member) {
        SQLiteDatabase db = null;

        try {
            db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(KEY_USER_ID, member.getUserId());
            values.put(KEY_FULL_NAME, member.getFullName());
            values.put(KEY_DATE_OF_BIRTH, member.getDateOfBirth());
            values.put(KEY_GENDER, member.getGender());
            values.put(KEY_ADDRESS, member.getAddress());
            values.put(KEY_EMERGENCY_CONTACT, member.getEmergencyContact());
            values.put(KEY_MEDICAL_CONDITIONS, member.getMedicalConditions());
            values.put(KEY_JOIN_DATE, DateUtil.formatSqlDate(new Date()));
            values.put(KEY_STATUS, "ACTIVE");

            long id = db.insert(TABLE_MEMBERS, null, values);

            if (id != -1) {
                Log.d(TAG, "Member added successfully with ID: " + id);
            }

            return id;
        } catch (Exception e) {
            Log.e(TAG, "Error adding member", e);
            return -1;
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
            values.put(KEY_FULL_NAME, member.getFullName());
            values.put(KEY_DATE_OF_BIRTH, member.getDateOfBirth());
            values.put(KEY_GENDER, member.getGender());
            values.put(KEY_ADDRESS, member.getAddress());
            values.put(KEY_EMERGENCY_CONTACT, member.getEmergencyContact());
            values.put(KEY_MEDICAL_CONDITIONS, member.getMedicalConditions());
            values.put(KEY_STATUS, member.getStatus());
            values.put(KEY_UPDATED_AT, DateUtil.formatSqlDateTime(new Date()));

            String whereClause = KEY_ID + " = ?";
            String[] whereArgs = {String.valueOf(member.getId())};

            int rowsAffected = db.update(TABLE_MEMBERS, values, whereClause, whereArgs);

            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating member", e);
            return false;
        }
    }

    /**
     * Delete member
     */
    public boolean deleteMember(int memberId) {
        SQLiteDatabase db = null;

        try {
            db = dbHelper.getWritableDatabase();

            String whereClause = KEY_ID + " = ?";
            String[] whereArgs = {String.valueOf(memberId)};

            int rowsAffected = db.delete(TABLE_MEMBERS, whereClause, whereArgs);

            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting member", e);
            return false;
        }
    }

    /**
     * Get active members count
     */
    public int getActiveMembersCount() {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            String selection = KEY_STATUS + " = ?";
            String[] selectionArgs = {"ACTIVE"};

            cursor = db.query(TABLE_MEMBERS, new String[]{"COUNT(*)"}, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting active members count", e);
        } finally {
            DatabaseHelper.closeCursor(cursor);
        }

        return 0;
    }

    /**
     * Extract Member object from cursor
     */
    private Member extractMemberFromCursor(Cursor cursor) {
        Member member = new Member();

        int idIndex = cursor.getColumnIndex(KEY_ID);
        int userIdIndex = cursor.getColumnIndex(KEY_USER_ID);
        int fullNameIndex = cursor.getColumnIndex(KEY_FULL_NAME);
        int dobIndex = cursor.getColumnIndex(KEY_DATE_OF_BIRTH);
        int genderIndex = cursor.getColumnIndex(KEY_GENDER);
        int addressIndex = cursor.getColumnIndex(KEY_ADDRESS);
        int emergencyContactIndex = cursor.getColumnIndex(KEY_EMERGENCY_CONTACT);
        int medicalConditionsIndex = cursor.getColumnIndex(KEY_MEDICAL_CONDITIONS);
        int joinDateIndex = cursor.getColumnIndex(KEY_JOIN_DATE);
        int statusIndex = cursor.getColumnIndex(KEY_STATUS);

        member.setId(cursor.getInt(idIndex));
        member.setUserId(cursor.getInt(userIdIndex));
        member.setFullName(cursor.getString(fullNameIndex));
        member.setDateOfBirth(cursor.getString(dobIndex));
        member.setGender(cursor.getString(genderIndex));
        member.setAddress(cursor.getString(addressIndex));
        member.setEmergencyContact(cursor.getString(emergencyContactIndex));
        member.setMedicalConditions(cursor.getString(medicalConditionsIndex));
        member.setJoinDate(cursor.getString(joinDateIndex));
        member.setStatus(cursor.getString(statusIndex));

        return member;
    }
}
