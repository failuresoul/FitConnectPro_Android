package com.gym.fitconnectpro.activities.admin;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.adapters.SalaryReportAdapter;
import com.gym.fitconnectpro.dao.SalaryDAO;
import com.gym.fitconnectpro.dao.TrainerDAO;
import com.gym.fitconnectpro.database.entities.Salary;
import com.gym.fitconnectpro.database.entities.Trainer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SalaryReportsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvStartDate, tvEndDate, tvTotalPaid;
    private Spinner spinnerTrainer;
    private Button btnGenerateReport;
    private LineChart chartSalaryTrend;
    private RecyclerView rvReportBreakdown;

    private SalaryDAO salaryDAO;
    private TrainerDAO trainerDAO;
    private SalaryReportAdapter adapter;

    private Calendar startCalendar, endCalendar;
    private SimpleDateFormat dateFormat;
    private List<Trainer> trainerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salary_reports);

        salaryDAO = new SalaryDAO(this);
        trainerDAO = new TrainerDAO(this);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        // Default range: Last 30 days
        startCalendar = Calendar.getInstance();
        startCalendar.add(Calendar.DAY_OF_YEAR, -30);
        endCalendar = Calendar.getInstance();

        initViews();
        setupToolbar();
        setupSpinners();
        setupListeners();
        setupChart();

        // Initial Load
        loadReportData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        tvTotalPaid = findViewById(R.id.tvTotalPaid);
        spinnerTrainer = findViewById(R.id.spinnerTrainer);
        btnGenerateReport = findViewById(R.id.btnGenerateReport);
        chartSalaryTrend = findViewById(R.id.chartSalaryTrend);
        rvReportBreakdown = findViewById(R.id.rvReportBreakdown);

        // Set initial date text
        updateDateLabels();

        // Setup RecyclerView
        rvReportBreakdown.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SalaryReportAdapter(this);
        rvReportBreakdown.setAdapter(adapter);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Salary Reports");
        }
    }

    private void setupSpinners() {
        trainerList = trainerDAO.getAvailableTrainers();
        List<String> trainerNames = new ArrayList<>();
        trainerNames.add("All Trainers");
        for (Trainer t : trainerList) {
            trainerNames.add(t.getFullName());
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, trainerNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTrainer.setAdapter(spinnerAdapter);
    }

    private void setupListeners() {
        tvStartDate.setOnClickListener(v -> showDatePicker(startCalendar, tvStartDate));
        tvEndDate.setOnClickListener(v -> showDatePicker(endCalendar, tvEndDate));
        btnGenerateReport.setOnClickListener(v -> loadReportData());
    }

    private void showDatePicker(Calendar calendar, TextView textView) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    textView.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateLabels() {
        tvStartDate.setText(dateFormat.format(startCalendar.getTime()));
        tvEndDate.setText(dateFormat.format(endCalendar.getTime()));
    }

    private void setupChart() {
        chartSalaryTrend.getDescription().setEnabled(false);
        chartSalaryTrend.setDrawGridBackground(false);
        chartSalaryTrend.getAxisRight().setEnabled(false);
        XAxis xAxis = chartSalaryTrend.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
    }

    private void loadReportData() {
        String startStr = tvStartDate.getText().toString();
        String endStr = tvEndDate.getText().toString();

        int selectedPosition = spinnerTrainer.getSelectedItemPosition();
        int trainerId = 0;
        if (selectedPosition > 0) {
            // Adjust for "All Trainers" at index 0
            trainerId = trainerList.get(selectedPosition - 1).getTrainerId();
        }

        List<Salary> data;
        if (trainerId == 0) {
            data = salaryDAO.getSalariesByDateRange(startStr, endStr);
        } else {
            data = salaryDAO.getSalariesByTrainerAndDateRange(trainerId, startStr, endStr);
        }

        updateList(data);
        calculateTotal(data);
        updateChart(data);
    }

    private void updateList(List<Salary> data) {
        adapter.setSalaries(data);
        if (data.isEmpty()) {
            Toast.makeText(this, "No records found for this period", Toast.LENGTH_SHORT).show();
        }
    }

    private void calculateTotal(List<Salary> data) {
        double total = 0;
        for (Salary s : data) {
            if ("PAID".equalsIgnoreCase(s.getStatus())) {
                total += s.getNetSalary();
            }
        }
        tvTotalPaid.setText(String.format("$%.2f", total));
    }

    private void updateChart(List<Salary> data) {
        if (data.isEmpty()) {
            chartSalaryTrend.clear();
            return;
        }

        // Group by Date for the chart
        // Map<DateString, TotalAmount>
        Map<String, Double> dailyTotals = new HashMap<>();
        for (Salary s : data) {
            if ("PAID".equalsIgnoreCase(s.getStatus()) && s.getPaymentDate() != null) {
                String date = s.getPaymentDate();
                dailyTotals.put(date, dailyTotals.getOrDefault(date, 0.0) + s.getNetSalary());
            }
        }

        // Sort by date
        List<String> sortedDates = new ArrayList<>(dailyTotals.keySet());
        Collections.sort(sortedDates);

        List<Entry> entries = new ArrayList<>();
        List<String> xLabels = new ArrayList<>();
        
        for (int i = 0; i < sortedDates.size(); i++) {
            String date = sortedDates.get(i);
            entries.add(new Entry(i, dailyTotals.get(date).floatValue()));
            xLabels.add(date.substring(5)); // Show MM-dd
        }

        LineDataSet dataSet = new LineDataSet(entries, "Salary Expense Trend");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setCircleRadius(4f);

        LineData lineData = new LineData(dataSet);
        chartSalaryTrend.setData(lineData);
        chartSalaryTrend.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xLabels));
        chartSalaryTrend.invalidate(); // Refresh
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
