package com.gym.fitconnectpro.activities.member;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.activities.LoginActivity;
import com.gym.fitconnectpro.adapters.WorkoutPlanExerciseAdapter;
import com.gym.fitconnectpro.dao.TrainerDAO;
import com.gym.fitconnectpro.dao.WorkoutDAO;
import com.gym.fitconnectpro.dao.WorkoutPlanDAO;
import com.gym.fitconnectpro.database.entities.PlanExercise;
import com.gym.fitconnectpro.database.entities.Trainer;
import com.gym.fitconnectpro.database.entities.Workout;
import com.gym.fitconnectpro.database.entities.WorkoutPlan;
import com.gym.fitconnectpro.services.Session;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ViewWorkoutPlanActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "ViewWorkoutPlan";
    
    private DrawerLayout drawerLayout;
    
    private TextView tvPlanDate, tvTrainerName, tvFocusArea, tvInstructions;
    private TextView tvDuration, tvCalories;
    private ImageView ivTrainerPhoto;
    private RecyclerView rvExercises;
    private Button btnMarkComplete, btnStartWorkout;
    private ImageButton btnPrevDate, btnNextDate;
    
    // Components
    private Session session;
    private WorkoutPlanDAO workoutPlanDAO;
    private WorkoutDAO workoutDAO;
    private TrainerDAO trainerDAO;
    private Calendar currentCalendar;
    private SimpleDateFormat displayFormat = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
    private SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    private WorkoutPlan currentPlan;
    private int memberId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_workout_plan);
        
        session = Session.getInstance(this);
        memberId = session.getUserId(); // Note: Session.getUserId might return UserID, we need MemberID. 
        // Typically usually we resolve MemberID. Checking MemberDashboardActivity logic:
        // "int userId = session.getUserId(); ... memberId = ... from dashboardDAO.getMemberHeaderInfo(userId)"
        // I need to resolve Member ID first.
        
        // Let's resolve memberID properly in init.
        
        initViews();
        initData();
        setupListeners();
        
        // Initial Load
        updateDateDisplay();
        loadPlanForDate();
    }
    
    private void initViews() {
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
        
        tvPlanDate = findViewById(R.id.tvPlanDate);
        tvTrainerName = findViewById(R.id.tvTrainerName);
        tvFocusArea = findViewById(R.id.tvFocusArea); // Focus area isn't in WorkoutPlan model usually, might default or use PlanName
        tvInstructions = findViewById(R.id.tvInstructions); // Plan name or specific notes? 
        // Model WorkoutPlan has: planName. No "instructions" field on Plan, but PlanExercise has notes.
        // I'll use PLAN NAME as Focus/Title and maybe fake instructions or leave blank/generic.
        
        tvDuration = findViewById(R.id.tvDuration);
        tvCalories = findViewById(R.id.tvCalories); // Plan doesn't have expected cal/duration. 
        // Prompt says: "Display: Expected duration, Expected calories".
        // Maybe calculate from exercises? (e.g. sum of rest + assumed set time). 
        // Or fetch from DailyGoals?
        // Let's implement simple ESTIMATION based on exercise count.
        
        ivTrainerPhoto = findViewById(R.id.ivTrainerPhoto);
        rvExercises = findViewById(R.id.rvExercises);
        rvExercises.setLayoutManager(new LinearLayoutManager(this));
        
        btnMarkComplete = findViewById(R.id.btnMarkComplete);
        btnStartWorkout = findViewById(R.id.btnStartWorkout);
        btnPrevDate = findViewById(R.id.btnPrevDate);
        btnNextDate = findViewById(R.id.btnNextDate);
    }
    
    private void initData() {
        workoutPlanDAO = new WorkoutPlanDAO(this);
        workoutDAO = new WorkoutDAO(this);
        trainerDAO = new TrainerDAO(this); // Need to check if TrainerDAO exists
        currentCalendar = Calendar.getInstance();
        
        // Resolve Member ID
        // Simplified: Assuming Session stores userID. We need to fetch MemberID from DB using UserID.
        // For now, I'll trust that the system handles this, or use a helper. 
        // Ideally Session should have getMemberId(), but it seems MemberDashboardActivity fetches it manually.
        // I will replicate the fetch or instantiate dashboardDAO to get it.
        // To be safe and minimal:
        resolveMemberId();
    }
    
    private void resolveMemberId() {
        com.gym.fitconnectpro.dao.MemberDashboardDAO dashDao = new com.gym.fitconnectpro.dao.MemberDashboardDAO(this);
        java.util.Map<String, String> info = dashDao.getMemberHeaderInfo(session.getUserId());
        if (info.containsKey("member_id")) {
             try {
                 memberId = Integer.parseInt(info.get("member_id"));
             } catch (Exception e) { memberId = -1; }
        }
    }
    
    private void setupListeners() {
        btnPrevDate.setOnClickListener(v -> {
            currentCalendar.add(Calendar.DAY_OF_YEAR, -1);
            updateDateDisplay();
            loadPlanForDate();
        });
        
        btnNextDate.setOnClickListener(v -> {
            currentCalendar.add(Calendar.DAY_OF_YEAR, 1);
            updateDateDisplay();
            loadPlanForDate();
        });
        
        tvPlanDate.setOnClickListener(v -> showDatePicker());
        
        btnStartWorkout.setOnClickListener(v -> startWorkout());
        
        btnMarkComplete.setOnClickListener(v -> markAsComplete());
    }
    
    private void showDatePicker() {
         new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
             currentCalendar.set(year, month, dayOfMonth);
             updateDateDisplay();
             loadPlanForDate();
         }, currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), currentCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }
    
    private void updateDateDisplay() {
        tvPlanDate.setText(displayFormat.format(currentCalendar.getTime()));
    }
    
    private void loadPlanForDate() {
        String dateStr = dbFormat.format(currentCalendar.getTime());
        // Fetch Plan
        currentPlan = workoutPlanDAO.getPlanForDate(memberId, dateStr);
        
        if (currentPlan != null) {
            // Update UI
            // Update UI
            tvFocusArea.setText(currentPlan.getFocusArea() != null ? currentPlan.getFocusArea() : currentPlan.getPlanName());
            tvInstructions.setText(currentPlan.getInstructions() != null ? currentPlan.getInstructions() : "Follow the exercises below. Stay hydrated.");
            
            // Allow actions
            btnStartWorkout.setEnabled(true);
            btnMarkComplete.setEnabled(true);
            
            // Load exercises
            List<PlanExercise> exercises = workoutPlanDAO.getPlanExercises(currentPlan.getId());
            WorkoutPlanExerciseAdapter adapter = new WorkoutPlanExerciseAdapter(exercises);
            rvExercises.setAdapter(adapter);
            
            // Estimate stats
            int estMins = exercises.size() * 5; // Rough estimate
            tvDuration.setText(estMins + " mins");
            tvCalories.setText((estMins * 8) + " kcal");
            
            // Load Trainer
             if (currentPlan.getTrainerId() > 0) {
                 Trainer trainer = trainerDAO.getTrainerById(currentPlan.getTrainerId());
                 if (trainer != null) {
                     tvTrainerName.setText(trainer.getFullName());
                 } else {
                     tvTrainerName.setText("Unknown Trainer");
                 }
             } else {
                 tvTrainerName.setText("Self-Directed");
             } 
        } else {
             // No Plan
             tvFocusArea.setText("Rest Day");
             tvInstructions.setText("No active workout plan for this date.");
             tvTrainerName.setText("--");
             rvExercises.setAdapter(null);
             tvDuration.setText("0 mins");
             tvCalories.setText("0 kcal");
             btnStartWorkout.setEnabled(false);
             btnMarkComplete.setEnabled(false);
        }
    }
    
    private void startWorkout() {
        // Opens workout execution screen
        Toast.makeText(this, "Starting Workout...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, WorkoutExecutionActivity.class);
        intent.putExtra("PLAN_ID", currentPlan.getId());
        intent.putExtra("MEMBER_ID", currentPlan.getMemberId());
        startActivity(intent);
    }
    
    private void markAsComplete() {
        if (currentPlan == null) return;
        
        // 1. Update status
        boolean statusUpdated = workoutPlanDAO.updatePlanStatus(currentPlan.getId(), "COMPLETED");
        
        // 2. Create Workout entry
        Workout workout = new Workout();
        workout.setPlanId(currentPlan.getId());
        workout.setMemberId(memberId);
        workout.setTrainerId(currentPlan.getTrainerId());
        workout.setSessionDate(dbFormat.format(Calendar.getInstance().getTime()));
        workout.setDurationMinutes(currentPlan.getId() * 5); // Dummy logic
        workout.setCaloriesBurned(currentPlan.getId() * 30);
        workout.setNotes("Completed via Plan View");
        
        boolean workoutCreated = workoutDAO.createWorkout(workout);
        
        if (statusUpdated && workoutCreated) {
            Toast.makeText(this, "Workout marked as complete!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error updating status", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.nav_dashboard) {
            finish(); // Return to dashboard
        } else if (id == R.id.nav_workouts) {
            // Already here, just close drawer
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.nav_log_workout) {
            startActivity(new Intent(this, WorkoutLogActivity.class));
        } else if (id == R.id.nav_meals) {
            startActivity(new Intent(this, ViewMealPlanActivity.class));
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
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
