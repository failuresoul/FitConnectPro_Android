package com.gym.fitconnectpro.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.gym.fitconnectpro.database.DatabaseHelper;
import com.gym.fitconnectpro.models.User;
import com.gym.fitconnectpro.models.Member;
import com.gym.fitconnectpro.database.entities.Trainer;
import com.gym.fitconnectpro.utils.PasswordUtil;
import com.gym.fitconnectpro.utils.DateUtil;

import java.util.Date;

/**
 * Data Access Object for authentication operations
 * Handles login and password management for all user types
 */
public class AuthDAO {

    private static final String TAG = "AuthDAO";
    private DatabaseHelper dbHelper;

    // Table names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_MEMBERS = "members";
    private static final String TABLE_TRAINERS = "trainers";
    private static final String TABLE_MEMBERSHIPS = "memberships";

    // Common column names
    private static final String KEY_ID = "id";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_USER_TYPE = "user_type";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_STATUS = "status";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_UPDATED_AT = "updated_at";

    // Membership columns
    private static final String KEY_MEMBER_ID = "member_id";
    private static final String KEY_END_DATE = "end_date";

    public AuthDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Authenticate admin user
     * @param username Admin username
     * @param password Admin password (plain text)
     * @return User object if authentication successful, null otherwise
     */
    public User authenticateAdmin(String username, String password) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();

            // Query admin from users table
            String selection = KEY_USERNAME + " = ? AND " + KEY_USER_TYPE + " = ? AND " + KEY_STATUS + " = ?";
            String[] selectionArgs = {username, "ADMIN", "ACTIVE"};

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

