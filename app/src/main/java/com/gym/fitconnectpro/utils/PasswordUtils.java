package com.gym.fitconnectpro.utils;

import android.util.Base64;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class PasswordUtils {

    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;

    /**
     * Hash a password with salt
     */
    public static String hashPassword(String password) {
        try {
            // Generate salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            // Hash password with salt
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());

            // Combine salt and hash
            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);

            return Base64.encodeToString(combined, Base64.NO_WRAP);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Verify a password against a hash
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        try {
            byte[] combined = Base64.decode(hashedPassword, Base64.NO_WRAP);

            // Extract salt
            byte[] salt = new byte[SALT_LENGTH];
            System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);

            // Hash the input password with the same salt
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            byte[] hashedInput = md.digest(password.getBytes());

            // Compare hashes
            if (hashedInput.length != combined.length - SALT_LENGTH) {
                return false;
            }

            for (int i = 0; i < hashedInput.length; i++) {
                if (hashedInput[i] != combined[SALT_LENGTH + i]) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

