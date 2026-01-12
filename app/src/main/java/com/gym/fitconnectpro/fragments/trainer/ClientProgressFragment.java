package com.gym.fitconnectpro.fragments.trainer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.components.SimpleLineChart;
import com.gym.fitconnectpro.dao.ProgressDAO;
import com.gym.fitconnectpro.dao.TrainerDAO;
import com.gym.fitconnectpro.database.entities.Member;
import com.gym.fitconnectpro.models.ProgressReport;
import com.gym.fitconnectpro.models.WeightLog;
import com.gym.fitconnectpro.services.Session;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClientProgressFragment extends Fragment {

    // UI Components
    private Spinner spinnerClient;
    private Button btnDateRange, btnRefresh, btnSendReport;
    private TextView tvWorkoutRate, tvMealsLogged, tvWaterCompliance, tvWeightChange;
    private EditText etFeedback;
    private SimpleLineChart chartWeight;

    // Data
    private TrainerDAO trainerDAO;
    private ProgressDAO progressDAO;
    private Session session;
    private int trainerId;
    private List<Member> clientList;
    private Member selectedMember;

    // State
    private LocalDate startDate, endDate;

    public static ClientProgressFragment newInstance() {
        return new ClientProgressFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_client_progress, container, false);
        
        initViews(view);
        initData();
        setupListeners();
        
        return view;
    }

    private void initViews(View view) {
        spinnerClient = view.findViewById(R.id.spinnerClient);
        btnDateRange = view.findViewById(R.id.btnDateRange);
        btnRefresh = view.findViewById(R.id.btnRefresh);
        btnSendReport = view.findViewById(R.id.btnSendReport);
        
        tvWorkoutRate = view.findViewById(R.id.tvWorkoutRate);
        tvMealsLogged = view.findViewById(R.id.tvMealsLogged);
        tvWaterCompliance = view.findViewById(R.id.tvWaterCompliance);
        tvWeightChange = view.findViewById(R.id.tvWeightChange);
        
        etFeedback = view.findViewById(R.id.etFeedback);
        chartWeight = view.findViewById(R.id.chartWeight);
    }

    private void initData() {
        trainerDAO = new TrainerDAO(getContext());
        progressDAO = new ProgressDAO(getContext());
        session = Session.getInstance(getContext());
        trainerId = session.getUserId();

        // Default: Last 7 days
        endDate = LocalDate.now();
        startDate = endDate.minusDays(6);
        updateDateButton();

        loadClients();
    }

    private void loadClients() {
        clientList = trainerDAO.getMyAssignedClients(trainerId);
        if (clientList == null) clientList = new ArrayList<>();

        List<String> names = new ArrayList<>();
        for (Member m : clientList) names.add(m.getFullName());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClient.setAdapter(adapter);
    }
    
    private void updateDateButton() {
        btnDateRange.setText(startDate.toString() + " to " + endDate.toString());
    }

    private void setupListeners() {
        spinnerClient.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMember = clientList.get(position);
                loadProgressData();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnRefresh.setOnClickListener(v -> loadProgressData());
        
        // Simple toggle for demo: 7 days vs 30 days
        btnDateRange.setOnClickListener(v -> {
            if (startDate.isEqual(endDate.minusDays(6))) {
                startDate = endDate.minusDays(29); // Switch to 30 days
            } else {
                startDate = endDate.minusDays(6); // Switch back to 7 days
            }
            updateDateButton();
            loadProgressData();
        });

        btnSendReport.setOnClickListener(v -> generateReport());
    }

    private void loadProgressData() {
        if (selectedMember == null) return;
        
        String start = startDate.toString();
        String end = endDate.toString();

        // 1. Get Stats
        Map<String, Object> stats = progressDAO.getClientProgress(selectedMember.getMemberId(), start, end);
        
        Double workoutRate = (Double) stats.get("workoutCompletionRate");
        Integer meals = (Integer) stats.get("mealsLoggedCount");
        Integer waterDays = (Integer) stats.get("waterComplianceDays");
        
        tvWorkoutRate.setText(String.format("%.0f%%", workoutRate));
        tvMealsLogged.setText(String.valueOf(meals));
        tvWaterCompliance.setText(waterDays + " days");

        // 2. Get Weight History
        List<WeightLog> weightLogs = progressDAO.getWeightHistory(selectedMember.getMemberId(), start, end);
        
        // Calculate change
        double change = 0;
        if (weightLogs.size() >= 2) {
            double first = weightLogs.get(0).getWeight();
            double last = weightLogs.get(weightLogs.size() - 1).getWeight();
            change = last - first;
        }
        
        String sign = change > 0 ? "+" : "";
        tvWeightChange.setText(String.format("%s%.1f kg", sign, change));
        
        // Update Chart
        chartWeight.setData(weightLogs);
    }
    
    private void generateReport() {
        if (selectedMember == null) {
            Toast.makeText(getContext(), "Select a client first", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressReport report = new ProgressReport();
        report.setTrainerId(trainerId);
        report.setMemberId(selectedMember.getMemberId());
        report.setReportStartDate(startDate.toString());
        report.setReportEndDate(endDate.toString());
        report.setTrainerFeedback(etFeedback.getText().toString());
        
        // Save snapshot of current metrics
        // (In a real app, you might want to re-calculate to ensure accuracy, but using UI values for demo simplicity or cached values)
        // Let's re-fetch simplified
        Map<String, Object> stats = progressDAO.getClientProgress(selectedMember.getMemberId(), startDate.toString(), endDate.toString());
        report.setWorkoutCompletionRate((Double) stats.get("workoutCompletionRate"));
        report.setMealsLoggedCount((Integer) stats.get("mealsLoggedCount"));
        
        // Calculate water rate
        int waterDays = (Integer) stats.get("waterComplianceDays");
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        report.setWaterComplianceRate((double) waterDays / totalDays * 100);
        
        // Weight change
        List<WeightLog> logs = progressDAO.getWeightHistory(selectedMember.getMemberId(), startDate.toString(), endDate.toString());
        if (!logs.isEmpty()) {
            double startW = logs.get(0).getWeight();
            double endW = logs.get(logs.size()-1).getWeight();
            report.setWeightChange(endW - startW);
        } else {
             report.setWeightChange(0);
        }

        if (progressDAO.saveWeeklyReport(report)) {
            Toast.makeText(getContext(), "Report generated & sent to client!", Toast.LENGTH_LONG).show();
            etFeedback.setText("");
        } else {
            Toast.makeText(getContext(), "Failed to save report", Toast.LENGTH_SHORT).show();
        }
    }
}
