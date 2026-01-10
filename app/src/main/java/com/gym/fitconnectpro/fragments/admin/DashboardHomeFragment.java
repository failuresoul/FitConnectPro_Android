package com.gym.fitconnectpro.fragments.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.activities.admin.MemberRegistrationActivity;
import com.gym.fitconnectpro.activities.admin.TrainerRegistrationActivity;
import com.gym.fitconnectpro.dao.StatisticsDAO;

public class DashboardHomeFragment extends Fragment {

    private Button btnAddMember, btnAddTrainer, btnAssignTrainer;
    private StatisticsDAO statisticsDAO;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard_home, container, false);

        btnAddMember = view.findViewById(R.id.btnAddMember);
        btnAddTrainer = view.findViewById(R.id.btnAddTrainer);
        btnAssignTrainer = view.findViewById(R.id.btnAssignTrainer);

        setupListeners();

        statisticsDAO = new StatisticsDAO(requireContext());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardStats();
    }

    private void setupListeners() {
        btnAddMember.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MemberRegistrationActivity.class);
            startActivity(intent);
        });

        btnAddTrainer.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TrainerRegistrationActivity.class);
            startActivity(intent);
        });

        btnAssignTrainer.setOnClickListener(v -> {
            AssignTrainerDialogFragment dialog = new AssignTrainerDialogFragment();
            dialog.setOnAssignmentCompleteListener(() -> {
                // Refresh dashboard stats after assignment
                loadDashboardStats();
            });
            dialog.show(getParentFragmentManager(), "AssignTrainerDialog");
        });
    }

    private void loadDashboardStats() {
        if (statisticsDAO == null) return;

        // Get Views
        android.widget.TextView tvTotalMembers = getView().findViewById(R.id.tvTotalMembers);
        android.widget.TextView tvTotalTrainers = getView().findViewById(R.id.tvTotalTrainers);
        android.widget.TextView tvMonthlyRevenue = getView().findViewById(R.id.tvMonthlyRevenue);

        // Fetch Data
        int totalMembers = statisticsDAO.getTotalMembers();
        int totalTrainers = statisticsDAO.getTotalTrainers();
        double monthlyRevenue = statisticsDAO.getMonthlyRevenue();

        // Update UI
        if (tvTotalMembers != null) {
            tvTotalMembers.setText(String.valueOf(totalMembers));
        }
        if (tvTotalTrainers != null) {
            tvTotalTrainers.setText(String.valueOf(totalTrainers));
        }
        if (tvMonthlyRevenue != null) {
            tvMonthlyRevenue.setText(String.format("BDT%.2f", monthlyRevenue));
        }
    }
}
