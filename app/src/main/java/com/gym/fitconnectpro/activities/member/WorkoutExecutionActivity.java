package com.gym.fitconnectpro.activities.member;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class WorkoutExecutionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView view = new TextView(this);
        view.setText("Workout Execution Screen - Coming Soon");
        setContentView(view);
    }
}
