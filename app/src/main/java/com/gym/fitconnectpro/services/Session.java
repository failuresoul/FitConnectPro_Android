package com.gym.fitconnectpro.services;

import android.content.Context;
import android.content.SharedPreferences;

import com.gym.fitconnectpro.models.User;
import com.gym.fitconnectpro.database.entities.Trainer;
import com.gym.fitconnectpro.models.Member;

/**
 * Singleton Session class to manage user authentication state
 * Stores current user information and provides session management
 */
public class Session {

    private static Session instance;
    private static final String PREF_NAME = "FitConnectProSession";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_USER_TYPE = "user_type";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private User currentUser;
    private Trainer currentTrainer;
    private Member currentMember;
    private String userType;

    // Private constructor for Singleton
    private Session(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    /**
     * Get singleton instance
     */
    public static synchronized Session getInstance(Context context) {
        if (instance == null) {
            instance = new Session(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Set current admin user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        this.userType = "ADMIN";

        editor.putInt(KEY_USER_ID, user.getId());
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.putString(KEY_USER_TYPE, "ADMIN");
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_PHONE, user.getPhone());
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    /**
     * Set current trainer user
     */
    public void setCurrentTrainer(Trainer trainer) {
        this.currentTrainer = trainer;
        this.userType = "TRAINER";

        editor.putInt(KEY_USER_ID, trainer.getId());
        editor.putString(KEY_USERNAME, trainer.getUsername());
        editor.putString(KEY_USER_TYPE, "TRAINER");
        editor.putString(KEY_EMAIL, trainer.getEmail());
        editor.putString(KEY_PHONE, trainer.getPhone());
        editor.putString(KEY_FULL_NAME, trainer.getFullName());
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    /**
     * Set current member user
     */
    public void setCurrentMember(Member member) {
        this.currentMember = member;
        this.userType = "MEMBER";

        editor.putInt(KEY_USER_ID, member.getId());
        editor.putString(KEY_USERNAME, member.getUsername());
        editor.putString(KEY_USER_TYPE, "MEMBER");
        editor.putString(KEY_EMAIL, member.getEmail());
        editor.putString(KEY_PHONE, member.getPhone());
        editor.putString(KEY_FULL_NAME, member.getFullName());
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    /**
     * Get current admin user
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Get current trainer
     */
    public Trainer getCurrentTrainer() {
        return currentTrainer;
    }

    /**
     * Get current member
     */
    public Member getCurrentMember() {
        return currentMember;
    }

    /**
     * Get user type (ADMIN, TRAINER, MEMBER)
     */
    public String getUserType() {
        if (userType == null) {
            userType = sharedPreferences.getString(KEY_USER_TYPE, null);
        }
        return userType;
    }

    /**
     * Get user ID
     */
    public int getUserId() {
        return sharedPreferences.getInt(KEY_USER_ID, -1);
    }

    /**
     * Get username
     */
    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, null);
    }

    /**
     * Get email
     */
    public String getEmail() {
        return sharedPreferences.getString(KEY_EMAIL, null);
    }

    /**
     * Get phone
     */
    public String getPhone() {
        return sharedPreferences.getString(KEY_PHONE, null);
    }

    /**
     * Get full name
     */
    public String getFullName() {
        return sharedPreferences.getString(KEY_FULL_NAME, null);
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Logout current user
     */
    public void logout() {
        currentUser = null;
        currentTrainer = null;
        currentMember = null;
        userType = null;

        editor.clear();
        editor.apply();
    }

    /**
     * Clear session (same as logout)
     */
    public void clearSession() {
        logout();
    }
}

