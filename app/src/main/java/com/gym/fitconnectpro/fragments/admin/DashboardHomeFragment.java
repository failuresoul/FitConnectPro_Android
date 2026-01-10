package com.gym.fitconnectpro.fragments.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.dao.StatisticsDAO;

public class DashboardHomeFragment extends Fragment {

    private TextView tvTotalMembers, tvTotalTrainers, tvMonthlyRevenue;
    private StatisticsDAO statisticsDAO;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard_home, container, false);

        initializeViews(view);
        loadStatistics();

        return view;
    }

    private void initializeViews(View view) {
        tvTotalMembers = view.findViewById(R.id.tvTotalMembers);
        tvTotalTrainers = view.findViewById(R.id.tvTotalTrainers);
        tvMonthlyRevenue = view.findViewById(R.id.tvMonthlyRevenue);

        view.findViewById(R.id.btnAddMember).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(getActivity(), com.gym.fitconnectpro.activities.admin.MemberRegistrationActivity.class);
            startActivity(intent);
        });

        statisticsDAO = new StatisticsDAO(requireContext());
    }

    private void loadStatistics() {
        new Thread(() -> {
            try {
                int totalMembers = statisticsDAO.getTotalMembers();
                int totalTrainers = statisticsDAO.getTotalTrainers();
                double monthlyRevenue = statisticsDAO.getMonthlyRevenue();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        tvTotalMembers.setText(String.valueOf(totalMembers));
                        tvTotalTrainers.setText(String.valueOf(totalTrainers));
                        tvMonthlyRevenue.setText(String.format("৳%.2f", monthlyRevenue));
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStatistics();
    }
}

