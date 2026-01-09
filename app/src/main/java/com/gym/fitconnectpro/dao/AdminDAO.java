package com.gym.fitconnectpro.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.gym.fitconnectpro.database.DatabaseHelper;
import com.gym.fitconnectpro.models.User;
import com.gym.fitconnectpro.utils.PasswordUtil;

import java.util.ArrayList;
import java.util.List;

public class AdminDAO {

    private static final String TAG = "AdminDAO";
    private DatabaseHelper dbHelper;

    // Table and column names
    private static final String TABLE_USERS = "users";
    private static final String KEY_ID = "id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_USER_TYPE = "user_type";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_STATUS = "status";

    public AdminDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Authenticate admin user
     */
    public User authenticateAdmin(String username, String password) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();

            String selection = KEY_USERNAME + " = ? AND " + KEY_USER_TYPE + " = ?";
            String[] selectionArgs = {username, "ADMIN"};

            cursor = db.query(
                TABLE_USERS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
            );

            if (cursor != null && cursor.moveToFirst()) {
                int passwordIndex = cursor.getColumnIndex(KEY_PASSWORD);
                String hashedPassword = cursor.getString(passwordIndex);

                if (PasswordUtil.verifyPassword(password, hashedPassword)) {
                    return extractUserFromCursor(cursor);
                }
            }

            return null;

        } catch (Exception e) {
            Log.e(TAG, "Error authenticating admin", e);
            return null;
        } finally {
            DatabaseHelper.closeCursor(cursor);
        }
    }

    /**
     * Get admin by ID
     */
    public User getAdminById(int adminId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();

            String selection = KEY_ID + " = ? AND " + KEY_USER_TYPE + " = ?";
            String[] selectionArgs = {String.valueOf(adminId), "ADMIN"};

            cursor = db.query(
                TABLE_USERS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
            );

            if (cursor != null && cursor.moveToFirst()) {
                return extractUserFromCursor(cursor);
            }

            return null;

        } catch (Exception e) {
            Log.e(TAG, "Error getting admin by ID", e);
            return null;
        } finally {
            DatabaseHelper.closeCursor(cursor);
        }
    }

    /**
     * Get all admins
     */
    public List<User> getAllAdmins() {
        List<User> admins = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();

            String selection = KEY_USER_TYPE + " = ?";
            String[] selectionArgs = {"ADMIN"};

            cursor = db.query(
                TABLE_USERS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                KEY_USERNAME + " ASC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    admins.add(extractUserFromCursor(cursor));
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            Log.e(TAG, "Error getting all admins", e);
        } finally {
            DatabaseHelper.closeCursor(cursor);
        }

        return admins;
    }

    /**
     * Update admin profile
     */
    public boolean updateAdmin(User admin) {
        SQLiteDatabase db = null;

        try {
            db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(KEY_EMAIL, admin.getEmail());
            values.put(KEY_PHONE, admin.getPhone());
            values.put(KEY_STATUS, admin.getStatus());

            String whereClause = KEY_ID + " = ? AND " + KEY_USER_TYPE + " = ?";
            String[] whereArgs = {String.valueOf(admin.getId()), "ADMIN"};

            int rowsAffected = db.update(TABLE_USERS, values, whereClause, whereArgs);

            return rowsAffected > 0;

        } catch (Exception e) {
            Log.e(TAG, "Error updating admin", e);
            return false;
        }
    }

    /**
     * Change admin password
     */
    public boolean changePassword(int adminId, String oldPassword, String newPassword) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getWritableDatabase();

            // Verify old password
            String selection = KEY_ID + " = ? AND " + KEY_USER_TYPE + " = ?";
            String[] selectionArgs = {String.valueOf(adminId), "ADMIN"};

            cursor = db.query(TABLE_USERS, new String[]{KEY_PASSWORD}, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int passwordIndex = cursor.getColumnIndex(KEY_PASSWORD);
                String hashedPassword = cursor.getString(passwordIndex);

                if (!PasswordUtil.verifyPassword(oldPassword, hashedPassword)) {
                    return false;
                }

                // Update with new password
                String newHashedPassword = PasswordUtil.hashPassword(newPassword);

                ContentValues values = new ContentValues();
                values.put(KEY_PASSWORD, newHashedPassword);

                int rowsAffected = db.update(TABLE_USERS, values, selection, selectionArgs);

                return rowsAffected > 0;
            }

            return false;

        } catch (Exception e) {
            Log.e(TAG, "Error changing password", e);
            return false;
        } finally {
            DatabaseHelper.closeCursor(cursor);
        }
    }

    /**
     * Extract User object from cursor
     */
    private User extractUserFromCursor(Cursor cursor) {
        User user = new User();

        int idIndex = cursor.getColumnIndex(KEY_ID);
        int usernameIndex = cursor.getColumnIndex(KEY_USERNAME);
        int userTypeIndex = cursor.getColumnIndex(KEY_USER_TYPE);
        int emailIndex = cursor.getColumnIndex(KEY_EMAIL);
        int phoneIndex = cursor.getColumnIndex(KEY_PHONE);
        int statusIndex = cursor.getColumnIndex(KEY_STATUS);

        user.setId(cursor.getInt(idIndex));
        user.setUsername(cursor.getString(usernameIndex));
        user.setUserType(cursor.getString(userTypeIndex));
        user.setEmail(cursor.getString(emailIndex));
        user.setPhone(cursor.getString(phoneIndex));
        user.setStatus(cursor.getString(statusIndex));

        return user;
    }
}
