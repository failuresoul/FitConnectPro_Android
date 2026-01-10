package com.gym.fitconnectpro.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.adapters.MemberAdapter;
import com.gym.fitconnectpro.dao.MemberDAO;
import com.gym.fitconnectpro.database.entities.Member;

import java.util.ArrayList;
import java.util.List;

public class MemberManagementActivity extends AppCompatActivity implements MemberAdapter.OnMemberActionListener {

    private static final String TAG = "MemberManagement";

    private TextInputEditText etSearch;
    private AutoCompleteTextView spinnerFilter;
    private RecyclerView recyclerViewMembers;
    private TextView tvMemberCount, tvPageNumber;
    private Button btnPrevious, btnNext;

    private MemberAdapter memberAdapter;
    private MemberDAO memberDAO;
    private List<Member> allMembers;
    private List<Member> filteredMembers;

    private static final int ITEMS_PER_PAGE = 10;
    private int currentPage = 1;
    private int totalPages = 1;
    private String currentFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_management);

        Log.d(TAG, "MemberManagementActivity started");

        initialize();
        setupToolbar();
        setupRecyclerView();
        setupSearchAndFilter();
        setupPagination();
        loadAllMembers();
    }

    private void initialize() {
        etSearch = findViewById(R.id.etSearch);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        recyclerViewMembers = findViewById(R.id.recyclerViewMembers);
        tvMemberCount = findViewById(R.id.tvMemberCount);
        tvPageNumber = findViewById(R.id.tvPageNumber);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);

        memberDAO = new MemberDAO(this);
        allMembers = new ArrayList<>();
        filteredMembers = new ArrayList<>();

        Log.d(TAG, "Views initialized");
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Member Management");
        }
        Log.d(TAG, "Toolbar setup complete");
    }

    private void setupRecyclerView() {
        memberAdapter = new MemberAdapter(this);
        recyclerViewMembers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMembers.setAdapter(memberAdapter);
        Log.d(TAG, "RecyclerView setup complete");
    }

    private void setupSearchAndFilter() {
        // Setup search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handleSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Setup filter dropdown
        String[] filterOptions = {"All", "Active", "Suspended", "Expired"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, filterOptions);
        spinnerFilter.setAdapter(adapter);

        spinnerFilter.setOnItemClickListener((parent, view, position, id) -> {
            currentFilter = filterOptions[position];
            handleFilter(currentFilter);
        });

        Log.d(TAG, "Search and filter setup complete");
    }

    private void setupPagination() {
        btnPrevious.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                updatePageDisplay();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentPage < totalPages) {
                currentPage++;
                updatePageDisplay();
            }
        });

        Log.d(TAG, "Pagination setup complete");
    }

    private void loadAllMembers() {
        Log.d(TAG, "Loading all members from database...");

        try {
            allMembers = memberDAO.getAllMembers();
            Log.d(TAG, "Successfully loaded " + allMembers.size() + " members from database");

            if (allMembers.isEmpty()) {
                Log.w(TAG, "No members found in database");
                Toast.makeText(this, "No members found. Please register members first.", Toast.LENGTH_LONG).show();
            } else {
                for (Member member : allMembers) {
                    Log.d(TAG, "Member: " + member.getFullName() + " - Status: " + member.getStatus());
                }
            }

            filteredMembers = new ArrayList<>(allMembers);
            updateMemberCount();
            calculatePagination();
            updatePageDisplay();
        } catch (Exception e) {
            Log.e(TAG, "Error loading members: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading members: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSearch(String query) {
        Log.d(TAG, "Searching for: " + query);

        if (query.isEmpty()) {
            filteredMembers = new ArrayList<>(allMembers);
        } else {
            filteredMembers = memberDAO.searchMembers(query);
            Log.d(TAG, "Search results: " + filteredMembers.size() + " members");
        }

        currentPage = 1;
        calculatePagination();
        updatePageDisplay();
        updateMemberCount();
    }

    private void handleFilter(String status) {
        Log.d(TAG, "Filtering by status: " + status);

        if ("All".equals(status)) {
            filteredMembers = new ArrayList<>(allMembers);
        } else {
            filteredMembers = memberDAO.filterMembersByStatus(status);
            Log.d(TAG, "Filter results: " + filteredMembers.size() + " members");
        }

        currentPage = 1;
        calculatePagination();
        updatePageDisplay();
        updateMemberCount();
    }

    private void calculatePagination() {
        totalPages = (int) Math.ceil((double) filteredMembers.size() / ITEMS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;
        Log.d(TAG, "Calculated " + totalPages + " pages");
    }

    private void updatePageDisplay() {
        int startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, filteredMembers.size());

        List<Member> pageMembers;
        if (filteredMembers.isEmpty()) {
            pageMembers = new ArrayList<>();
            Log.d(TAG, "No members to display");
        } else {
            pageMembers = filteredMembers.subList(startIndex, endIndex);
            Log.d(TAG, "Displaying " + pageMembers.size() + " members on page " + currentPage);
        }

        memberAdapter.setMembers(pageMembers);

        tvPageNumber.setText("Page " + currentPage + " of " + totalPages);
        btnPrevious.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPages);
    }

    private void updateMemberCount() {
        String countText = "Total Members: " + filteredMembers.size();
        tvMemberCount.setText(countText);
        Log.d(TAG, countText);
    }

    private void refreshList() {
        Log.d(TAG, "Refreshing member list...");
        loadAllMembers();
    }

    @Override
    public void onEdit(Member member) {
        Log.d(TAG, "Edit clicked for member: " + member.getFullName());
        Intent intent = new Intent(this, MemberRegistrationActivity.class);
        intent.putExtra("MEMBER_ID", member.getMemberId());
        intent.putExtra("EDIT_MODE", true);
        startActivity(intent);
    }

    @Override
    public void onSuspend(Member member) {
        Log.d(TAG, "Suspend/Activate clicked for member: " + member.getFullName());
        String newStatus = "Active".equals(member.getStatus()) ? "Suspended" : "Active";
        String message = "Active".equals(member.getStatus()) ?
                "Are you sure you want to suspend this member?" :
                "Are you sure you want to activate this member?";

        new AlertDialog.Builder(this)
                .setTitle("Confirm Action")
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> {
                    boolean success = memberDAO.updateMemberStatus(member.getMemberId(), newStatus);
                    if (success) {
                        Toast.makeText(this, "Member status updated successfully", Toast.LENGTH_SHORT).show();
                        refreshList();
                    } else {
                        Toast.makeText(this, "Failed to update member status", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDelete(Member member) {
        Log.d(TAG, "Delete clicked for member: " + member.getFullName());
        new AlertDialog.Builder(this)
                .setTitle("Delete Member")
                .setMessage("Are you sure you want to delete " + member.getFullName() + "? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean success = memberDAO.deleteMember(member.getMemberId());
                    if (success) {
                        Toast.makeText(this, "Member deleted successfully", Toast.LENGTH_SHORT).show();
                        refreshList();
                    } else {
                        Toast.makeText(this, "Failed to delete member", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Activity resumed, refreshing list");
        refreshList();
    }
}
