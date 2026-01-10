package com.gym.fitconnectpro.activities.trainer;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.activities.LoginActivity;
import com.gym.fitconnectpro.dao.TrainerStatisticsDAO;
import com.gym.fitconnectpro.database.entities.Message;
import com.gym.fitconnectpro.services.Session;

import java.util.List;

public class TrainerDashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private android.widget.TableLayout tableRecentMessages;
    private TextView tvNoMessages;
    private Button btnCreateMealPlan;
    private TextView tvClientsCount, tvCompletedWorkouts, tvPendingPlans;
    private TextView tvTrainerNameHeader;
    private Button btnCreatePlan, btnViewClients;
    
    private Session session;
    private TrainerStatisticsDAO statsDAO;
    private int trainerId; // We need to resolve this from User ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_dashboard);

        session = Session.getInstance(this); // Assuming singleton or similar
        
        // Safety check if session is valid
        if (!session.isLoggedIn()) {
             startActivity(new Intent(this, LoginActivity.class));
             finish();
             return;
        }

        // Initialize DAO
        statsDAO = new TrainerStatisticsDAO(this);
        
        // TODO: We need to get the Trainer ID from the User ID stored in session. 
        // For now, assuming we might store it or we need a helper. 
        // Since we don't have a direct 'getTrainerId' in Session (likely just userId), 
        // we might need to look it up. But to prevent complex lookups in verify step, 
        // let's assume specific logic or use userId if they are 1:1 mapped or if Session has it.
        // OR better: query the 'trainers' table to get trainer_id where user_id = session.getUserId().
        // I will implement a quick lookup in the DAO or here? 
        // Actually, let's just use userId and hope the DAO handles it? 
        // No, DAO expects trainerId.
        // Let's assume for this task that the user ID IS the trainer ID or close enough, 
        // OR I will add a quick lookup method to `TrainerStatisticsDAO` to `getTrainerIdByUserId(int userId)`.
        
        // Let's try to get trainerId.
        int userId = session.getUserId();
        // Since I can't easily modify the DAO *again* cheaply without another tool call, 
        // I'll assume for now that I can pass userId to the DAO methods 
        // and I will update the DAO to support user_id lookup if needed, 
        // BUT looking at my DAO implementation: it expects `trainerId` (int) and queries `trainer_id` column.
        // So I *MUST* get the trainerId.
        // I will add a method to `DatabaseHelper` or `TrainerStatisticsDAO`?
        // I'll assume I can get it. For now, I'll pass userId.
        // Wait, if I pass userId as trainerId, it will fail if they are different.
        // I'll add `getTrainerIdByUserId` to `TrainerStatisticsDAO` in next step if it fails?
        // No, I should do it right.
        // I'll assume `session.getUserId()` is what we have.
        // I will use a placeholder `1` for trainerId if lookup fails in this step, but in real generic code:
        trainerId = getTrainerIdFromUserId(userId); 

        initViews();
        setupNavigation();
        loadDashboardStatistics();
        setupListeners();
    }
    
    // Placeholder helper - In real app, query DB
    private int getTrainerIdFromUserId(int userId) {
        // Simple logic: In a real scenario, we query 'trainers' table: SELECT id FROM trainers WHERE user_id = ?
        // I'll implement this query directly here to be safe and robust.
        android.database.sqlite.SQLiteDatabase db = com.gym.fitconnectpro.database.DatabaseHelper.getInstance(this).getReadableDatabase();
        android.database.Cursor cursor = null;
        int tId = -1;
        try {
            cursor = db.rawQuery("SELECT id FROM trainers WHERE user_id = ?", new String[]{String.valueOf(userId)});
            if (cursor != null && cursor.moveToFirst()) {
                tId = cursor.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }
        return tId;
    }

    private void initViews() {
        // tvWelcome is removed from binding if it was removed from XML, 
        // NO, tvWelcome was removed from XML in my previous step description? 
        // Wait, looking at XML in previous step:
        // "Welcome Section (Removed detailed text...)"
        // "Dashboard Overview" is static text.
        // So I should remove tvWelcome logic.
        
        tvClientsCount = findViewById(R.id.tvClientsCount);
        tvCompletedWorkouts = findViewById(R.id.tvCompletedWorkouts);
        tvPendingPlans = findViewById(R.id.tvPendingPlans);
        // tvMessageCount removed
        
        tableRecentMessages = findViewById(R.id.tableRecentMessages);
        tvNoMessages = findViewById(R.id.tvNoMessages);
        
        btnCreatePlan = findViewById(R.id.btnCreatePlan);
        btnCreateMealPlan = findViewById(R.id.btnCreateMealPlan);
        btnViewClients = findViewById(R.id.btnViewClients);
        // btnViewMessages removed
    }

    private void setupNavigation() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, 
                R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        
        // Setup Header
        View headerView = navigationView.getHeaderView(0);
        tvTrainerNameHeader = headerView.findViewById(R.id.tvTrainerNameHeader);
        String name = session.getFullName();
        if (name == null) name = session.getUsername();
        tvTrainerNameHeader.setText(name);
    }

    // ...

    private void loadDashboardStatistics() {
        if (trainerId == -1) {
            Toast.makeText(this, "Error: Trainer profile not found.", Toast.LENGTH_LONG).show();
            return;
        }

        int clientCount = statsDAO.getMyClientsCount(trainerId);
        int completedWorkouts = statsDAO.getTodayCompletedWorkouts(trainerId);
        int pendingPlans = statsDAO.getPendingWorkoutPlans(trainerId);
        List<Message> messages = statsDAO.getRecentMessages(trainerId, 5); // Fetch 5 for table
        
        tvClientsCount.setText(String.valueOf(clientCount));
        tvCompletedWorkouts.setText(String.valueOf(completedWorkouts));
        tvPendingPlans.setText(String.valueOf(pendingPlans));
        
        // Populate messages table
        tableRecentMessages.removeViews(2, tableRecentMessages.getChildCount() - 2); // Keep Header(0) and Line(1)
        
        if (messages.isEmpty()) {
            tvNoMessages.setVisibility(View.VISIBLE);
        } else {
            tvNoMessages.setVisibility(View.GONE);
            for (Message msg : messages) {
                android.widget.TableRow row = new android.widget.TableRow(this);
                row.setBackgroundColor(android.graphics.Color.WHITE);
                row.setPadding(0, 16, 0, 16);
                
                TextView tvFrom = new TextView(this);
                tvFrom.setText(msg.getSenderName());
                tvFrom.setTextColor(android.graphics.Color.parseColor("#34495E"));
                tvFrom.setPadding(0, 0, 16, 0);
                
                TextView tvContent = new TextView(this);
                tvContent.setText(msg.getContent());
                tvContent.setTextColor(android.graphics.Color.parseColor("#7F8C8D"));
                tvContent.setMaxLines(1);
                tvContent.setEllipsize(android.text.TextUtils.TruncateAt.END);
                
                TextView tvDate = new TextView(this);
                tvDate.setText(msg.getTimestamp()); // Simplistic, might need formatting
                tvDate.setTextColor(android.graphics.Color.parseColor("#95A5A6"));
                tvDate.setGravity(android.view.Gravity.END);
                tvDate.setTextSize(12);
                
                row.addView(tvFrom);
                row.addView(tvContent);
                row.addView(tvDate);
                
                // Set layout params for weights if needed, usually TableLayout handles stretching
                // But for fixed column widths we might need more logic XML based or LayoutParams
                // row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                
                tableRecentMessages.addView(row);
                
                // Divider
                View divider = new View(this);
                divider.setLayoutParams(new android.widget.TableRow.LayoutParams(android.widget.TableRow.LayoutParams.MATCH_PARENT, 1));
                divider.setBackgroundColor(android.graphics.Color.parseColor("#ECF0F1"));
                tableRecentMessages.addView(divider);
            }
        }
    }

    private void setupListeners() {
        btnCreatePlan.setOnClickListener(v -> Toast.makeText(this, "Create Workout Plan", Toast.LENGTH_SHORT).show());
        btnCreateMealPlan.setOnClickListener(v -> Toast.makeText(this, "Create Meal Plan", Toast.LENGTH_SHORT).show());
        btnViewClients.setOnClickListener(v -> Toast.makeText(this, "View My Clients", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_dashboard) {
            // Already here
        } else if (id == R.id.nav_clients) {
            Toast.makeText(this, "My Clients - Coming Soon", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_plans) {
            Toast.makeText(this, "Workout Plans - Coming Soon", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_messages) {
             Toast.makeText(this, "Messages - Coming Soon", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_logout) {
            session.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
             Toast.makeText(this, "Feature Coming Soon", Toast.LENGTH_SHORT).show();
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
