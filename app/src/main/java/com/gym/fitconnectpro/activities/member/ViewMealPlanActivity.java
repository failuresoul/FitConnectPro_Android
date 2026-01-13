package com.gym.fitconnectpro.activities.member;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.activities.LoginActivity;
import com.gym.fitconnectpro.dao.MealPlanDAO;
import com.gym.fitconnectpro.dao.MemberDashboardDAO;
import com.gym.fitconnectpro.models.MealPlan;
import com.gym.fitconnectpro.models.MealPlanFood;
import com.gym.fitconnectpro.services.Session;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ViewMealPlanActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private ImageButton btnPreviousDay, btnNextDay;
    private TextView tvSelectedDate, tvTotalCalories, tvTotalProtein, tvTotalCarbs, tvTotalFats;
    private TabLayout tabLayout;
    private TextView tvMealContent;
    private Button btnMarkAsEaten, btnLogDifferent;
    private LinearLayout actionButtons;
    
    private MealPlanDAO mealPlanDAO;
    private MemberDashboardDAO memberDashboardDAO;
    private Session session;
    private int memberId;
    
    private Calendar selectedDate;
    private SimpleDateFormat dateFormat;
    private String[] mealTypes = {"Breakfast", "Lunch", "Dinner", "Snacks"};
    private MealPlan currentMealPlan;
    private String currentMealType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_view_meal_plan);

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            
            // Setup drawer
            drawerLayout = findViewById(R.id.drawer_layout);
            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
            
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawerLayout, toolbar,
                    R.string.open_drawer, R.string.close_drawer);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
            
            session = Session.getInstance(this);
            mealPlanDAO = new MealPlanDAO(this);
            memberDashboardDAO = new MemberDashboardDAO(this);
            
            // Get member ID
            int userId = session.getUserId();
            var memberInfo = memberDashboardDAO.getMemberHeaderInfo(userId);
            if (memberInfo != null && memberInfo.containsKey("member_id")) {
                memberId = Integer.parseInt(memberInfo.get("member_id"));
            } else {
                Toast.makeText(this, "Error loading member info", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            selectedDate = Calendar.getInstance();
            dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            
            initViews();
            setupListeners();
            loadMealPlans();
        } catch (Exception e) {
            Toast.makeText(this, "Error loading meal plans: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }
    }

    private void initViews() {
        btnPreviousDay = findViewById(R.id.btnPreviousDay);
        btnNextDay = findViewById(R.id.btnNextDay);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvTotalCalories = findViewById(R.id.tvTotalCalories);
        tvTotalProtein = findViewById(R.id.tvTotalProtein);
        tvTotalCarbs = findViewById(R.id.tvTotalCarbs);
        tvTotalFats = findViewById(R.id.tvTotalFats);
        tvMealContent = findViewById(R.id.tvMealContent);
        
        actionButtons = findViewById(R.id.actionButtons);
        btnMarkAsEaten = findViewById(R.id.btnMarkAsEaten);
        btnLogDifferent = findViewById(R.id.btnLogDifferent);
        
        tabLayout = findViewById(R.id.tabLayout);
        
        // Add tabs
        for (String mealType : mealTypes) {
            tabLayout.addTab(tabLayout.newTab().setText(mealType));
        }
        
        updateDateDisplay();
    }

    private void setupListeners() {
        btnPreviousDay.setOnClickListener(v -> {
            selectedDate.add(Calendar.DAY_OF_MONTH, -1);
            updateDateDisplay();
            loadMealPlans();
        });
        
        btnNextDay.setOnClickListener(v -> {
            selectedDate.add(Calendar.DAY_OF_MONTH, 1);
            updateDateDisplay();
            loadMealPlans();
        });
        
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadMealPlan(mealTypes[tab.getPosition()]);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        
        btnMarkAsEaten.setOnClickListener(v -> markMealAsEaten());
        btnLogDifferent.setOnClickListener(v -> logDifferentMeal());
    }

    private void updateDateDisplay() {
        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        tvSelectedDate.setText(displayFormat.format(selectedDate.getTime()));
    }

    private void loadMealPlans() {
        String date = dateFormat.format(selectedDate.getTime());
        List<MealPlan> plans = mealPlanDAO.getMemberMealPlansForDate(memberId, date);
        
        // Calculate daily totals
        int totalCal = 0;
        double totalProt = 0, totalCarb = 0, totalFat = 0;
        
        for (MealPlan plan : plans) {
            for (MealPlanFood food : plan.getFoods()) {
                totalCal += (int) (food.getFood().getCalories() * food.getQuantity());
                totalProt += food.getFood().getProtein() * food.getQuantity();
                totalCarb += food.getFood().getCarbs() * food.getQuantity();
                totalFat += food.getFood().getFats() * food.getQuantity();
            }
        }
        
        tvTotalCalories.setText(String.valueOf(totalCal));
        tvTotalProtein.setText(String.format("%.1fg", totalProt));
        tvTotalCarbs.setText(String.format("%.1fg", totalCarb));
        tvTotalFats.setText(String.format("%.1fg", totalFat));
        
        // Load first tab's content
        if (tabLayout.getSelectedTabPosition() >= 0) {
            loadMealPlan(mealTypes[tabLayout.getSelectedTabPosition()]);
        } else {
            loadMealPlan(mealTypes[0]);
        }
    }

    private void loadMealPlan(String mealType) {
        String date = dateFormat.format(selectedDate.getTime());
        currentMealPlan = mealPlanDAO.getMealPlanByType(memberId, date, mealType);
        currentMealType = mealType;
        
        if (currentMealPlan != null) {
            StringBuilder content = new StringBuilder();
            
            if (currentMealPlan.getInstructions() != null && !currentMealPlan.getInstructions().isEmpty()) {
                content.append("Instructions:\n").append(currentMealPlan.getInstructions()).append("\n\n");
            }
            
            content.append("Food Items:\n\n");
            for (MealPlanFood food : currentMealPlan.getFoods()) {
                content.append("• ").append(food.getFood().getName())
                       .append(" - ").append(String.format("%.1f", food.getQuantity()))
                       .append(" ").append(food.getFood().getServingUnit())
                       .append("\n  (").append(food.getFood().getCalories()).append(" cal, ")
                       .append("P: ").append(String.format("%.1f", food.getFood().getProtein())).append("g, ")
                       .append("C: ").append(String.format("%.1f", food.getFood().getCarbs())).append("g, ")
                       .append("F: ").append(String.format("%.1f", food.getFood().getFats())).append("g)\n\n");
            }
            
            tvMealContent.setText(content.toString());
            actionButtons.setVisibility(View.VISIBLE);
        } else {
            tvMealContent.setText("No meal plan for " + mealType + " on this date.\n\nYour trainer hasn't assigned a meal plan yet.");
            actionButtons.setVisibility(View.GONE);
        }
    }
    
    private void markMealAsEaten() {
        if (currentMealPlan == null) {
            Toast.makeText(this, "No meal plan to mark", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // TODO: Implement meal logging to database
        Toast.makeText(this, currentMealType + " marked as eaten!", Toast.LENGTH_SHORT).show();
    }
    
    private void logDifferentMeal() {
        if (currentMealType == null) {
            Toast.makeText(this, "Please select a meal type first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent intent = new Intent(this, LogMealActivity.class);
        intent.putExtra("MEAL_TYPE", currentMealType);
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.nav_dashboard) {
            finish(); // Return to dashboard
        } else if (id == R.id.nav_workouts) {
            startActivity(new Intent(this, ViewWorkoutPlanActivity.class));
        } else if (id == R.id.nav_log_workout) {
            startActivity(new Intent(this, WorkoutLogActivity.class));
        } else if (id == R.id.nav_meals) {
            // Already here, just close drawer
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.nav_water) {
            startActivity(new Intent(this, WaterTrackerActivity.class));
        } else if (id == R.id.nav_logout) {
            session.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
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
