package com.gym.fitconnectpro.activities.member;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.adapters.WaterLogAdapter;
import com.gym.fitconnectpro.dao.MemberDashboardDAO;
import com.gym.fitconnectpro.dao.WaterLogDAO;
import com.gym.fitconnectpro.models.WaterLog;
import com.gym.fitconnectpro.services.Session;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WaterTrackerActivity extends AppCompatActivity implements WaterLogAdapter.OnDeleteClickListener {
    private ProgressBar progressWater;
    private TextView tvCurrentAmount, tvGoalAmount, tvPercentage, tvGoalInfo;
    private Button btn250ml, btn500ml, btn750ml, btnAddCustom;
    private EditText etCustomAmount;
    private RecyclerView rvWaterLogs;
    
    private WaterLogDAO waterLogDAO;
    private MemberDashboardDAO memberDashboardDAO;
    private WaterLogAdapter adapter;
    private Session session;
    
    private int memberId;
    private int waterGoal = 2500; // Default goal
    private int currentAmount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_tracker);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize
        session = Session.getInstance(this);
        waterLogDAO = new WaterLogDAO(this);
        memberDashboardDAO = new MemberDashboardDAO(this);

        // Get member ID
        int userId = session.getUserId();
        Map<String, String> memberInfo = memberDashboardDAO.getMemberHeaderInfo(userId);
        if (memberInfo != null && memberInfo.containsKey("member_id")) {
            memberId = Integer.parseInt(memberInfo.get("member_id"));
        }

        initViews();
        loadWaterGoal();
        loadTodayData();
        setupListeners();
    }

    private void initViews() {
        progressWater = findViewById(R.id.progressWater);
        tvCurrentAmount = findViewById(R.id.tvCurrentAmount);
        tvGoalAmount = findViewById(R.id.tvGoalAmount);
        tvPercentage = findViewById(R.id.tvPercentage);
        tvGoalInfo = findViewById(R.id.tvGoalInfo);
        
        btn250ml = findViewById(R.id.btn250ml);
        btn500ml = findViewById(R.id.btn500ml);
        btn750ml = findViewById(R.id.btn750ml);
        btnAddCustom = findViewById(R.id.btnAddCustom);
        etCustomAmount = findViewById(R.id.etCustomAmount);
        
        rvWaterLogs = findViewById(R.id.rvWaterLogs);
        rvWaterLogs.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadWaterGoal() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String today = dateFormat.format(new Date());
            
            Map<String, Object> goals = memberDashboardDAO.getTodayGoals(memberId, today);
            if (goals != null && goals.containsKey("water_target")) {
                waterGoal = (Integer) goals.get("water_target");
                if (waterGoal <= 0) waterGoal = 2500;
            }
        } catch (Exception e) {
            waterGoal = 2500;
        }
        
        tvGoalAmount.setText(waterGoal + "ml");
        tvGoalInfo.setText("Goal set by Trainer: " + waterGoal + "ml");
        progressWater.setMax(100);
    }

    private void loadTodayData() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = dateFormat.format(new Date());
        
        // Get total
        currentAmount = waterLogDAO.getTodayWaterTotal(memberId, today);
        updateUI();
        
        // Get logs
        List<WaterLog> logs = waterLogDAO.getTodayLogs(memberId, today);
        adapter = new WaterLogAdapter(logs, this);
        rvWaterLogs.setAdapter(adapter);
    }

    private void setupListeners() {
        btn250ml.setOnClickListener(v -> addWater(250));
        btn500ml.setOnClickListener(v -> addWater(500));
        btn750ml.setOnClickListener(v -> addWater(750));
        
        btnAddCustom.setOnClickListener(v -> {
            String amountStr = etCustomAmount.getText().toString();
            if (!TextUtils.isEmpty(amountStr)) {
                try {
                    int amount = Integer.parseInt(amountStr);
                    if (amount > 0 && amount <= 5000) {
                        addWater(amount);
                        etCustomAmount.setText("");
                    } else {
                        Toast.makeText(this, "Please enter amount between 1-5000ml", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addWater(int amount) {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentTime = dateTimeFormat.format(new Date());
        
        boolean success = waterLogDAO.logWater(memberId, amount, currentTime);
        if (success) {
            currentAmount += amount;
            updateUI();
            loadTodayData(); // Refresh logs
            
            // Check if goal achieved
            if (currentAmount >= waterGoal && (currentAmount - amount) < waterGoal) {
                showGoalAchievedDialog();
            }
            
            Toast.makeText(this, "Added " + amount + "ml", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to add water", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI() {
        tvCurrentAmount.setText(currentAmount + "ml");
        
        int percentage = (int) ((currentAmount * 100.0) / waterGoal);
        if (percentage > 100) percentage = 100;
        
        tvPercentage.setText(percentage + "%");
        progressWater.setProgress(percentage);
    }

    private void showGoalAchievedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("🎉 Congratulations!")
                .setMessage("You've achieved your daily water intake goal of " + waterGoal + "ml!")
                .setPositiveButton("Great!", null)
                .show();
    }

    @Override
    public void onDeleteClick(WaterLog log) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Log")
                .setMessage("Delete this water log entry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean success = waterLogDAO.deleteWaterLog(log.getLogId());
                    if (success) {
                        currentAmount -= log.getAmountMl();
                        if (currentAmount < 0) currentAmount = 0;
                        updateUI();
                        loadTodayData();
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
