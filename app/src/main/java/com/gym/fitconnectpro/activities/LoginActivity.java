package com.gym.fitconnectpro.activities;



import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.activities.admin.AdminDashboardActivity;
import com.gym.fitconnectpro.dao.AuthDAO;
import com.gym.fitconnectpro.models.User;
import com.gym.fitconnectpro.services.Session;
import com.gym.fitconnectpro.utils.ValidationUtil;

/**
 * Login Activity for FitConnect Pro
 * Handles authentication for Admin, Trainer, and Member users
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private RadioGroup radioGroupUserType;
    private RadioButton rbAdmin;
    private RadioButton rbTrainer;
    private RadioButton rbMember;
    private Button btnLogin;
    private TextView tvForgotPassword;

    private AuthDAO authDAO;
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize DAO and Session
        authDAO = new AuthDAO(this);
        session = Session.getInstance(this);

        // Auto-login disabled for testing
        // if (session.isLoggedIn()) {
        //     navigateToDashboard(session.getUserType());
        //     return;
        // }

        // Initialize UI components
        initializeViews();

        // Set listeners
        setListeners();

        // Handle back press with new API
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitDialog();
            }
        });
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        radioGroupUserType = findViewById(R.id.radioGroupUserType);
        rbAdmin = findViewById(R.id.rbAdmin);
        rbTrainer = findViewById(R.id.rbTrainer);
        rbMember = findViewById(R.id.rbMember);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Set default selection to Admin
        rbAdmin.setChecked(true);
    }

    /**
     * Set click listeners
     */
    private void setListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleForgotPassword();
            }
        });
    }

    /**
     * Handle login button click
     */
    private void handleLogin() {
        // Get input values
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate inputs
        if (!validateInputs(username, password)) {
            return;
        }

        // Get selected user type
        String userType = getSelectedUserType();

        // Show progress (optional)
        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        // Authenticate based on user type
        new Thread(new Runnable() {
            @Override
            public void run() {
                authenticateUser(username, password, userType);
            }
        }).start();
    }

    /**
     * Validate input fields
     */
    private boolean validateInputs(String username, String password) {
        if (!ValidationUtil.isNotEmpty(username)) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return false;
        }

        if (!ValidationUtil.isNotEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }

        if (username.length() < 3) {
            etUsername.setError("Username must be at least 3 characters");
            etUsername.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Get selected user type from radio group
     */
    private String getSelectedUserType() {
        int selectedId = radioGroupUserType.getCheckedRadioButtonId();

        if (selectedId == R.id.rbAdmin) {
            return "ADMIN";
        } else if (selectedId == R.id.rbTrainer) {
            return "TRAINER";
        } else if (selectedId == R.id.rbMember) {
            return "MEMBER";
        }

        return "ADMIN"; // Default
    }

    /**
     * Authenticate user based on type
     */
    private void authenticateUser(String username, String password, String userType) {
        try {
            boolean success = false;

            // Only allow admin login
            if ("ADMIN".equals(userType)) {
                User admin = authDAO.authenticateAdmin(username, password);
                if (admin != null) {
                    session.setCurrentUser(admin);
                    success = true;
                }
            } else {
                // Show message that only admin can login
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("LOGIN");
                    Toast.makeText(this, "Only Admin can login at this time", Toast.LENGTH_LONG).show();
                });
                return;
            }

            final boolean loginSuccess = success;
            final String finalUserType = userType;

            // Update UI on main thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("LOGIN");

                    if (loginSuccess) {
                        onLoginSuccess(finalUserType);
                    } else {
                        onLoginFailure();
                    }
                }
            });

        } catch (Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("LOGIN");
                    showErrorDialog("Login Error", "An error occurred during login. Please try again.");
                }
            });
        }
    }

    /**
     * Handle successful login
     */
    private void onLoginSuccess(String userType) {
        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();

        // Navigate to appropriate dashboard
        navigateToDashboard(userType);
    }

    /**
     * Handle failed login
     */
    private void onLoginFailure() {
        showErrorDialog("Login Failed",
            "Invalid username or password.\n\n" +
            "Please check your credentials and try again.");

        // Clear password field
        etPassword.setText("");
        etPassword.requestFocus();
    }

    /**
     * Navigate to appropriate dashboard based on user type
     */
    private void navigateToDashboard(String userType) {
        // Only admin dashboard is available
        if ("ADMIN".equals(userType)) {
            Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
            finish(); // Prevent going back to login screen
        } else {
            Toast.makeText(this, "Dashboard not available for " + userType, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle forgot password click
     */
    private void handleForgotPassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Forgot Password");
        builder.setMessage("To reset your password, please contact the system administrator.\n\n" +
                "Admin Email: admin@fitconnectpro.com\n" +
                "Phone: 1234567890\n\n" +
                "For security reasons, password reset must be done by the administrator.");
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    /**
     * Show error dialog
     */
    private void showErrorDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    /**
     * Show exit confirmation dialog
     */
    private void showExitDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Exit Application")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Yes", (dialog, which) -> {
                finishAffinity();
            })
            .setNegativeButton("No", null)
            .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up resources if needed
    }
}

