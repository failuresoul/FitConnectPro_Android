package com.gym.fitconnectpro.utils;

import android.util.Log;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for password hashing and verification using BCrypt
 */
public class PasswordUtil {

    private static final String TAG = "PasswordUtil";
    private static final int BCRYPT_ROUNDS = 12;

    /**
     * Hash a plain text password using BCrypt
     * @param plainPassword The plain text password to hash
     * @return The BCrypt hashed password
     * @throws IllegalArgumentException if password is null or empty
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        try {
            return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
        } catch (Exception e) {
            Log.e(TAG, "Error hashing password", e);
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    /**
     * Verify a plain text password against a BCrypt hash
     * @param plainPassword The plain text password to verify
     * @param hashedPassword The BCrypt hashed password to verify against
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            Log.w(TAG, "Password or hash is null");
            return false;
        }

        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            Log.e(TAG, "Error verifying password", e);
            return false;
        }
    }

    /**
     * Check if a password needs to be rehashed (useful for security upgrades)
     * @param hashedPassword The hashed password to check
     * @return true if the password should be rehashed
     */
    public static boolean needsRehash(String hashedPassword) {
        if (hashedPassword == null) {
            return true;
        }

        try {
            // Check if the hash uses fewer rounds than current standard
            String[] parts = hashedPassword.split("\\$");
            if (parts.length < 4) {
                return true;
            }

            int rounds = Integer.parseInt(parts[2]);
            return rounds < BCRYPT_ROUNDS;
        } catch (Exception e) {
            Log.e(TAG, "Error checking hash", e);
            return true;
        }
    }
}

