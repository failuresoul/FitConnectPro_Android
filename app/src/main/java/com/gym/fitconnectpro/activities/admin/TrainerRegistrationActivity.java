package com.gym.fitconnectpro.activities.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.dao.TrainerDAO;
import com.gym.fitconnectpro.database.entities.Trainer;

import java.util.Random;

public class TrainerRegistrationActivity extends AppCompatActivity {

    private static final String TAG = "TrainerRegistration";

    private TextInputEditText etFullName, etEmail, etPhone;
    private TextInputEditText etSpecialization, etExperience, etCertification, etSalary;
    private TextView tvGeneratedUsername, tvGeneratedPassword;
    private Button btnRegisterTrainer, btnClearForm;

    private TrainerDAO trainerDAO;
    private String generatedUsername, generatedPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_registration);

        initializeViews();
        setupToolbar();
        setupButtons();

        trainerDAO = new TrainerDAO(this);
    }

    private void initializeViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etSpecialization = findViewById(R.id.etSpecialization);
        etExperience = findViewById(R.id.etExperience);
        etCertification = findViewById(R.id.etCertification);
        etSalary = findViewById(R.id.etSalary);
        tvGeneratedUsername = findViewById(R.id.tvGeneratedUsername);
        tvGeneratedPassword = findViewById(R.id.tvGeneratedPassword);
        btnRegisterTrainer = findViewById(R.id.btnRegisterTrainer);
        btnClearForm = findViewById(R.id.btnClearForm);

        Log.d(TAG, "All views initialized");
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.trainer_registration);
        }
    }

    private void setupButtons() {
        btnRegisterTrainer.setOnClickListener(v -> handleRegisterTrainer());
        btnClearForm.setOnClickListener(v -> clearForm());
    }

    private boolean validateInputs() {
        if (TextUtils.isEmpty(etFullName.getText())) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(etEmail.getText())) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(etPhone.getText())) {
            etPhone.setError("Phone is required");
            etPhone.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(etSpecialization.getText())) {
            etSpecialization.setError("Specialization is required");
            etSpecialization.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(etExperience.getText())) {
            etExperience.setError("Experience is required");
            etExperience.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(etCertification.getText())) {
            etCertification.setError("Certification is required");
            etCertification.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(etSalary.getText())) {
            etSalary.setError("Salary is required");
            etSalary.requestFocus();
            return false;
        }

        return true;
    }

    private String generateUsername() {
        String fullName = etFullName.getText().toString().trim();
        String[] nameParts = fullName.toLowerCase().split(" ");

        String baseUsername;
        if (nameParts.length >= 2) {
            baseUsername = nameParts[0] + "." + nameParts[nameParts.length - 1];
        } else {
            baseUsername = nameParts[0];
        }

        generatedUsername = baseUsername;

        int counter = 1;
        while (trainerDAO.isUsernameExists(generatedUsername)) {
            generatedUsername = baseUsername + counter;
            counter++;
        }

        Log.d(TAG, "Generated username: " + generatedUsername);
        return generatedUsername;
    }

    private String generatePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        generatedPassword = password.toString();
        Log.d(TAG, "Generated password");
        return generatedPassword;
    }

    private void handleRegisterTrainer() {
        if (!validateInputs()) {
            return;
        }

        Log.d(TAG, "Starting trainer registration...");

        generateUsername();
        generatePassword();

        tvGeneratedUsername.setText(generatedUsername);
        tvGeneratedPassword.setText(generatedPassword);

        Trainer trainer = new Trainer();
        trainer.setFullName(etFullName.getText().toString().trim());
        trainer.setEmail(etEmail.getText().toString().trim());
        trainer.setPhone(etPhone.getText().toString().trim());
        trainer.setEducation("N/A");
        trainer.setCertifications(etCertification.getText().toString().trim());
        trainer.setExperienceYears(Integer.parseInt(etExperience.getText().toString().trim()));
        trainer.setSpecializations(etSpecialization.getText().toString().trim());
        trainer.setMonthlySalary(Double.parseDouble(etSalary.getText().toString().trim()));
        trainer.setMaxClients(10);
        trainer.setAccountStatus("ACTIVE");
        trainer.setUsername(generatedUsername);
        trainer.setPasswordHash(generatedPassword);

        boolean success = trainerDAO.registerTrainer(trainer);

        if (success) {
            Log.d(TAG, "Trainer registered successfully");
            showSuccessDialog();
        } else {
            Log.e(TAG, "Failed to register trainer");
            Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Registration Successful")
                .setMessage("Trainer registered successfully!\n\n" +
                        "Username: " + generatedUsername + "\n" +
                        "Password: " + generatedPassword + "\n\n" +
                        "Please save these credentials.")
                .setPositiveButton("OK", (dialog, which) -> {
                    clearForm();
                    finish();
                })
                .setNegativeButton("Add Another", (dialog, which) -> clearForm())
                .setCancelable(false)
                .show();
    }

    private void clearForm() {
        etFullName.setText("");
        etEmail.setText("");
        etPhone.setText("");
        etSpecialization.setText("");
        etExperience.setText("");
        etCertification.setText("");
        etSalary.setText("");
        tvGeneratedUsername.setText(R.string.username_placeholder);
        tvGeneratedPassword.setText(R.string.password_placeholder);

        generatedUsername = null;
        generatedPassword = null;

        Log.d(TAG, "Form cleared");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
