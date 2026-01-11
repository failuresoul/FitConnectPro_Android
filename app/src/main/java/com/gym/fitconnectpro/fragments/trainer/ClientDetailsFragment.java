package com.gym.fitconnectpro.fragments.trainer;

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

import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.dao.TrainerDAO;
import com.gym.fitconnectpro.database.entities.Member;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ClientDetailsFragment extends Fragment {

    private static final String ARG_MEMBER_ID = "member_id";
    private int memberId;
    private TrainerDAO trainerDAO;
    
    private TextView tvName, tvEmail, tvGender, tvAge, tvStatus, tvHeightWeight, tvMedical, tvPhone, tvEmergency;

    public ClientDetailsFragment() {
        // Required empty public constructor
    }

    public static ClientDetailsFragment newInstance(int memberId) {
        ClientDetailsFragment fragment = new ClientDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MEMBER_ID, memberId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            memberId = getArguments().getInt(ARG_MEMBER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            return inflater.inflate(R.layout.fragment_client_details, container, false);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error Inflating View: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return new View(getContext()); // Return dummy view to prevent crash
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        try {
            tvName = view.findViewById(R.id.tvDetailName);
            tvEmail = view.findViewById(R.id.tvDetailEmail);
            tvGender = view.findViewById(R.id.tvDetailGender);
            tvAge = view.findViewById(R.id.tvDetailAge);
            tvStatus = view.findViewById(R.id.tvDetailStatus);
            tvHeightWeight = view.findViewById(R.id.tvDetailHeightWeight);
            tvMedical = view.findViewById(R.id.tvDetailMedical);
            tvPhone = view.findViewById(R.id.tvDetailPhone);
            tvEmergency = view.findViewById(R.id.tvDetailEmergency);
            
            android.widget.ImageButton btnBack = view.findViewById(R.id.btnBack);
            btnBack.setOnClickListener(v -> {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                }
            });
            
            trainerDAO = new TrainerDAO(requireContext());
            loadClientDetails();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error in ViewCreated: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void loadClientDetails() {
        try {
            Member member = trainerDAO.getClientDetails(memberId);
            if (member != null) {
                if (tvName != null) tvName.setText(member.getFullName());
                if (tvEmail != null) tvEmail.setText(member.getEmail());
                if (tvGender != null) tvGender.setText(member.getGender());
                if (tvStatus != null) tvStatus.setText(member.getStatus());
                if (tvHeightWeight != null) tvHeightWeight.setText("Height: " + member.getHeight() + " cm | Weight: " + member.getWeight() + " kg");
                if (tvMedical != null) tvMedical.setText(member.getMedicalNotes() != null ? member.getMedicalNotes() : "None");
                if (tvPhone != null) tvPhone.setText("Phone: " + member.getPhone());
                if (tvEmergency != null) tvEmergency.setText("Emergency: " + (member.getEmergencyContact() != null ? member.getEmergencyContact() : "N/A"));
                
                // Age calc simplified or use DOB directly
                if (tvAge != null) tvAge.setText(member.getDateOfBirth() != null ? member.getDateOfBirth() : "N/A");
                
            } else {
                Toast.makeText(getContext(), "Error loading client details: Member not found", Toast.LENGTH_SHORT).show();
                // Go back safely
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error loading details: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