                // Verify password using BCrypt
                if (PasswordUtil.verifyPassword(password, hashedPassword)) {
                    User admin = extractUserFromCursor(cursor);

                    // Update last login timestamp
                    updateLastLogin(db, admin.getId(), TABLE_USERS);

                    Log.d(TAG, "Admin authenticated successfully: " + username);
                    return admin;
                }
            }

            Log.w(TAG, "Admin authentication failed for username: " + username);
            return null;

        } catch (Exception e) {
            Log.e(TAG, "Error authenticating admin", e);
            return null;
        } finally {
            DatabaseHelper.closeCursor(cursor);
        }
    }

    /**
     * Authenticate trainer user
     * @param username Trainer username
     * @param password Trainer password (plain text)
     * @return Trainer object if authentication successful, null otherwise
     */
    public Trainer authenticateTrainer(String username, String password) {
        SQLiteDatabase db = null;
        Cursor userCursor = null;
        Cursor trainerCursor = null;

        try {
            db = dbHelper.getReadableDatabase();

            // First, query user from users table
            String userSelection = KEY_USERNAME + " = ? AND " + KEY_USER_TYPE + " = ? AND " + KEY_STATUS + " = ?";
            String[] userSelectionArgs = {username, "TRAINER", "ACTIVE"};

            userCursor = db.query(
                TABLE_USERS,
                null,
                userSelection,
                userSelectionArgs,
                null,
                null,
                null
            );

            if (userCursor != null && userCursor.moveToFirst()) {
                int passwordIndex = userCursor.getColumnIndex(KEY_PASSWORD);
                int userIdIndex = userCursor.getColumnIndex(KEY_ID);

                String hashedPassword = userCursor.getString(passwordIndex);
                int userId = userCursor.getInt(userIdIndex);

                // Verify password using BCrypt
                if (PasswordUtil.verifyPassword(password, hashedPassword)) {

                    // Query trainer details from trainers table
                    String trainerSelection = KEY_USER_ID + " = ? AND " + KEY_STATUS + " = ?";
                    String[] trainerSelectionArgs = {String.valueOf(userId), "ACTIVE"};

                    trainerCursor = db.query(
                        TABLE_TRAINERS,
                        null,
                        trainerSelection,
                        trainerSelectionArgs,
                        null,
                        null,
                        null
                    );

                    if (trainerCursor != null && trainerCursor.moveToFirst()) {
                        Trainer trainer = extractTrainerFromCursors(userCursor, trainerCursor);

                        // Update last login timestamp
                        updateLastLogin(db, trainer.getId(), TABLE_TRAINERS);

                        Log.d(TAG, "Trainer authenticated successfully: " + username);
                        return trainer;
                    }
                }
            }

            Log.w(TAG, "Trainer authentication failed for username: " + username);
            return null;

        } catch (Exception e) {
            Log.e(TAG, "Error authenticating trainer", e);
            return null;
        } finally {
            DatabaseHelper.closeCursor(userCursor);
            DatabaseHelper.closeCursor(trainerCursor);
        }
    }

    /**
     * Authenticate member user
     * @param username Member username
     * @param password Member password (plain text)
     * @return Member object if authentication successful, null otherwise
     */
    public Member authenticateMember(String username, String password) {
        SQLiteDatabase db = null;
        Cursor userCursor = null;
        Cursor memberCursor = null;
        Cursor membershipCursor = null;

        try {
            db = dbHelper.getReadableDatabase();

            // First, query user from users table
            String userSelection = KEY_USERNAME + " = ? AND " + KEY_USER_TYPE + " = ? AND " + KEY_STATUS + " = ?";
            String[] userSelectionArgs = {username, "MEMBER", "ACTIVE"};

            userCursor = db.query(
                TABLE_USERS,
                null,
                userSelection,
                userSelectionArgs,
                null,
                null,
                null
            );

            if (userCursor != null && userCursor.moveToFirst()) {
                int passwordIndex = userCursor.getColumnIndex(KEY_PASSWORD);
                int userIdIndex = userCursor.getColumnIndex(KEY_ID);

                String hashedPassword = userCursor.getString(passwordIndex);
                int userId = userCursor.getInt(userIdIndex);

                // Verify password using BCrypt
                if (PasswordUtil.verifyPassword(password, hashedPassword)) {

                    // Query member details from members table
                    String memberSelection = KEY_USER_ID + " = ? AND " + KEY_STATUS + " = ?";
                    String[] memberSelectionArgs = {String.valueOf(userId), "ACTIVE"};

                    memberCursor = db.query(
                        TABLE_MEMBERS,
                        null,
                        memberSelection,
                        memberSelectionArgs,
                        null,
                        null,
                        null
                    );

                    if (memberCursor != null && memberCursor.moveToFirst()) {
                        int memberIdIndex = memberCursor.getColumnIndex(KEY_ID);
                        int memberId = memberCursor.getInt(memberIdIndex);

                        // Check if membership is valid (not expired)
                        if (isMembershipValid(db, memberId)) {
                            Member member = extractMemberFromCursors(userCursor, memberCursor);

                            // Update last login timestamp
                            updateLastLogin(db, member.getId(), TABLE_MEMBERS);

                            Log.d(TAG, "Member authenticated successfully: " + username);
                            return member;
                        } else {
                            Log.w(TAG, "Member membership has expired: " + username);
                        }
                    }
                }
            }

            Log.w(TAG, "Member authentication failed for username: " + username);
            return null;

        } catch (Exception e) {
            Log.e(TAG, "Error authenticating member", e);
            return null;
        } finally {
            DatabaseHelper.closeCursor(userCursor);
            DatabaseHelper.closeCursor(memberCursor);
            DatabaseHelper.closeCursor(membershipCursor);
        }
    }

    /**
     * Change password for any user type
     * @param userId User ID
     * @param userType User type (ADMIN, TRAINER, MEMBER)
     * @param newPassword New password (plain text)
     * @return true if password changed successfully, false otherwise
     */
    public boolean changePassword(int userId, String userType, String newPassword) {
        SQLiteDatabase db = null;

        try {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();

            // Hash new password using BCrypt
            String hashedPassword = PasswordUtil.hashPassword(newPassword);

            // Update password in users table
            ContentValues userValues = new ContentValues();
            userValues.put(KEY_PASSWORD, hashedPassword);
            userValues.put(KEY_UPDATED_AT, DateUtil.formatSqlDateTime(new Date()));

            // Find user by user_id in the respective table
            int userPrimaryId = getUserPrimaryId(db, userId, userType);

            if (userPrimaryId == -1) {
                Log.e(TAG, "User not found for password change");
                return false;
            }

            String whereClause = KEY_ID + " = ?";
            String[] whereArgs = {String.valueOf(userPrimaryId)};

            int rowsAffected = db.update(TABLE_USERS, userValues, whereClause, whereArgs);

            if (rowsAffected > 0) {
                db.setTransactionSuccessful();
                Log.d(TAG, "Password changed successfully for user ID: " + userId + ", type: " + userType);
                return true;
            }

            return false;

        } catch (Exception e) {
            Log.e(TAG, "Error changing password", e);
            return false;
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
    }

    /**
     * Update last login timestamp
     * @param db Database instance
     * @param recordId Record ID in the specific table
     * @param tableName Table name (users, trainers, or members)
     */
    private void updateLastLogin(SQLiteDatabase db, int recordId, String tableName) {
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_UPDATED_AT, DateUtil.formatSqlDateTime(new Date()));

            String whereClause = KEY_ID + " = ?";
            String[] whereArgs = {String.valueOf(recordId)};

            db.update(tableName, values, whereClause, whereArgs);

        } catch (Exception e) {
            Log.e(TAG, "Error updating last login", e);
        }
    }

    /**
     * Check if member's membership is valid (not expired)
     * @param db Database instance
     * @param memberId Member ID
     * @return true if membership is valid, false otherwise
     */
    private boolean isMembershipValid(SQLiteDatabase db, int memberId) {
        Cursor cursor = null;

        try {
            String currentDate = DateUtil.formatSqlDate(new Date());

            String selection = KEY_MEMBER_ID + " = ? AND " + KEY_STATUS + " = ? AND " + KEY_END_DATE + " >= ?";
            String[] selectionArgs = {String.valueOf(memberId), "ACTIVE", currentDate};

            cursor = db.query(
                TABLE_MEMBERSHIPS,
                new String[]{KEY_ID},
                selection,
                selectionArgs,
                null,
                null,
                null
            );

            return cursor != null && cursor.getCount() > 0;

        } catch (Exception e) {
            Log.e(TAG, "Error checking membership validity", e);
            return false;
        } finally {
            DatabaseHelper.closeCursor(cursor);
        }
    }

    /**
     * Get user primary ID from users table based on record ID and user type
     * @param db Database instance
     * @param recordId Record ID in specific table (members, trainers)
     * @param userType User type
     * @return User primary ID or -1 if not found
     */
    private int getUserPrimaryId(SQLiteDatabase db, int recordId, String userType) {
        Cursor cursor = null;

        try {
            if ("ADMIN".equals(userType)) {
                return recordId; // For admin, recordId is the user ID
            }

            String tableName = "TRAINER".equals(userType) ? TABLE_TRAINERS : TABLE_MEMBERS;

            String selection = KEY_ID + " = ?";
            String[] selectionArgs = {String.valueOf(recordId)};

            cursor = db.query(
                tableName,
                new String[]{KEY_USER_ID},
                selection,
                selectionArgs,
                null,
                null,
                null
            );

            if (cursor != null && cursor.moveToFirst()) {
                int userIdIndex = cursor.getColumnIndex(KEY_USER_ID);
                return cursor.getInt(userIdIndex);
            }

            return -1;

        } catch (Exception e) {
            Log.e(TAG, "Error getting user primary ID", e);
            return -1;
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

    /**
     * Extract Trainer object from user and trainer cursors
     */
    private Trainer extractTrainerFromCursors(Cursor userCursor, Cursor trainerCursor) {
        Trainer trainer = new Trainer();

        // Extract from user cursor
        int userIdIndex = userCursor.getColumnIndex(KEY_ID);
        int usernameIndex = userCursor.getColumnIndex(KEY_USERNAME);
        int emailIndex = userCursor.getColumnIndex(KEY_EMAIL);
        int phoneIndex = userCursor.getColumnIndex(KEY_PHONE);

        trainer.setUserId(userCursor.getInt(userIdIndex));
        trainer.setUsername(userCursor.getString(usernameIndex));
        trainer.setEmail(userCursor.getString(emailIndex));
        trainer.setPhone(userCursor.getString(phoneIndex));

        // Extract from trainer cursor
        int idIndex = trainerCursor.getColumnIndex(KEY_ID);
        int fullNameIndex = trainerCursor.getColumnIndex(KEY_FULL_NAME);
        int statusIndex = trainerCursor.getColumnIndex(KEY_STATUS);

        trainer.setId(trainerCursor.getInt(idIndex));
        trainer.setFullName(trainerCursor.getString(fullNameIndex));
        trainer.setStatus(trainerCursor.getString(statusIndex));

        return trainer;
    }

    /**
     * Extract Member object from user and member cursors
     */
    private Member extractMemberFromCursors(Cursor userCursor, Cursor memberCursor) {
        Member member = new Member();

        // Extract from user cursor
        int userIdIndex = userCursor.getColumnIndex(KEY_ID);
        int usernameIndex = userCursor.getColumnIndex(KEY_USERNAME);
        int emailIndex = userCursor.getColumnIndex(KEY_EMAIL);
        int phoneIndex = userCursor.getColumnIndex(KEY_PHONE);

        member.setUserId(userCursor.getInt(userIdIndex));
        member.setUsername(userCursor.getString(usernameIndex));
        member.setEmail(userCursor.getString(emailIndex));
        member.setPhone(userCursor.getString(phoneIndex));

        // Extract from member cursor
        int idIndex = memberCursor.getColumnIndex(KEY_ID);
        int fullNameIndex = memberCursor.getColumnIndex(KEY_FULL_NAME);
        int statusIndex = memberCursor.getColumnIndex(KEY_STATUS);

        member.setId(memberCursor.getInt(idIndex));
        member.setFullName(memberCursor.getString(fullNameIndex));
        member.setStatus(memberCursor.getString(statusIndex));

        return member;
    }
}

