package com.gym.fitconnectpro.fragments.trainer;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.dao.TrainerDAO;
import com.gym.fitconnectpro.dao.WorkoutPlanDAO;
import com.gym.fitconnectpro.database.entities.Member;
import com.gym.fitconnectpro.database.entities.WorkoutPlan;

import java.util.List;

public class ClientPlansFragment extends Fragment {

    private static final String ARG_MEMBER_ID = "member_id";
    private int memberId;
    
    private TextView tvClientPlansHeader;
    private TextView tvWorkoutPlansList;
    private TextView tvMealPlansList;
    private FloatingActionButton fabCreatePlan;

    public ClientPlansFragment() {
        // Required empty public constructor
    }

    public static ClientPlansFragment newInstance(int memberId) {
        ClientPlansFragment fragment = new ClientPlansFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MEMBER_ID, memberId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            memberId = getArguments().getInt(ARG_MEMBER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_client_plans, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        tvClientPlansHeader = view.findViewById(R.id.tvClientPlansHeader);
        tvWorkoutPlansList = view.findViewById(R.id.tvWorkoutPlansList);
        tvMealPlansList = view.findViewById(R.id.tvMealPlansList);
        fabCreatePlan = view.findViewById(R.id.fabCreatePlan);
        
        android.widget.ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        fabCreatePlan.setOnClickListener(v -> {
            // Navigate to Create Plan with pre-selected member
             Fragment createFragment = CreateWorkoutPlanFragment.newInstance(memberId);
             getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, createFragment)
                    .addToBackStack(null)
                    .commit();
        });

        loadData();
    }

    private void loadData() {
        try {
            // 1. Get Member Name for Header
            TrainerDAO trainerDAO = new TrainerDAO(requireContext());
            Member member = trainerDAO.getClientDetails(memberId);
            if (member != null) {
                tvClientPlansHeader.setText(member.getFullName() + "'s Plans");
            } else {
                tvClientPlansHeader.setText("Client Plans");
            }

            // 2. Load Workout Plans
            WorkoutPlanDAO workoutPlanDAO = new WorkoutPlanDAO(requireContext());
            List<WorkoutPlan> plans = workoutPlanDAO.getPlansByMemberId(memberId);

            if (plans != null && !plans.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (WorkoutPlan plan : plans) {
                    if (sb.length() > 0) sb.append("\n\n====================\n\n");
                    
                    sb.append("PLAN: ").append(plan.getPlanName());
                    sb.append("\nGenerated: ").append(plan.getStartDate());
                    sb.append("\nStatus: ").append(plan.getStatus());
                    
                    // Fetch exercises for this plan
                    List<com.gym.fitconnectpro.database.entities.PlanExercise> exercises = workoutPlanDAO.getPlanExercises(plan.getId());
                    if (exercises != null && !exercises.isEmpty()) {
                        sb.append("\n\nExercises:");
                        for (com.gym.fitconnectpro.database.entities.PlanExercise ex : exercises) {
                            sb.append("\n• ").append(ex.getExercise() != null ? ex.getExercise().getName() : "Unknown Exercise");
                            sb.append(" | ").append(ex.getSets()).append(" x ").append(ex.getReps());
                            if (ex.getWeightKg() > 0) sb.append(" @ ").append(ex.getWeightKg()).append("kg");
                        }
                    } else {
                        sb.append("\n\n(No exercises added)");
                    }
                }
                tvWorkoutPlansList.setText(sb.toString());
            } else {
                tvWorkoutPlansList.setText("No active workout plans assigned.");
            }

            // 3. Meal Plans
            com.gym.fitconnectpro.dao.MealPlanDAO mealPlanDAO = new com.gym.fitconnectpro.dao.MealPlanDAO(requireContext());
            List<com.gym.fitconnectpro.models.MealPlan> mealPlans = mealPlanDAO.getMealPlans(memberId);
            
            if (mealPlans != null && !mealPlans.isEmpty()) {
                StringBuilder mpSb = new StringBuilder();
                String currentDate = "";
                
                for (com.gym.fitconnectpro.models.MealPlan mp : mealPlans) {
                    // Group by date visually
                    if (!mp.getPlanDate().equals(currentDate)) {
                        if (mpSb.length() > 0) mpSb.append("\n\n--------------------\n\n");
                        mpSb.append("DATE: ").append(mp.getPlanDate());
                        currentDate = mp.getPlanDate();
                    }
                    
                    mpSb.append("\n\n").append(mp.getMealType().toUpperCase());
                    if (mp.getInstructions() != null && !mp.getInstructions().isEmpty()) {
                        mpSb.append(" (").append(mp.getInstructions()).append(")");
                    }
                    
                    if (mp.getFoods() != null && !mp.getFoods().isEmpty()) {
                        for (com.gym.fitconnectpro.models.MealPlanFood f : mp.getFoods()) {
                            double cal = 0;
                            if (f.getFood() != null) cal = f.getFood().getCalories() * f.getQuantity();
                            
                            mpSb.append("\n• ").append(f.getFood() != null ? f.getFood().getName() : "Unknown");
                            mpSb.append(" x ").append(f.getQuantity()).append(" (~").append((int)cal).append(" kcal)");
                        }
                    } else {
                        mpSb.append("\n(No foods listed)");
                    }
                }
                tvMealPlansList.setText(mpSb.toString());
            } else {
                tvMealPlansList.setText("No active meal plans assigned.");
            }
            
        } catch (Exception e) {
            Log.e("ClientPlansFragment", "Error loading data", e);
            Toast.makeText(getContext(), "Error loading plans", Toast.LENGTH_SHORT).show();
        }
    }
}
