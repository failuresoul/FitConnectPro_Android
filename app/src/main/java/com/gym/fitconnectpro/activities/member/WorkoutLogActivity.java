package com.gym.fitconnectpro.activities.member;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.adapters.WorkoutSessionAdapter;
import com.gym.fitconnectpro.dao.MemberDashboardDAO;
import com.gym.fitconnectpro.dao.WorkoutDAO;
import com.gym.fitconnectpro.models.WorkoutSession;
import com.gym.fitconnectpro.services.Session;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WorkoutLogActivity extends AppCompatActivity {
    private RecyclerView rvWorkoutSessions;
    private LinearLayout emptyState;
    private WorkoutDAO workoutDAO;
    private MemberDashboardDAO memberDashboardDAO;
    private Session session;
    private int memberId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_log);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize
        session = Session.getInstance(this);
        workoutDAO = new WorkoutDAO(this);
        memberDashboardDAO = new MemberDashboardDAO(this);
        
        // Get member ID from database using user_id
        int userId = session.getUserId();
        Map<String, String> memberInfo = memberDashboardDAO.getMemberHeaderInfo(userId);
        if (memberInfo != null && memberInfo.containsKey("member_id")) {
            memberId = Integer.parseInt(memberInfo.get("member_id"));
        } else {
            memberId = 0;
        }

        rvWorkoutSessions = findViewById(R.id.rvWorkoutSessions);
        emptyState = findViewById(R.id.emptyState);

        rvWorkoutSessions.setLayoutManager(new LinearLayoutManager(this));

        loadTodayWorkouts();
    }

    private void loadTodayWorkouts() {
        // Get today's date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = dateFormat.format(new Date());

        try {
            // Query workout sessions for today
            List<WorkoutSession> sessions = workoutDAO.getTodayWorkoutSessions(memberId, today);

            if (sessions != null && !sessions.isEmpty()) {
                // Show RecyclerView, hide empty state
                rvWorkoutSessions.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);

                // Set up adapter
                WorkoutSessionAdapter adapter = new WorkoutSessionAdapter(sessions);
                rvWorkoutSessions.setAdapter(adapter);
            } else {
                // Show empty state, hide RecyclerView
                rvWorkoutSessions.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Show empty state on error
            rvWorkoutSessions.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        }
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
