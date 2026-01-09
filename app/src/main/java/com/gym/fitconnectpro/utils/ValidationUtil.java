package com.gym.fitconnectpro.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.regex.Pattern;

/**
 * Utility class for input validation and alert dialogs
 */
public class ValidationUtil {

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    // Phone validation pattern (Indian format, can be modified)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[6-9]\\d{9}$"
    );

    // Password validation pattern (min 6 chars, at least one letter and one number)
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{6,}$"
    );

    /**
     * Validate email format
     * @param email Email address to validate
     * @return true if email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (isNotEmpty(email)) {
            return EMAIL_PATTERN.matcher(email.trim()).matches();
        }
        return false;
    }

    /**
     * Validate phone number format
     * @param phone Phone number to validate
     * @return true if phone is valid, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        if (isNotEmpty(phone)) {
            String cleanPhone = phone.replaceAll("[\\s-]", "");
            return PHONE_PATTERN.matcher(cleanPhone).matches();
        }
        return false;
    }

    /**
     * Validate password format
     * Minimum 6 characters, at least one letter and one number
     * @param password Password to validate
     * @return true if password is valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        if (isNotEmpty(password)) {
            return PASSWORD_PATTERN.matcher(password).matches();
        }
        return false;
    }

    /**
     * Check if text is not null or empty
     * @param text Text to check
     * @return true if text is not null and not empty, false otherwise
     */
    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    /**
     * Validate username format
     * @param username Username to validate
     * @return true if username is valid (3-20 chars, alphanumeric and underscore)
     */
    public static boolean isValidUsername(String username) {
        if (isNotEmpty(username)) {
            return username.matches("^[a-zA-Z0-9_]{3,20}$");
        }
        return false;
    }

    /**
     * Validate numeric input
     * @param text Text to validate
     * @return true if text is a valid number
     */
    public static boolean isNumeric(String text) {
        if (isNotEmpty(text)) {
            try {
                Double.parseDouble(text);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * Validate positive number
     * @param text Text to validate
     * @return true if text is a valid positive number
     */
    public static boolean isPositiveNumber(String text) {
        if (isNumeric(text)) {
            return Double.parseDouble(text) > 0;
        }
        return false;
    }

    /**
     * Alert types for different message scenarios
     */
    public enum AlertType {
        SUCCESS,
        ERROR,
        WARNING,
        INFO
    }

    /**
     * Show a toast message
     * @param context Application context
     * @param message Message to display
     * @param length Toast.LENGTH_SHORT or Toast.LENGTH_LONG
     */
    public static void showToast(Context context, String message, int length) {
        if (context != null && isNotEmpty(message)) {
            Toast.makeText(context, message, length).show();
        }
    }

    /**
     * Show a simple toast message (short duration)
     * @param context Application context
     * @param message Message to display
     */
    public static void showToast(Context context, String message) {
        showToast(context, message, Toast.LENGTH_SHORT);
    }

    /**
     * Show an alert dialog
     * @param context Activity context
     * @param title Dialog title
     * @param message Dialog message
     * @param type Alert type
     */
    public static void showAlert(Context context, String title, String message, AlertType type) {
        if (context == null || !(context instanceof Activity)) {
            return;
        }

        Activity activity = (Activity) context;
        if (activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);

        // Set icon based on alert type
        switch (type) {
            case SUCCESS:
                builder.setIcon(android.R.drawable.ic_dialog_info);
                break;
            case ERROR:
                builder.setIcon(android.R.drawable.ic_dialog_alert);
                break;
            case WARNING:
                builder.setIcon(android.R.drawable.ic_dialog_alert);
                break;
            case INFO:
                builder.setIcon(android.R.drawable.ic_dialog_info);
                break;
        }

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    /**
     * Show a confirmation dialog with callback
     * @param context Activity context
     * @param title Dialog title
     * @param message Dialog message
     * @param listener Callback listener
     */
    public static void showConfirmDialog(Context context, String title, String message,
                                        final ConfirmDialogListener listener) {
        if (context == null || !(context instanceof Activity)) {
            return;
        }

        Activity activity = (Activity) context;
        if (activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onConfirm();
                }
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onCancel();
                }
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    /**
     * Interface for confirmation dialog callbacks
     */
    public interface ConfirmDialogListener {
        void onConfirm();
        void onCancel();
    }
}
