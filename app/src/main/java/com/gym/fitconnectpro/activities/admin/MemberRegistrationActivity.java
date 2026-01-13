package com.gym.fitconnectpro.activities.admin;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.dao.MemberDAO;
import com.gym.fitconnectpro.database.entities.Member;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MemberRegistrationActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPhone, etHeight, etWeight, etMedicalNotes, etEmergencyContact;
    private Button btnDateOfBirth, btnMembershipStartDate, btnRegisterMember, btnClearForm;
    private Spinner spinnerGender, spinnerMembershipType, spinnerDuration;
    private TextView tvGeneratedUsername, tvGeneratedPassword;

    private Calendar dateOfBirth, membershipStartDate;
    private String generatedUsername = "";
    private String generatedPassword = "";

    private MemberDAO memberDAO;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_registration);

        // Setup toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        Toast.makeText(this, "Launching Member Registration...", Toast.LENGTH_SHORT).show();

        initializeViews();
        setupSpinners();
        setupDatePickers();
        setupListeners();

        memberDAO = new MemberDAO(this);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.member_registration);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initializeViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        etMedicalNotes = findViewById(R.id.etMedicalNotes);
        etEmergencyContact = findViewById(R.id.etEmergencyContact);

        btnDateOfBirth = findViewById(R.id.btnDateOfBirth);
        btnMembershipStartDate = findViewById(R.id.btnMembershipStartDate);
        btnRegisterMember = findViewById(R.id.btnRegisterMember);
        btnClearForm = findViewById(R.id.btnClearForm);

        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerMembershipType = findViewById(R.id.spinnerMembershipType);
        spinnerDuration = findViewById(R.id.spinnerDuration);

        tvGeneratedUsername = findViewById(R.id.tvGeneratedUsername);
        tvGeneratedPassword = findViewById(R.id.tvGeneratedPassword);

        dateOfBirth = Calendar.getInstance();
        membershipStartDate = Calendar.getInstance();
    }

    private void setupSpinners() {
        // Gender Spinner
        String[] genders = {"Male", "Female", "Other"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, genders);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        // Membership Type Spinner
        String[] membershipTypes = {"Basic – 1000/month", "Premium – 2000/month", "Elite – 3000/month"};
        ArrayAdapter<String> membershipAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, membershipTypes);
        membershipAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMembershipType.setAdapter(membershipAdapter);

        // Duration Spinner
        String[] durations = new String[24]; // 1-24 months
        for (int i = 0; i < durations.length; i++) {
            durations[i] = (i + 1) + " " + (i == 0 ? "Month" : "Months");
        }
        ArrayAdapter<String> durationAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, durations);
        durationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDuration.setAdapter(durationAdapter);
    }

    private void setupDatePickers() {
        btnDateOfBirth.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        dateOfBirth.set(year, month, dayOfMonth);
                        btnDateOfBirth.setText(dateFormat.format(dateOfBirth.getTime()));
                    },
                    dateOfBirth.get(Calendar.YEAR),
                    dateOfBirth.get(Calendar.MONTH),
                    dateOfBirth.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        btnMembershipStartDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        membershipStartDate.set(year, month, dayOfMonth);
                        btnMembershipStartDate.setText(dateFormat.format(membershipStartDate.getTime()));
                    },
                    membershipStartDate.get(Calendar.YEAR),
                    membershipStartDate.get(Calendar.MONTH),
                    membershipStartDate.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });
    }

    private void setupListeners() {
        // Auto-generate username when full name changes
        etFullName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                generateUsername(s.toString());
            }
        });

        btnRegisterMember.setOnClickListener(v -> registerMember());
        btnClearForm.setOnClickListener(v -> clearForm());
        
        Button btnBackToDashboard = findViewById(R.id.btnBackToDashboard);
        btnBackToDashboard.setOnClickListener(v -> {
            finish(); // Go back to Dashboard
        });
    }

    private void generateUsername(String fullName) {
        if (fullName.trim().isEmpty()) {
            generatedUsername = "";
            tvGeneratedUsername.setText(R.string.username_placeholder);
            return;
        }

        String[] names = fullName.trim().toLowerCase().split("\\s+");
        String baseUsername;

        if (names.length >= 2) {
            baseUsername = names[0] + "." + names[names.length - 1];
        } else {
            baseUsername = names[0];
        }

        // Remove special characters
        baseUsername = baseUsername.replaceAll("[^a-z0-9.]", "");

        // Add random 4-digit number to ensure uniqueness
        int randomNum = (int) (Math.random() * 9000) + 1000; // 1000-9999
        generatedUsername = baseUsername + randomNum;

        tvGeneratedUsername.setText(generatedUsername);
    }

    private String generatePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(8);

        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

    private boolean validateInput() {
        if (etFullName.getText().toString().trim().isEmpty()) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return false;
        }

        if (etEmail.getText().toString().trim().isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString()).matches()) {
            etEmail.setError("Invalid email format");
            etEmail.requestFocus();
            return false;
        }

        if (etPhone.getText().toString().trim().isEmpty()) {
            etPhone.setError("Phone number is required");
            etPhone.requestFocus();
            return false;
        }

        if (btnDateOfBirth.getText().toString().equals(getString(R.string.select_date))) {
            Toast.makeText(this, "Please select date of birth", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (etHeight.getText().toString().trim().isEmpty()) {
            etHeight.setError("Height is required");
            etHeight.requestFocus();
            return false;
        }

        if (etWeight.getText().toString().trim().isEmpty()) {
            etWeight.setError("Weight is required");
            etWeight.requestFocus();
            return false;
        }

        if (btnMembershipStartDate.getText().toString().equals(getString(R.string.select_date))) {
            Toast.makeText(this, "Please select membership start date", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (etEmergencyContact.getText().toString().trim().isEmpty()) {
            etEmergencyContact.setError("Emergency contact is required");
            etEmergencyContact.requestFocus();
            return false;
        }

        return true;
    }

    private void registerMember() {
        if (!validateInput()) {
            return;
        }

        // Regenerate username and password to ensure uniqueness
        generateUsername(etFullName.getText().toString());
        generatedPassword = generatePassword();
        tvGeneratedPassword.setText(generatedPassword);

        // Calculate membership end date
        Calendar endDate = (Calendar) membershipStartDate.clone();
        int duration = spinnerDuration.getSelectedItemPosition() + 1;
        endDate.add(Calendar.MONTH, duration);

        // Get membership type
        String membershipTypeSelected = spinnerMembershipType.getSelectedItem().toString();
        String membershipType = membershipTypeSelected.split("–")[0].trim();

        double membershipFee = 0;
        switch (membershipType) {
            case "Basic":
                membershipFee = 1000;
                break;
            case "Premium":
                membershipFee = 2000;
                break;
            case "Elite":
                membershipFee = 3000;
                break;
        }

        // Create Member object
        Member member = new Member();
        member.setFullName(etFullName.getText().toString().trim());
        member.setEmail(etEmail.getText().toString().trim());
        member.setPhone(etPhone.getText().toString().trim());
        member.setDateOfBirth(dateFormat.format(dateOfBirth.getTime()));
        member.setGender(spinnerGender.getSelectedItem().toString());
        member.setHeight(Double.parseDouble(etHeight.getText().toString().trim()));
        member.setWeight(Double.parseDouble(etWeight.getText().toString().trim()));
        member.setMembershipType(membershipType);
        member.setMembershipFee(membershipFee);
        member.setMembershipStartDate(dateFormat.format(membershipStartDate.getTime()));
        member.setMembershipEndDate(dateFormat.format(endDate.getTime()));
        member.setMedicalNotes(etMedicalNotes.getText().toString().trim());
        member.setEmergencyContact(etEmergencyContact.getText().toString().trim());
        member.setUsername(generatedUsername);
        member.setPassword(generatedPassword);
        member.setStatus("Active");

        // Register member
        String result = memberDAO.registerMember(member);

        if ("SUCCESS".equals(result)) {
            showSuccessDialog();
        } else {
            Toast.makeText(this, "Registration Failed: " + result, Toast.LENGTH_LONG).show();
            // Also log to console for debugging
            System.out.println("Registration Error: " + result);
        }
    }

    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Registration Successful");
        builder.setMessage("Member registered successfully!\n\n" +
                "Username: " + generatedUsername + "\n" +
                "Password: " + generatedPassword + "\n\n" +
                "Please save these credentials.");
        builder.setPositiveButton("OK", (dialog, which) -> {
            clearForm();
            finish();
        });
        builder.setNeutralButton("Send Email (Mock)", (dialog, which) -> {
            Toast.makeText(this, "Email sent to " + etEmail.getText().toString(), Toast.LENGTH_SHORT).show();
            clearForm();
            finish();
        });
        builder.show();
    }

    private void clearForm() {
        etFullName.setText("");
        etEmail.setText("");
        etPhone.setText("");
        etHeight.setText("");
        etWeight.setText("");
        etMedicalNotes.setText("");
        etEmergencyContact.setText("");
        btnDateOfBirth.setText(R.string.select_date);
        btnMembershipStartDate.setText(R.string.select_date);
        spinnerGender.setSelection(0);
        spinnerMembershipType.setSelection(0);
        spinnerDuration.setSelection(0);
        tvGeneratedUsername.setText(R.string.username_placeholder);
        tvGeneratedPassword.setText(R.string.password_placeholder);
        generatedUsername = "";
        generatedPassword = "";
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
