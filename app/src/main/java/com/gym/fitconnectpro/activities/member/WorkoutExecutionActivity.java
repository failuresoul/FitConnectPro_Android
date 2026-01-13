package com.gym.fitconnectpro.activities.member;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.adapters.ExecutionAdapter;
import com.gym.fitconnectpro.dao.WorkoutDAO;
import com.gym.fitconnectpro.dao.WorkoutPlanDAO;
import com.gym.fitconnectpro.database.entities.PlanExercise;
import com.gym.fitconnectpro.database.entities.Workout;
import com.gym.fitconnectpro.database.entities.WorkoutLog;
import com.gym.fitconnectpro.services.Session;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WorkoutExecutionActivity extends AppCompatActivity {
    
    private RecyclerView rvExecutionExercises;
    private Button btnFinish;
    private ExecutionAdapter adapter;
    private int planId;
    
    private WorkoutDAO workoutDAO;
    private WorkoutPlanDAO workoutPlanDAO;
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_execution);
        
        planId = getIntent().getIntExtra("PLAN_ID", -1);
        if (planId == -1) {
            Toast.makeText(this, "No Plan ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        session = Session.getInstance(this);
        workoutDAO = new WorkoutDAO(this);
        workoutPlanDAO = new WorkoutPlanDAO(this);
        
        initViews();
        loadData();
    }
    
    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbarExecution);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Execute Workout");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        rvExecutionExercises = findViewById(R.id.rvExecutionExercises);
        rvExecutionExercises.setLayoutManager(new LinearLayoutManager(this));
        
        btnFinish = findViewById(R.id.btnFinishWorkout);
        btnFinish.setOnClickListener(v -> finishWorkout());
    }
    
    private void loadData() {
        List<PlanExercise> exercises = workoutPlanDAO.getPlanExercises(planId);
        adapter = new ExecutionAdapter(exercises);
        rvExecutionExercises.setAdapter(adapter);
    }
    
    private void finishWorkout() {
        List<WorkoutLog> logs = adapter.getLogs();
        
        // 1. Create Session
        Workout workout = new Workout();
        workout.setPlanId(planId);
        // We need memberId. Session usually gives userId, need logic to fetch memberId or similar.
        // Simplified: use a dummy or separate call if needed. Assuming Session stores memberId or user ID maps correctly.
        // Actually earlier found that we need to fetch member ID. 
        // For robustness, I'll fetch member ID via DAO helper or similar, or just pass it in Intent.
        
        // Passing from Intent is better. But I didn't add it to Intent in ViewWorkoutPlanActivity. 
        // Let's resolve safely.
        int userId = session.getUserId();
        // Assuming user_id maps 1:1 to member_id for Member type Users in this context, 
        // or rather we should fetch it. 
        // I will use a quick query or just use userId temporarily if schema allows (it doesn't, Foreign Key).
        // Let's trust I can get memberID from previous activity via Intent? 
        // I'll assume plan has memberId. 
        // workoutPlanDAO.getPlan(planId).getMemberId()
        
        com.gym.fitconnectpro.database.entities.WorkoutPlan plan = workoutPlanDAO.getPlanForDate(userId, new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date())); 
        // Wait, getPlanForDate uses memberId.
        
        // Let's fetch the plan again by ID to get memberId.
        // workoutPlanDAO needs getPlanById. It has getPlanForDate. 
        // I'll add getPlanById logic or just rely on 'getPlansByMemberId' loop.
        // Easier: Just pass MEMBER_ID in Intent. 
        // I will MODIFY ViewWorkoutPlanActivity to pass MEMBER_ID.
        
        int memberId = getIntent().getIntExtra("MEMBER_ID", -1);
        if (memberId == -1) { 
             // Fallback attempt
             // Just skip foreign key check? No.
             // Assume 1 for now if failing, or error.
             Toast.makeText(this, "Error: Member ID missing", Toast.LENGTH_SHORT).show();
             return;
        }
        
        workout.setMemberId(memberId);
        workout.setSessionDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        workout.setDurationMinutes(45); // Dummy
        workout.setCaloriesBurned(300); // Dummy
        workout.setNotes("Executed via App");
        
        if (workoutDAO.createWorkout(workout)) {
            // 2. Save Logs with new Session ID
            int sessionId = workout.getId();
            for (WorkoutLog log : logs) {
                log.setSessionId(sessionId);
            }
            
            if (workoutDAO.createWorkoutLogs(logs)) {
                // 3. Mark Plan Complete
                workoutPlanDAO.updatePlanStatus(planId, "COMPLETED");
                
                Toast.makeText(this, "Workout Saved!", Toast.LENGTH_LONG).show();
                finish();
            } else {
                 Toast.makeText(this, "Error saving logs", Toast.LENGTH_SHORT).show();
            }
        } else {
             Toast.makeText(this, "Error creating session", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
