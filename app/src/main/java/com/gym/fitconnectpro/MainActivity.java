package com.gym.fitconnectpro;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gym.fitconnectpro.activities.LoginActivity;
import com.gym.fitconnectpro.activities.admin.AdminDashboardActivity;
import com.gym.fitconnectpro.database.DatabaseHelper;
import com.gym.fitconnectpro.services.Session;

/**
 * Main Activity - Launcher Activity
 * Initializes the database and redirects to appropriate screen
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int SPLASH_DELAY = 2000; // 2 seconds splash screen

    private DatabaseHelper databaseHelper;
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            // Set activity properties
            setTitle(R.string.app_name);

            // Lock orientation to portrait (optional)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            // Initialize session
            session = Session.getInstance(this);

            // Initialize database in background thread
            new Thread(new Runnable() {
                @Override
                public void run() {
                    initializeDatabase();
                }
            }).start();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error initializing application", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * Initialize database and create default admin user
     */
    private void initializeDatabase() {
        try {
            Log.d(TAG, "Initializing database...");

            // Get database instance (this will create the database if it doesn't exist)
            databaseHelper = DatabaseHelper.getInstance(this);

            // Force database creation/opening
            databaseHelper.getWritableDatabase();

            Log.d(TAG, "Database initialized successfully");

            // Delay for splash screen effect, then navigate
            new Handler(getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    navigateToNextScreen();
                }
            }, SPLASH_DELAY);

        } catch (Exception e) {
            Log.e(TAG, "Error initializing database", e);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,
                        "Database initialization failed. Please restart the app.",
                        Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        }
    }

    /**
     * Navigate to appropriate screen based on session state
     */
    private void navigateToNextScreen() {
        Intent intent;

        // Always navigate to Login Activity (Auto-login disabled for testing)
        Log.d(TAG, "Navigating to LoginActivity");
        intent = new Intent(MainActivity.this, LoginActivity.class);

        // Start next activity and finish this one
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up resources if needed
    }
}