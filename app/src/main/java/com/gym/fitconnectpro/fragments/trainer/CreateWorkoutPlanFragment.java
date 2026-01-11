package com.gym.fitconnectpro.fragments.trainer;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.dao.TrainerDAO;
import com.gym.fitconnectpro.dao.WorkoutPlanDAO;
import com.gym.fitconnectpro.database.entities.Exercise;
import com.gym.fitconnectpro.database.entities.Member;
import com.gym.fitconnectpro.database.entities.PlanExercise;
import com.gym.fitconnectpro.database.entities.WorkoutPlan;
import com.gym.fitconnectpro.services.Session;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateWorkoutPlanFragment extends Fragment {

    private Spinner spinnerClients, spinnerExercises;
    private EditText etPlanDate, etPlanName, etSets, etReps, etWeight, etRest, etNotes;
    private RecyclerView rvPlanExercises;
    private Button btnAddExercise, btnAssignPlan, btnCancelPlan;

    private TrainerDAO trainerDAO;
    private WorkoutPlanDAO workoutPlanDAO;
    private Session session;
    private PlanExerciseAdapter adapter;
    private List<PlanExercise> addedExercises = new ArrayList<>();
    private List<Member> clientList;
    private List<Exercise> exerciseList;

    private Calendar calendar = Calendar.getInstance();

    public CreateWorkoutPlanFragment() {
        // Required empty public constructor
    }

    private static final String ARG_PRE_SELECTED_MEMBER_ID = "pre_selected_member_id";
    private int preSelectedMemberId = -1;



    public static CreateWorkoutPlanFragment newInstance() {
        return new CreateWorkoutPlanFragment();
    }

    public static CreateWorkoutPlanFragment newInstance(int memberId) {
        CreateWorkoutPlanFragment fragment = new CreateWorkoutPlanFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PRE_SELECTED_MEMBER_ID, memberId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            preSelectedMemberId = getArguments().getInt(ARG_PRE_SELECTED_MEMBER_ID, -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_workout_plan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupDependencies();
        setupListeners();
        loadData();
    }

    private TextView tvPlanTotals;
    private EditText etOverallInstructions;
    private Button btnSaveTemplate;

    // ... (existing helper methods)

    private void initViews(View view) {
        spinnerClients = view.findViewById(R.id.spinnerClients);
        spinnerExercises = view.findViewById(R.id.spinnerExercises);
        etPlanDate = view.findViewById(R.id.etPlanDate);
        etPlanName = view.findViewById(R.id.etPlanName);
        etSets = view.findViewById(R.id.etSets);
        etReps = view.findViewById(R.id.etReps);
        etWeight = view.findViewById(R.id.etWeight);
        etRest = view.findViewById(R.id.etRest);
        etNotes = view.findViewById(R.id.etNotes);
        rvPlanExercises = view.findViewById(R.id.rvPlanExercises);
        
        tvPlanTotals = view.findViewById(R.id.tvPlanTotals);
        etOverallInstructions = view.findViewById(R.id.etOverallInstructions);
        btnSaveTemplate = view.findViewById(R.id.btnSaveTemplate);
        
        btnAddExercise = view.findViewById(R.id.btnAddExercise);
        btnAssignPlan = view.findViewById(R.id.btnAssignPlan);
        btnCancelPlan = view.findViewById(R.id.btnCancelPlan);

        // Setup RecyclerView
        rvPlanExercises.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PlanExerciseAdapter(position -> {
            addedExercises.remove(position);
            adapter.setExercises(addedExercises);
            calculateTotals(); // Recalculate on remove
        });
        rvPlanExercises.setAdapter(adapter);
    }
    
    private void setupDependencies() {
        trainerDAO = new TrainerDAO(requireContext());
        workoutPlanDAO = new WorkoutPlanDAO(requireContext());
        session = Session.getInstance(requireContext());
    }

    private void setupListeners() {
        // Date Picker
        etPlanDate.setOnClickListener(v -> showDatePicker());

        // Add Exercise Button
        btnAddExercise.setOnClickListener(v -> addExercise());

        // Assign Plan Button
        btnAssignPlan.setOnClickListener(v -> assignPlan());
        
        // Save Template Button
        btnSaveTemplate.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Save as Template feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Cancel Button (same as back)
        btnCancelPlan.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        // Back Button visibility and listener
        android.widget.ImageButton btnBack = requireView().findViewById(R.id.btnBack);
        if (preSelectedMemberId != -1) {
            // Child flow - Show Back
            btnBack.setVisibility(View.VISIBLE);
            btnBack.setOnClickListener(v -> {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                }
            });
        } else {
            // First Page (Drawer/Dashboard) - Hide Back
            btnBack.setVisibility(View.GONE);
        }

        // Spinner Exercises Item Selected Listener (added as per instruction)
        spinnerExercises.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
             @Override
             public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                 // Optional: Handle item selection if needed
             }
             @Override
             public void onNothingSelected(android.widget.AdapterView<?> parent) {
                 // Optional: Handle nothing selected if needed
             }
        });
    }

    // ...



    private void calculateTotals() {
        int totalDurationMin = 0;
        int totalCalories = 0;

        for (PlanExercise ex : addedExercises) {
            // Estimate: 3 seconds per rep + rest time
            // If reps is "8-12", take 10. For now parse simple int or default
            int reps = 10; 
            try {
                reps = Integer.parseInt(ex.getReps());
            } catch (Exception e) {
                // Ignore range format for calcs
            }
            
            int setDurationSec = (reps * 3) + ex.getRestSeconds();
            int exerciseDurationSec = setDurationSec * ex.getSets();
            
            totalDurationMin += (exerciseDurationSec / 60);
        }
        
        // Approx 6 kcal/min for moderate weight training
        totalCalories = totalDurationMin * 6;
        
        tvPlanTotals.setText(String.format(Locale.US, "Est. Duration: %d min | Est. Calories: %d kcal", totalDurationMin, totalCalories));
    }

    private void loadData() {
        int trainerId = session.getUserId(); // Providing userId which is trainerId for trainer sessions

        // Load Clients
        clientList = trainerDAO.getMyAssignedClients(trainerId);
        
        // Custom Adapter to ensure full name is displayed
        ArrayAdapter<Member> clientAdapter = new ArrayAdapter<Member>(requireContext(),
                android.R.layout.simple_spinner_item, clientList) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view.findViewById(android.R.id.text1);
                if (getItem(position) != null) {
                    tv.setText(getItem(position).getFullName());
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view.findViewById(android.R.id.text1);
                if (getItem(position) != null) {
                    tv.setText(getItem(position).getFullName());
                }
                return view;
            }
        };
        clientAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClients.setAdapter(clientAdapter);
        
        if (preSelectedMemberId != -1) {
            for (int i = 0; i < clientList.size(); i++) {
                if (clientList.get(i).getMemberId() == preSelectedMemberId) {
                    spinnerClients.setSelection(i);
                    spinnerClients.setEnabled(false); // Lock selection for direct assignment
                    break;
                }
            }
        }

        // Load Exercises
        exerciseList = workoutPlanDAO.getAllExercises();
        ArrayAdapter<Exercise> exerciseAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, exerciseList);
        exerciseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExercises.setAdapter(exerciseAdapter);
    }

    private void showDatePicker() {
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateLabel();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateLabel() {
        String myFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        etPlanDate.setText(sdf.format(calendar.getTime()));
    }

    private void addExercise() {
        if (spinnerExercises.getSelectedItem() == null) {
            Toast.makeText(getContext(), "Please select an exercise", Toast.LENGTH_SHORT).show();
            return;
        }

        String setsStr = etSets.getText().toString();
        String repsStr = etReps.getText().toString();
        String weightStr = etWeight.getText().toString(); // Optional
        String restStr = etRest.getText().toString(); // Optional

        if (TextUtils.isEmpty(setsStr) || TextUtils.isEmpty(repsStr)) {
            Toast.makeText(getContext(), "Sets and Reps are required", Toast.LENGTH_SHORT).show();
            return;
        }

        PlanExercise planExercise = new PlanExercise();
        planExercise.setExercise((Exercise) spinnerExercises.getSelectedItem());
        planExercise.setSets(Integer.parseInt(setsStr));
        planExercise.setReps(repsStr);
        try {
            planExercise.setWeightKg(TextUtils.isEmpty(weightStr) ? 0 : Double.parseDouble(weightStr));
            planExercise.setRestSeconds(TextUtils.isEmpty(restStr) ? 0 : Integer.parseInt(restStr));
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
            return;
        }
        planExercise.setNotes(etNotes.getText().toString());

        addedExercises.add(planExercise);
        adapter.setExercises(addedExercises);

        // Clear exercise specific inputs
        etSets.setText("");
        etReps.setText("");
        etWeight.setText("");
        etRest.setText("");
        etNotes.setText("");
        Toast.makeText(getContext(), "Exercise Added", Toast.LENGTH_SHORT).show();
    }

    private void assignPlan() {
        if (spinnerClients.getSelectedItem() == null) {
            Toast.makeText(getContext(), "Please select a client", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(etPlanDate.getText())) {
            Toast.makeText(getContext(), "Please select a start date", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(etPlanName.getText())) {
            Toast.makeText(getContext(), "Please enter a plan name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (addedExercises.isEmpty()) {
            Toast.makeText(getContext(), "Please add at least one exercise", Toast.LENGTH_SHORT).show();
            return;
        }

        Member selectedClient = (Member) spinnerClients.getSelectedItem();
        
        WorkoutPlan plan = new WorkoutPlan();
        plan.setTrainerId(session.getUserId());
        plan.setMemberId(selectedClient.getMemberId());
        plan.setPlanName(etPlanName.getText().toString());
        plan.setStartDate(etPlanDate.getText().toString());
        plan.setEndDate(etPlanDate.getText().toString()); // For now same as start, or could calculate duration
        plan.setStatus("ACTIVE");

        boolean success = workoutPlanDAO.createWorkoutPlan(plan, addedExercises);

        if (success) {
            Toast.makeText(getContext(), "Workout Plan Assigned Successfully!", Toast.LENGTH_LONG).show();
            
            // Navigate to the client's plans
            // If we came from ClientPlansFragment (preSelectedMemberId != -1), we can just pop back.
            // If we came from Dashboard (preSelectedMemberId == -1), we MUST navigate to ClientPlansFragment explicitly.
            
            if (preSelectedMemberId != -1) {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                }
            } else {
                 // Open ClientPlansFragment for the selected member
                ClientPlansFragment plansFragment = ClientPlansFragment.newInstance(selectedClient.getMemberId());
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, plansFragment)
                        .addToBackStack(null) // Allow back to Dashboard/Create
                        .commit();
            }
        } else {
            Toast.makeText(getContext(), "Failed to create plan", Toast.LENGTH_SHORT).show();
        }
    }
}
