package com.gym.fitconnectpro.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.activities.LoginActivity;
import com.gym.fitconnectpro.fragments.admin.ApplicationsFragment;
import com.gym.fitconnectpro.fragments.admin.DashboardHomeFragment;
import com.gym.fitconnectpro.fragments.admin.MemberManagementFragment;
import com.gym.fitconnectpro.fragments.admin.ReportsFragment;
import com.gym.fitconnectpro.fragments.admin.SalaryManagementFragment;
import com.gym.fitconnectpro.fragments.admin.TrainerManagementFragment;
import com.gym.fitconnectpro.services.Session;

public class AdminDashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Session session;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView toolbarTitle;
    private Button btnLogout;
    private TextView headerAdminName, headerAdminEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        session = Session.getInstance(this);

        // Check if user is logged in
        if (!session.isLoggedIn() || !"ADMIN".equals(session.getUserType())) {
            redirectToLogin();
            return;
        }

        initializeViews();
        setupNavigation();
        setupListeners();

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new DashboardHomeFragment());
            navigationView.setCheckedItem(R.id.nav_dashboard);
            toolbarTitle.setText("Admin Dashboard");
        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        toolbarTitle = findViewById(R.id.toolbar_title);
        btnLogout = findViewById(R.id.btnLogout);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Set toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Initialize Header Views
        if (navigationView.getHeaderCount() > 0) {
            android.view.View headerView = navigationView.getHeaderView(0);
            headerAdminName = headerView.findViewById(R.id.tvAdminName);
            headerAdminEmail = headerView.findViewById(R.id.tvAdminEmail);

            String username = session.getUsername();
            if (username != null) {
                headerAdminName.setText(username);
                // In a real app, email would also be in session or fetched
                headerAdminEmail.setText(username + "@fitconnectpro.com"); // Placeholder
            }
        }

        // Handle Back Press
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void setupNavigation() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_dashboard) {
            loadFragment(new DashboardHomeFragment());
            toolbarTitle.setText("Admin Dashboard");
        } else if (id == R.id.nav_members) {
            loadMemberManagement();
        } else if (id == R.id.nav_trainers) {
            loadTrainerManagement();
        } else if (id == R.id.nav_salary) {
            loadSalaryManagement();
        } else if (id == R.id.nav_reports) {
            loadReports();
        } else if (id == R.id.nav_logout) {
            showLogoutDialog();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content_area, fragment);
        transaction.commit();
    }

    public void loadMemberManagement() {
        loadFragment(new MemberManagementFragment());
        toolbarTitle.setText("Member Management");
    }

    public void loadTrainerManagement() {
        loadFragment(new TrainerManagementFragment());
        toolbarTitle.setText("Trainer Management");
    }

    public void loadSalaryManagement() {
        loadFragment(new SalaryManagementFragment());
        toolbarTitle.setText("Salary Management");
    }

    public void loadReports() {
        loadFragment(new ReportsFragment());
        toolbarTitle.setText("Reports");
    }

    public void loadApplications() {
        loadFragment(new ApplicationsFragment());
        toolbarTitle.setText("Applications");
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes", (dialog, which) -> logout())
            .setNegativeButton("No", null)
            .show();
    }

    private void logout() {
        session.logout();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}
