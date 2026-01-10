package com.gym.fitconnectpro.activities.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.adapters.SalaryAdapter;
import com.gym.fitconnectpro.dao.SalaryDAO;
import com.gym.fitconnectpro.database.entities.Salary;

import androidx.appcompat.widget.Toolbar;
import com.gym.fitconnectpro.services.Session;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SalaryManagementActivity extends AppCompatActivity implements SalaryAdapter.OnSalaryActionListener {

    private Spinner spinnerMonth, spinnerYear;
    private Button btnGenerate;
    private RecyclerView rvSalaries;
    private TextView tvEmptyState, tvTotalPending, tvTotalPaid;
    private Toolbar toolbar;
    
    private SalaryDAO salaryDAO;
    private SalaryAdapter adapter;
    private List<Salary> salaryList;
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salary_management);

        session = Session.getInstance(this);
        salaryDAO = new SalaryDAO(this);

        initViews();
        setupToolbar();
        setupSpinners();
        
        btnGenerate.setOnClickListener(v -> generateSalaries()); // Generate new records
        
        // Initial load (current month default)
        Calendar cal = Calendar.getInstance();
        spinnerMonth.setSelection(cal.get(Calendar.MONTH)); // 0-indexed
        
        loadSalaries(); // Load for default selection
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerYear = findViewById(R.id.spinnerYear);
        btnGenerate = findViewById(R.id.btnGenerate);
        rvSalaries = findViewById(R.id.rvSalaries);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvTotalPending = findViewById(R.id.tvTotalPending);
        tvTotalPaid = findViewById(R.id.tvTotalPaid);

        rvSalaries.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SalaryAdapter(this, this);
        rvSalaries.setAdapter(adapter);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    private void setupSpinners() {
        // Months
        String[] months = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        // Years (Current year - 5 to +1)
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        List<String> years = new ArrayList<>();
        for (int i = currentYear - 5; i <= currentYear + 1; i++) {
            years.add(String.valueOf(i));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);
        spinnerYear.setSelection(5); // Select current year (index 5 in loop of 7 items if currentYear-5 is 0)
        // Adjust selection index logic:
        // index 0: current-5
        // index 5: current
        
        // Add listener to refresh list when selection changes
        /* 
           Ideally we should have a "Filter" button or refresh automatically. 
           For simplicity, let's refresh automatically on selection.
        */
        /* 
           Commented out auto-refresh for now to avoid multiple triggers on init. 
           User can use "Generate" to create, but viewing existing should probably be automatic? 
           Let's make "Generate" explicitly create, and maybe add a separate mechanism to just fetch?
           Actually, let's just use a dedicated "Filter/Load" button or do it on spinner change.
           Let's do it on spinner change for better UX.
        */
         android.widget.AdapterView.OnItemSelectedListener selectionListener = new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                loadSalaries();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        };
        
        spinnerMonth.setOnItemSelectedListener(selectionListener);
        spinnerYear.setOnItemSelectedListener(selectionListener);
    }

    private void loadSalaries() {
        int month = spinnerMonth.getSelectedItemPosition() + 1; // 1-12
        int year = Integer.parseInt(spinnerYear.getSelectedItem().toString());

        salaryList = salaryDAO.getSalariesForMonth(month, year);
        adapter.setSalaries(salaryList);

        if (salaryList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvSalaries.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvSalaries.setVisibility(View.VISIBLE);
        }

        updateSummary(month, year);
    }

    private void generateSalaries() {
        int month = spinnerMonth.getSelectedItemPosition() + 1;
        int year = Integer.parseInt(spinnerYear.getSelectedItem().toString());

        int adminId = session.getUserId();
        if (adminId == -1) {
            Toast.makeText(this, "Session Expired. Please Login Again.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = salaryDAO.generateMonthlySalaries(month, year, adminId);
        if (success) {
            // Check if any were actually created? 
            // The DAO returns true if no error, but let's assume if successful it's good.
            // We can reload list to see.
            loadSalaries();
            if (salaryList.isEmpty()) {
                 Toast.makeText(this, "No active trainers found to generate salaries for.", Toast.LENGTH_LONG).show();
            } else {
                 Toast.makeText(this, "Salaries Generated/Updated Successfully", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Failed to generate salaries.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSummary(int month, int year) {
        double pending = salaryDAO.getTotalPendingSalaries(); // This is total ALL time pending? Requirement says "Total Pending Salaries". Assuming all time or current view? Usually specific to view is better but text implies global. Let's assume global pending is useful, but contextually for this month is better? 
        // DAO method update: getTotalPendingSalaries() has no filters. 
        // Let's use it as is (Global pending).
        
        double paidThisMonth = salaryDAO.getTotalPaidSalaries(month, year);

        tvTotalPending.setText(String.format("$%.2f", pending));
        tvTotalPaid.setText(String.format("$%.2f", paidThisMonth));
    }

    @Override
    public void onPay(Salary salary) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Payment")
                .setMessage("Are you sure you want to mark " + salary.getTrainerName() + "'s salary as PAID?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    boolean success = salaryDAO.updateSalaryStatus(salary.getSalaryId(), "PAID", null);
                    if (success) {
                        Toast.makeText(this, "Payment Recorded", Toast.LENGTH_SHORT).show();
                        loadSalaries();
                    } else {
                        Toast.makeText(this, "Error Processing Payment", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onEdit(Salary salary) {
        // Dialog to edit Bonus and Deductions
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Salary Details");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);

        final EditText etBonus = new EditText(this);
        etBonus.setHint("Bonus Amount");
        etBonus.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etBonus.setText(String.valueOf(salary.getBonus()));
        layout.addView(etBonus);

        final EditText etDeductions = new EditText(this);
        etDeductions.setHint("Deductions Amount");
        etDeductions.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etDeductions.setText(String.valueOf(salary.getDeductions()));
        layout.addView(etDeductions);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            try {
                double bonus = Double.parseDouble(etBonus.getText().toString());
                double deductions = Double.parseDouble(etDeductions.getText().toString());

                boolean success = salaryDAO.updateSalaryDetails(salary.getSalaryId(), bonus, deductions);
                if (success) {
                    Toast.makeText(this, "Salary Updated", Toast.LENGTH_SHORT).show();
                    loadSalaries();
                } else {
                    Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid Numbers", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public void onViewDetails(Salary salary) {
        String details = "Trainer: " + salary.getTrainerName() + "\n" +
                         "Base Salary: $" + salary.getBaseSalary() + "\n" +
                         "Bonus: $" + salary.getBonus() + "\n" +
                         "Deductions: $" + salary.getDeductions() + "\n" +
                         "----------------------------\n" +
                         "Net Salary: $" + salary.getNetSalary() + "\n" +
                         "Status: " + salary.getStatus();
        
        if ("PAID".equals(salary.getStatus())) {
            details += "\nPaid Date: " + salary.getPaymentDate();
        }

        new AlertDialog.Builder(this)
                .setTitle("Salary Breakdown")
                .setMessage(details)
                .setPositiveButton("Close", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
