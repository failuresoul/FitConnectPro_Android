package com.gym.fitconnectpro.activities.member;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.activities.LoginActivity;
import com.gym.fitconnectpro.dao.MemberDashboardDAO;
import com.gym.fitconnectpro.services.Session;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class MemberDashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ScrollView dashboardContent;
    private android.widget.FrameLayout fragmentContainer;

    // Dashboard UI
    private TextView tvWelcome, tvTrainerName, tvActivePlan, tvMealCount;
    private TextView tvWeightStat, tvWorkoutsStat, tvStreakStat;
    private ProgressBar pbCalories, pbWater;
    private Button btnMessageTrainer, btnStartWorkout, btnViewMeals;

    private Session session;
    private MemberDashboardDAO dashboardDAO;
    private int userId;
    private int memberId; // We need this to query member-specific tables
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_dashboard);

        session = Session.getInstance(this);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        if (!session.isLoggedIn()) {
             startActivity(new Intent(this, LoginActivity.class));
             finish();
             return;
        }
        
        userId = session.getUserId();
        dashboardDAO = new MemberDashboardDAO(this);

        initViews();
        setupNavigation();
        loadDashboardData();
        setupListeners();
        
         getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                showDashboard();
            }
        });
    }

    private void initViews() {
        dashboardContent = findViewById(R.id.member_dashboard_content);
        fragmentContainer = findViewById(R.id.fragment_container_member);
        
        tvWelcome = findViewById(R.id.tvWelcomeMember);
        tvTrainerName = findViewById(R.id.tvTrainerName);
        tvActivePlan = findViewById(R.id.tvActivePlan);
        tvMealCount = findViewById(R.id.tvMealCount);
        
        tvWeightStat = findViewById(R.id.tvWeightStat);
        tvWorkoutsStat = findViewById(R.id.tvWorkoutsStat);
        tvStreakStat = findViewById(R.id.tvStreakStat);
        
        pbCalories = findViewById(R.id.pbCalories);
        pbWater = findViewById(R.id.pbWater);
        
        btnMessageTrainer = findViewById(R.id.btnMessageTrainer);
        btnStartWorkout = findViewById(R.id.btnStartWorkout);
        btnViewMeals = findViewById(R.id.btnViewMeals);
    }

    private void setupNavigation() {
        Toolbar toolbar = findViewById(R.id.toolbar_member);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout_member);
        navigationView = findViewById(R.id.nav_view_member);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, 
                R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        
        // Populate navigation header with member info
        View headerView = navigationView.getHeaderView(0);
        TextView tvMemberName = headerView.findViewById(R.id.tvMemberNameHeader);
        TextView tvMemberId = headerView.findViewById(R.id.tvMemberIdHeader);
        
        String memberName = session.getFullName();
        if (memberName != null && !memberName.isEmpty()) {
            tvMemberName.setText(memberName);
        }
        tvMemberId.setText("ID: #" + session.getUserId());
    }

    private void loadDashboardData() {
        try {
            String today = dateFormat.format(new Date());
            
            // 1. Header Info
            Map<String, String> info = dashboardDAO.getMemberHeaderInfo(userId);
            if (info.containsKey("member_name")) {
                tvWelcome.setText("Welcome, " + info.get("member_name"));
                memberId = Integer.parseInt(info.get("member_id"));
            }
            if (info.containsKey("trainer_name")) {
                tvTrainerName.setText(info.get("trainer_name"));
            }
            
            if (memberId == 0) return;

            // 2. Today's Goals
            Map<String, Object> goals = dashboardDAO.getTodayGoals(memberId, today);
            if (goals.containsKey("water_target")) {
                int waterTarget = (Integer) goals.get("water_target");
                pbWater.setMax(waterTarget > 0 ? waterTarget : 2500);
                
                int calTarget = (Integer) goals.get("calories_target");
                pbCalories.setMax(calTarget > 0 ? calTarget : 2000);
            } else {
                 // Defaults
                 pbWater.setMax(2500);
                 pbCalories.setMax(2000);
            }

            // 3. active Plan
            String planName = dashboardDAO.getActiveWorkoutPlanName(memberId);
            tvActivePlan.setText(planName);
            
            // 4. Meals
            int meals = dashboardDAO.getTodayMealCount(memberId, today);
            tvMealCount.setText(meals + " Meals Assigned");
            
            // 5. Quick Stats
            Map<String, String> stats = dashboardDAO.getQuickStats(memberId);
            tvWeightStat.setText("Wt: " + stats.getOrDefault("weight", "--"));
            tvWorkoutsStat.setText("Total: " + stats.getOrDefault("total_workouts", "0"));
            tvStreakStat.setText("Week: " + stats.getOrDefault("weekly_workouts", "0"));
        } catch (Exception e) {
            Toast.makeText(this, "Error loading dashboard: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void setupListeners() {
        btnMessageTrainer.setOnClickListener(v -> Toast.makeText(this, "Trainer Chat - Coming Soon", Toast.LENGTH_SHORT).show());
        btnStartWorkout.setOnClickListener(v -> Toast.makeText(this, "Start Workout - Coming Soon", Toast.LENGTH_SHORT).show());
        btnViewMeals.setOnClickListener(v -> Toast.makeText(this, "View Meals - Coming Soon", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.nav_dashboard) {
            showDashboard();
        } else if (id == R.id.nav_logout) {
            session.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Feature Coming Soon", Toast.LENGTH_SHORT).show();
            // Implement fragment loading (e.g., MyWorkoutsFragment, etc.)
        }
        
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    
    private void showDashboard() {
        // Clear all fragments from back stack
        getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
        
        // Show dashboard content, hide fragment container
        if (dashboardContent != null) dashboardContent.setVisibility(View.VISIBLE);
        if (fragmentContainer != null) fragmentContainer.setVisibility(View.GONE);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Member Dashboard");
        }
    }
    
    private void loadFragment(Fragment fragment, String title) {
        if (dashboardContent != null) dashboardContent.setVisibility(View.GONE);
        if (fragmentContainer != null) fragmentContainer.setVisibility(View.VISIBLE);
        getSupportActionBar().setTitle(title);
        
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container_member, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
    
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
