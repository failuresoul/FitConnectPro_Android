package com.gym.fitconnectpro.activities.admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.adapters.TrainerAdapter;
import com.gym.fitconnectpro.dao.TrainerDAO;
import com.gym.fitconnectpro.database.entities.Trainer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TrainerManagementActivity extends AppCompatActivity implements TrainerAdapter.OnTrainerActionListener {

    private EditText etSearchTrainer;
    private Spinner spinnerStatusFilter;
    private RecyclerView rvTrainers;
    private Button btnAddNewTrainer;
    private Toolbar toolbar;

    private TrainerDAO trainerDAO;
    private TrainerAdapter adapter;
    private List<Trainer> allTrainers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_management);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();

        trainerDAO = new TrainerDAO(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllTrainers();
    }

    private void initializeViews() {
        etSearchTrainer = findViewById(R.id.etSearchTrainer);
        spinnerStatusFilter = findViewById(R.id.spinnerStatusFilter);
        rvTrainers = findViewById(R.id.rvTrainers);
        btnAddNewTrainer = findViewById(R.id.btnAddNewTrainer);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Trainer Management");
        }
    }

    private void setupRecyclerView() {
        adapter = new TrainerAdapter(this, this);
        rvTrainers.setLayoutManager(new LinearLayoutManager(this));
        rvTrainers.setAdapter(adapter);
    }

    private void setupListeners() {
        btnAddNewTrainer.setOnClickListener(v -> {
            Intent intent = new Intent(this, TrainerRegistrationActivity.class);
            startActivity(intent);
        });

        // Search Listener
        etSearchTrainer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Filter Spinner Setup
        String[] statuses = {"All", "Active", "Inactive", "On Leave"}; // "On Leave" maps to "ON_LEAVE" usually, but let's check DB check constraint. 
        // DB says: 'ACTIVE', 'INACTIVE', 'ON_LEAVE'
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatusFilter.setAdapter(spinnerAdapter);

        spinnerStatusFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterList();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadAllTrainers() {
        allTrainers = trainerDAO.getAllTrainers();
        // Populate calculating fields
        for (Trainer t : allTrainers) {
            int clientCount = trainerDAO.getAssignedClientsCount(t.getTrainerId());
            t.setAssignedClientsCount(clientCount);
        }
        filterList();
    }

    private void filterList() {
        String query = etSearchTrainer.getText().toString().toLowerCase().trim();
        String statusFilter = spinnerStatusFilter.getSelectedItem().toString();

        List<Trainer> filteredList = new ArrayList<>();

        for (Trainer t : allTrainers) {
            boolean matchesSearch = t.getFullName().toLowerCase().contains(query) ||
                                    t.getSpecialization().toLowerCase().contains(query);
            
            boolean matchesStatus = "All".equals(statusFilter) || 
                                    t.getStatus().equalsIgnoreCase(statusFilter.replace(" ", "_")); // Handle "On Leave" -> "ON_LEAVE" match sort of

            // Precise mapping
             if (!"All".equals(statusFilter)) {
                 String dbStatus = t.getStatus(); // ACTIVE, INACTIVE, ON_LEAVE
                 if ("Active".equals(statusFilter) && !"ACTIVE".equalsIgnoreCase(dbStatus)) matchesStatus = false;
                 if ("Inactive".equals(statusFilter) && !"INACTIVE".equalsIgnoreCase(dbStatus)) matchesStatus = false;
                 if ("On Leave".equals(statusFilter) && !"ON_LEAVE".equalsIgnoreCase(dbStatus)) matchesStatus = false;
             }

            if (matchesSearch && matchesStatus) {
                filteredList.add(t);
            }
        }
        adapter.setTrainers(filteredList);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --- Action Listeners ---

    @Override
    public void onEdit(Trainer trainer) {
        Toast.makeText(this, "Edit functionality coming soon for: " + trainer.getFullName(), Toast.LENGTH_SHORT).show();
        // Intent intent = new Intent(this, TrainerRegistrationActivity.class);
        // intent.putExtra("TRAINER_ID", trainer.getTrainerId());
        // startActivity(intent);
    }

    @Override
    public void onViewClients(Trainer trainer) {
        Toast.makeText(this, "View Clients for: " + trainer.getFullName(), Toast.LENGTH_SHORT).show();
        // Navigate to Client List filtered by Trainer?
    }

    @Override
    public void onDelete(Trainer trainer) {
        if (trainer.getAssignedClientsCount() > 0) {
            new AlertDialog.Builder(this)
                    .setTitle("Cannot Delete Trainer")
                    .setMessage("This trainer has " + trainer.getAssignedClientsCount() + " active clients. Reassign them before deleting.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Trainer")
                .setMessage("Are you sure you want to delete " + trainer.getFullName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean success = trainerDAO.deleteTrainer(trainer.getTrainerId());
                    if (success) {
                        Toast.makeText(this, "Trainer deleted successfully", Toast.LENGTH_SHORT).show();
                        loadAllTrainers();
                    } else {
                        Toast.makeText(this, "Failed to delete trainer", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
