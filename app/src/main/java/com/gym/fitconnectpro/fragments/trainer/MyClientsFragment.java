package com.gym.fitconnectpro.fragments.trainer;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.dao.TrainerDAO;
import com.gym.fitconnectpro.database.entities.Member;

import java.util.ArrayList;
import java.util.List;

public class MyClientsFragment extends Fragment implements MyClientsAdapter.ClientActionListener {

    private static final String TAG = "MyClientsFragment";
    private RecyclerView rvMyClients;
    private MyClientsAdapter adapter;
    private androidx.appcompat.widget.SearchView searchView;
    private TextView tvEmptyState;
    private TrainerDAO trainerDAO;
    private int trainerId = -1; // Should be retrieved from session

    public MyClientsFragment() {
        // Required empty public constructor
    }

    public static MyClientsFragment newInstance(int trId) {
        MyClientsFragment fragment = new MyClientsFragment();
        Bundle args = new Bundle();
        args.putInt("TRAINER_ID", trId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            trainerId = getArguments().getInt("TRAINER_ID", -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_clients, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvMyClients = view.findViewById(R.id.rvMyClients);
        searchView = view.findViewById(R.id.searchViewClients);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        
        android.widget.ImageButton btnBack = view.findViewById(R.id.btnBack);
        // "First page for navigation drawer" -> No back button needed
        btnBack.setVisibility(View.GONE);
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });
        
        trainerDAO = new TrainerDAO(requireContext());

        setupRecyclerView();
        loadClients();
        
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.getFilter().filter(newText);
                }
                return true;
            }
        });
    }
    
    // Method to set trainer ID externally if needed (e.g. from Activity)
    public void setTrainerId(int id) {
        this.trainerId = id;
        if (isAdded()) {
            loadClients();
        }
    }

    private void setupRecyclerView() {
        rvMyClients.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MyClientsAdapter(getContext(), new ArrayList<>(), this);
        rvMyClients.setAdapter(adapter);
    }

    private void loadClients() {
        if (trainerId == -1) {
            tvEmptyState.setText("Trainer session error. ID: " + trainerId);
            tvEmptyState.setVisibility(View.VISIBLE);
            return;
        }

        List<Member> clients = trainerDAO.getMyAssignedClients(trainerId); // Assuming getMyAssignedClients takes trainer DB ID.
                                                                        // Check if trainerId matches what DAO expects (trainers.id)
        if (clients.isEmpty()) {
            rvMyClients.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvMyClients.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
            adapter.updateList(clients);
        }
    }

    @Override
    public void onViewProfile(Member client) {
        try {
            // Navigate to ClientDetailsFragment
            ClientDetailsFragment detailsFragment = ClientDetailsFragment.newInstance(client.getMemberId());
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, detailsFragment)
                        .addToBackStack(null)
                        .commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error opening profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreatePlan(Member client) {
        // Navigate to ClientPlansFragment instead of direct creation
        ClientPlansFragment plansFragment = ClientPlansFragment.newInstance(client.getMemberId());
        if (getParentFragmentManager() != null) {
             getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, plansFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onMessage(Member client) {
        // Simple Message Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Message " + client.getFullName());
        builder.setMessage("This feature is coming soon!");
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
