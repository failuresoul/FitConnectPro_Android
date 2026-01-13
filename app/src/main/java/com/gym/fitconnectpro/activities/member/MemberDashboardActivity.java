package com.gym.fitconnectpro.activities.member;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
        
        try {
            setContentView(R.layout.activity_member_dashboard);
        } catch (Exception e) {
            Log.e("MemberDashboard", "Failed to set content view", e);
            Toast.makeText(this, "Critical error: Cannot load layout", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        try {
            session = Session.getInstance(this);
            dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            
            if (!session.isLoggedIn()) {
                 startActivity(new Intent(this, LoginActivity.class));
                 finish();
                 return;
            }
            
            userId = session.getUserId();
            
            if (userId <= 0) {
                Toast.makeText(this, "Invalid session. Please log in again.", Toast.LENGTH_LONG).show();
                session.logout();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }
            
            dashboardDAO = new MemberDashboardDAO(this);
        } catch (Exception e) {
            Log.e("MemberDashboard", "Session initialization failed", e);
            Toast.makeText(this, "Session error. Please log in again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize views - continue even if this fails
        try {
            initViews();
            Log.d("MemberDashboard", "Views initialized successfully");
        } catch (Exception e) {
            Log.e("MemberDashboard", "Failed to initialize views", e);
            Toast.makeText(this, "Warning: Some UI elements may not display", Toast.LENGTH_SHORT).show();
        }
        
        // Setup navigation - continue even if this fails
        try {
            setupNavigation();
            Log.d("MemberDashboard", "Navigation setup successfully");
        } catch (Exception e) {
            Log.e("MemberDashboard", "Failed to setup navigation", e);
            Toast.makeText(this, "Warning: Navigation may not work properly", Toast.LENGTH_SHORT).show();
        }
        
        // Load dashboard data - continue even if this fails
        try {
            loadDashboardData();
            Log.d("MemberDashboard", "Dashboard data loaded successfully");
        } catch (Exception e) {
            Log.e("MemberDashboard", "Failed to load dashboard data", e);
            Toast.makeText(this, "Warning: Could not load all dashboard data", Toast.LENGTH_SHORT).show();
        }
        
        // Setup listeners - continue even if this fails
        try {
            setupListeners();
            Log.d("MemberDashboard", "Listeners setup successfully");
        } catch (Exception e) {
            Log.e("MemberDashboard", "Failed to setup listeners", e);
            Toast.makeText(this, "Warning: Some buttons may not work", Toast.LENGTH_SHORT).show();
        }
        
        // Setup fragment manager listener
        try {
            getSupportFragmentManager().addOnBackStackChangedListener(() -> {
                if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                    showDashboard();
                }
            });
        } catch (Exception e) {
            Log.e("MemberDashboard", "Failed to setup fragment listener", e);
        }
        
        Log.d("MemberDashboard", "onCreate completed - dashboard should be visible");
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
        try {
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
            
            // Populate navigation header with member info - with null checks
            if (navigationView.getHeaderCount() > 0) {
                View headerView = navigationView.getHeaderView(0);
                if (headerView != null) {
                    TextView tvMemberName = headerView.findViewById(R.id.tvMemberNameHeader);
                    TextView tvMemberId = headerView.findViewById(R.id.tvMemberIdHeader);
                    
                    String memberName = session.getFullName();
                    if (tvMemberName != null) {
                        if (memberName != null && !memberName.isEmpty()) {
                            tvMemberName.setText(memberName);
                        } else {
                            tvMemberName.setText("Member");
                        }
                    }
                    
                    if (tvMemberId != null) {
                        tvMemberId.setText("ID: #" + session.getUserId());
                    }
                }
            }
        } catch (Exception e) {
            Log.e("MemberDashboard", "Error in setupNavigation", e);
            // Continue anyway - navigation setup is not critical for basic functionality
        }
    }

    private void loadDashboardData() {
        try {
            String today = dateFormat.format(new Date());
            
            // 1. Header Info - with defensive null checking
            Map<String, String> info = dashboardDAO.getMemberHeaderInfo(userId);
            
            // Check if member info was found
            if (info == null || info.isEmpty()) {
                Toast.makeText(this, "Member profile not found. Please contact admin.", Toast.LENGTH_LONG).show();
                Log.e("MemberDashboard", "Member info not found for userId: " + userId);
                return;
            }
            
            if (info.containsKey("member_name") && info.get("member_name") != null) {
                tvWelcome.setText("Welcome, " + info.get("member_name"));
            } else {
                tvWelcome.setText("Welcome, Member");
                Log.w("MemberDashboard", "Member name not found");
            }
            
            // Safely parse member_id with error handling
            try {
                String memberIdStr = info.get("member_id");
                if (memberIdStr != null && !memberIdStr.isEmpty()) {
                    memberId = Integer.parseInt(memberIdStr);
                    Log.d("MemberDashboard", "Member ID: " + memberId);
                } else {
                    Log.e("MemberDashboard", "Member ID is null or empty");
                    Toast.makeText(this, "Error loading profile. Please contact admin.", Toast.LENGTH_LONG).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Log.e("MemberDashboard", "Error parsing member_id: " + info.get("member_id"), e);
                Toast.makeText(this, "Error loading profile data.", Toast.LENGTH_LONG).show();
                return;
            }
            
            if (info.containsKey("trainer_name") && info.get("trainer_name") != null) {
                tvTrainerName.setText(info.get("trainer_name"));
            } else {
                tvTrainerName.setText("No Trainer Assigned");
            }
            
            if (memberId == 0) {
                Log.e("MemberDashboard", "Invalid member ID (0)");
                return;
            }

            // 2. Today's Goals
            try {
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
            } catch (Exception e) {
                Log.e("MemberDashboard", "Error loading goals", e);
                pbWater.setMax(2500);
                pbCalories.setMax(2000);
            }

            // 3. Active Plan
            try {
                String planName = dashboardDAO.getActiveWorkoutPlanName(memberId);
                tvActivePlan.setText(planName != null ? planName : "No Active Plan");
            } catch (Exception e) {
                Log.e("MemberDashboard", "Error loading workout plan", e);
                tvActivePlan.setText("No Active Plan");
            }
            
            // 4. Meals
            try {
                int meals = dashboardDAO.getTodayMealCount(memberId, today);
                tvMealCount.setText(meals + " Meals Assigned");
            } catch (Exception e) {
                Log.e("MemberDashboard", "Error loading meals", e);
                tvMealCount.setText("0 Meals Assigned");
            }
            
            // 5. Quick Stats
            try {
                Map<String, String> stats = dashboardDAO.getQuickStats(memberId);
                tvWeightStat.setText("Wt: " + stats.getOrDefault("weight", "--"));
                tvWorkoutsStat.setText("Total: " + stats.getOrDefault("total_workouts", "0"));
                tvStreakStat.setText("Week: " + stats.getOrDefault("weekly_workouts", "0"));
            } catch (Exception e) {
                Log.e("MemberDashboard", "Error loading stats", e);
                tvWeightStat.setText("Wt: --");
                tvWorkoutsStat.setText("Total: 0");
                tvStreakStat.setText("Week: 0");
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading dashboard: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("MemberDashboard", "Critical error in loadDashboardData", e);
            e.printStackTrace();
        }
    }

    private void setupListeners() {
        btnMessageTrainer.setOnClickListener(v -> Toast.makeText(this, "Trainer Chat - Coming Soon", Toast.LENGTH_SHORT).show());
        
        View.OnClickListener planClickListener = v -> {
            Intent intent = new Intent(MemberDashboardActivity.this, ViewWorkoutPlanActivity.class);
            startActivity(intent);
        };
        
        btnStartWorkout.setOnClickListener(planClickListener);
        tvActivePlan.setOnClickListener(planClickListener);
        
        btnViewMeals.setOnClickListener(v -> Toast.makeText(this, "View Meals - Coming Soon", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.nav_dashboard) {
            showDashboard();
        } else if (id == R.id.nav_workouts) {
            // View Workout Plan - shows the assigned workout plan
             Intent intent = new Intent(this, ViewWorkoutPlanActivity.class);
             startActivity(intent);
        } else if (id == R.id.nav_log_workout) {
            // Log Workout - shows today's workout session/logs
            Intent intent = new Intent(this, WorkoutLogActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_meals) {
            // My Meal Plans
            Intent intent = new Intent(this, ViewMealPlanActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_water) {
            // Water Tracker
            Intent intent = new Intent(this, WaterTrackerActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            session.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Feature Coming Soon", Toast.LENGTH_SHORT).show();
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
