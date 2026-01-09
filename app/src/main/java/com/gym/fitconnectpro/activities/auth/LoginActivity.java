package com.gym.fitconnectpro.activities.auth;

import android.content.Intent;
import android.os.Bundle;
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
import com.gym.fitconnectpro.models.Trainer;
import com.gym.fitconnectpro.models.Member;
import com.gym.fitconnectpro.services.Session;
import com.gym.fitconnectpro.utils.ValidationUtil;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private RadioGroup radioGroupUserType;
    private RadioButton rbAdmin, rbTrainer, rbMember;
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

        // Check if user is already logged in
        if (session.isLoggedIn()) {
            navigateToDashboard(session.getUserType());
            return;
        }

        // Bind Views
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        radioGroupUserType = findViewById(R.id.radioGroupUserType);
        rbAdmin = findViewById(R.id.rbAdmin);
        rbTrainer = findViewById(R.id.rbTrainer);
        rbMember = findViewById(R.id.rbMember);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Default selection
        rbAdmin.setChecked(true);

        btnLogin.setOnClickListener(v -> handleLogin());
        
        tvForgotPassword.setOnClickListener(v -> handleForgotPassword());

        // Handle back press with new API
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitDialog();
            }
        });
    }

    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        // Validate inputs
        if (!validateInputs(username, password)) {
            return;
        }

        // Get selected user type
        String userType = getSelectedUserType();

        // Show progress
        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        // Authenticate in background thread
        new Thread(() -> authenticateUser(username, password, userType)).start();
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

            boolean finalSuccess = success;
            String finalUserType = userType;

            // Update UI on main thread
            runOnUiThread(() -> {
                btnLogin.setEnabled(true);
                btnLogin.setText("LOGIN");

                if (finalSuccess) {
                    onLoginSuccess(finalUserType);
                } else {
                    onLoginFailure();
                }
            });

        } catch (Exception e) {
            runOnUiThread(() -> {
                btnLogin.setEnabled(true);
                btnLogin.setText("LOGIN");
                showErrorDialog("Login Error", "An error occurred during login. Please try again.");
            });
        }
    }

    /**
     * Handle successful login
     */
    private void onLoginSuccess(String userType) {
        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
        navigateToDashboard(userType);
    }

    /**
     * Handle failed login
     */
    private void onLoginFailure() {
        showErrorDialog("Login Failed",
            "Invalid username or password.\n\n" +
            "Please check your credentials and try again.");

        etPassword.setText("");
        etPassword.requestFocus();
    }

    /**
     * Navigate to appropriate dashboard
     */
    private void navigateToDashboard(String userType) {
        Intent intent;

        // Only admin dashboard is available
        if ("ADMIN".equals(userType)) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else {
            Toast.makeText(this, "Dashboard not available for " + userType, Toast.LENGTH_SHORT).show();
            return;
        }

        startActivity(intent);
        finish();
    }

    /**
     * Handle forgot password
     */
    private void handleForgotPassword() {
        new AlertDialog.Builder(this)
            .setTitle("Forgot Password")
            .setMessage("To reset your password, please contact the system administrator.\n\n" +
                    "Admin Email: admin@fitconnectpro.com\n" +
                    "Phone: 1234567890\n\n" +
                    "For security reasons, password reset must be done by the administrator.")
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton("OK", null)
            .show();
    }

    /**
     * Show error dialog
     */
    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("OK", null)
            .show();
    }

    /**
     * Show exit confirmation dialog
     */
    private void showExitDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Exit Application")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Yes", (dialog, which) -> finishAffinity())
            .setNegativeButton("No", null)
            .show();
    }
}
