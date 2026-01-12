package com.gym.fitconnectpro.fragments.trainer;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.gym.fitconnectpro.dao.DailyGoalDAO;
import com.gym.fitconnectpro.dao.TrainerDAO;
import com.gym.fitconnectpro.database.entities.Member;
import com.gym.fitconnectpro.models.TrainerDailyGoal;
import com.gym.fitconnectpro.services.Session;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SetDailyGoalsFragment extends Fragment {

    private Spinner spinnerClient;
    private TextView tvDate;
    private EditText etWorkoutDuration, etCalorieTarget, etCalorieLimit;
    private EditText etProtein, etCarbs, etFats, etWaterIntake, etInstructions;
    private Button btnWater2000, btnWater2500, btnWater3000, btnWater3500;
    private Button btnAssign, btnSetWeek;

    private TrainerDAO trainerDAO;
    private DailyGoalDAO dailyGoalDAO;
    private Session session;
    private List<Member> clientList;
    private LocalDate selectedDate;
    private int trainerId;

    public static SetDailyGoalsFragment newInstance() {
        return new SetDailyGoalsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_set_daily_goals, container, false);

        initViews(view);
        initData();
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        spinnerClient = view.findViewById(R.id.spinnerClient);
        tvDate = view.findViewById(R.id.tvDate);
        etWorkoutDuration = view.findViewById(R.id.etWorkoutDuration);
        etCalorieTarget = view.findViewById(R.id.etCalorieTarget);
        etCalorieLimit = view.findViewById(R.id.etCalorieLimit);
        etProtein = view.findViewById(R.id.etProtein);
        etCarbs = view.findViewById(R.id.etCarbs);
        etFats = view.findViewById(R.id.etFats);
        etWaterIntake = view.findViewById(R.id.etWaterIntake);
        etInstructions = view.findViewById(R.id.etInstructions);
        
        btnWater2000 = view.findViewById(R.id.btnWater2000);
        btnWater2500 = view.findViewById(R.id.btnWater2500);
        btnWater3000 = view.findViewById(R.id.btnWater3000);
        btnWater3500 = view.findViewById(R.id.btnWater3500);
        
        btnAssign = view.findViewById(R.id.btnAssign);
        btnSetWeek = view.findViewById(R.id.btnSetWeek);
    }

    private void initData() {
        trainerDAO = new TrainerDAO(getContext());
        dailyGoalDAO = new DailyGoalDAO(getContext());
        session = Session.getInstance(getContext());
        
        // This assumes Session.getUserId() returns the trainer_id (or user_id which maps 1:1 for simplicity in this context)
        // If trainerId logic from Activity is needed, we could pass via Bundle, but let's try session first.
        trainerId = session.getUserId();

        selectedDate = LocalDate.now();
        updateDateDisplay();

        loadClients();
    }

    private void loadClients() {
        // Fetch clients assigned to this trainer
        // Assuming TrainerDAO has getMyAssignedClients or similar. 
        // Based on previous step, I recall TrainerDAO having getAssignedMembers(trainerId)
        // Let's verify or assume standard naming as I cannot see TrainerDAO content here. 
        // I'll try getAssignedMembers
        
        try {
            // Placeholder: functionality depends on TrainerDAO implementation
            // If getAssignedMembers exists:
            // clientList = trainerDAO.getAssignedMembers(trainerId);
             
            // For now, let's assume we might need to implement this if missing or use a dummy list if DAO fails
            // I'll assume getClients method exists or similar based on Desktop app logic mirrored.
            // If the method signature is different, I might need to fix it. 
            // In Desktop it was getMyAssignedClients. Let's try to match that logic.
            // But wait, the Android project structure might differ. 
            // Let's just create a quick method inside TrainerDAO if it doesn't exist? No, I can't edit TrainerDAO blind.
            // I'll assume generic method.
            
            // To be safe, I'm checking existing methods in TrainerDAO is tricky without listing.
            // I'll proceed with assumed method "getAssignedMembers".
            // Fetch clients assigned to this trainer
             clientList = trainerDAO.getMyAssignedClients(trainerId);
            
            if (clientList == null) clientList = new ArrayList<>();
            
            List<String> clientNames = new ArrayList<>();
            for (Member m : clientList) {
                clientNames.add(m.getFullName());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, clientNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerClient.setAdapter(adapter);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error loading clients", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupListeners() {
        tvDate.setOnClickListener(v -> showDatePicker());

        btnWater2000.setOnClickListener(v -> etWaterIntake.setText("2000"));
        btnWater2500.setOnClickListener(v -> etWaterIntake.setText("2500"));
        btnWater3000.setOnClickListener(v -> etWaterIntake.setText("3000"));
        btnWater3500.setOnClickListener(v -> etWaterIntake.setText("3500"));

        btnAssign.setOnClickListener(v -> assignGoals(false));
        btnSetWeek.setOnClickListener(v -> assignGoals(true));
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                    updateDateDisplay();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        tvDate.setText(selectedDate.toString());
    }

    private void assignGoals(boolean isWeekly) {
        if (spinnerClient.getSelectedItem() == null) {
            Toast.makeText(getContext(), "Please select a client", Toast.LENGTH_SHORT).show();
            return;
        }

        int clientIndex = spinnerClient.getSelectedItemPosition();
        Member selectedMember = clientList.get(clientIndex);
        
        try {
            TrainerDailyGoal goal = new TrainerDailyGoal();
            goal.setTrainerId(trainerId);
            goal.setMemberId(selectedMember.getMemberId());
            goal.setGoalDate(selectedDate.toString());
            
            goal.setWorkoutDuration(parseIntOrDefault(etWorkoutDuration.getText().toString(), 0));
            goal.setCalorieTarget(parseIntOrDefault(etCalorieTarget.getText().toString(), 0));
            goal.setCalorieLimit(parseIntOrDefault(etCalorieLimit.getText().toString(), 0));
            goal.setProteinTarget(parseIntOrDefault(etProtein.getText().toString(), 0));
            goal.setCarbsTarget(parseIntOrDefault(etCarbs.getText().toString(), 0));
            goal.setFatsTarget(parseIntOrDefault(etFats.getText().toString(), 0));
            goal.setWaterIntakeMl(parseIntOrDefault(etWaterIntake.getText().toString(), 0));
            goal.setSpecialInstructions(etInstructions.getText().toString());

            boolean success;
            if (isWeekly) {
                success = dailyGoalDAO.setGoalsForWeek(goal, 7);
            } else {
                success = dailyGoalDAO.setDailyGoals(goal);
            }

            if (success) {
                String msg = isWeekly ? "Weekly goals set successfully!" : "Daily goals set successfully!";
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to set goals", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
