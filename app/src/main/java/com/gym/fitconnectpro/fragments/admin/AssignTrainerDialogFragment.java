package com.gym.fitconnectpro.fragments.admin;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.dao.MemberDAO;
import com.gym.fitconnectpro.dao.TrainerDAO;
import com.gym.fitconnectpro.database.entities.Member;
import com.gym.fitconnectpro.database.entities.Trainer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AssignTrainerDialogFragment extends DialogFragment {

    private Spinner spinnerMember, spinnerTrainer;
    private DatePicker datePickerAssignment;
    private Button btnAssign, btnCancel;

    private MemberDAO memberDAO;
    private TrainerDAO trainerDAO;

    private List<Member> memberList;
    private List<Trainer> trainerList;
    private ArrayAdapter<String> memberAdapter;
    private ArrayAdapter<String> trainerAdapter;

    private OnAssignmentCompleteListener listener;

    public interface OnAssignmentCompleteListener {
        void onAssignmentComplete();
    }

    public void setOnAssignmentCompleteListener(OnAssignmentCompleteListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        View view = onCreateView(LayoutInflater.from(getContext()), null, savedInstanceState);
        builder.setView(view);
        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_assign_trainer, container, false);

        // Initialize DAOs
        memberDAO = new MemberDAO(requireContext());
        trainerDAO = new TrainerDAO(requireContext());

        // Initialize views
        spinnerMember = view.findViewById(R.id.spinnerMember);
        spinnerTrainer = view.findViewById(R.id.spinnerTrainer);
        datePickerAssignment = view.findViewById(R.id.datePickerAssignment);
        btnAssign = view.findViewById(R.id.btnAssign);
        btnCancel = view.findViewById(R.id.btnCancel);

        // Load data
        loadMembers();
        loadTrainers();

        // Setup listeners
        setupListeners();

        return view;
    }

    private void loadMembers() {
        memberList = memberDAO.getAllActiveMembers();
        List<String> memberNames = new ArrayList<>();

        if (memberList.isEmpty()) {
            memberNames.add("No members available");
        } else {
            for (Member member : memberList) {
                // Check if member already has a trainer
                Integer assignedTrainerId = memberDAO.getAssignedTrainerId(member.getMemberId());
                
                String displayText = member.getFullName();
                if (assignedTrainerId != null) {
                    displayText += " [Reassign]";
                }
                
                memberNames.add(displayText);
            }
        }

        memberAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, memberNames);
        memberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMember.setAdapter(memberAdapter);
    }

    private void loadTrainers() {
        trainerList = trainerDAO.getAvailableTrainers();
        List<String> trainerNames = new ArrayList<>();

        if (trainerList.isEmpty()) {
            trainerNames.add("No trainers available");
        } else {
            for (Trainer trainer : trainerList) {
                String displayText = trainer.getFullName() + 
                                   " (Clients: " + trainer.getAssignedClientsCount() + ")";
                trainerNames.add(displayText);
            }
        }

        trainerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, trainerNames);
        trainerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTrainer.setAdapter(trainerAdapter);
    }

    private void setupListeners() {
        btnAssign.setOnClickListener(v -> handleAssign());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void handleAssign() {
        // Validate selections
        if (memberList.isEmpty() || trainerList.isEmpty()) {
            Toast.makeText(requireContext(), 
                "No members or trainers available for assignment", 
                Toast.LENGTH_SHORT).show();
            return;
        }

        int memberPosition = spinnerMember.getSelectedItemPosition();
        int trainerPosition = spinnerTrainer.getSelectedItemPosition();

        if (memberPosition < 0 || trainerPosition < 0) {
            Toast.makeText(requireContext(), 
                "Please select both member and trainer", 
                Toast.LENGTH_SHORT).show();
            return;
        }

        Member selectedMember = memberList.get(memberPosition);
        Trainer selectedTrainer = trainerList.get(trainerPosition);

        // Get selected date
        int day = datePickerAssignment.getDayOfMonth();
        int month = datePickerAssignment.getMonth();
        int year = datePickerAssignment.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String assignedDate = dateFormat.format(calendar.getTime());

        // Check if member already has an active assignment
        Integer existingTrainerId = memberDAO.getAssignedTrainerId(selectedMember.getMemberId());
        
        if (existingTrainerId != null && existingTrainerId.equals(selectedTrainer.getTrainerId())) {
            Toast.makeText(requireContext(), 
                "Member is already assigned to this trainer", 
                Toast.LENGTH_SHORT).show();
            return;
        }

        // Perform assignment
        boolean success = trainerDAO.assignTrainerToMember(
            selectedTrainer.getTrainerId(),
            selectedMember.getMemberId(),
            assignedDate
        );

        if (success) {
            String message = existingTrainerId != null 
                ? "Trainer reassigned successfully" 
                : "Trainer assigned successfully";
            
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            
            // Notify listener
            if (listener != null) {
                listener.onAssignmentComplete();
            }
            
            dismiss();
        } else {
            Toast.makeText(requireContext(), 
                "Failed to assign trainer. Please try again.", 
                Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}
