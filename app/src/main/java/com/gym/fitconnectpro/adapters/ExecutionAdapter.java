package com.gym.fitconnectpro.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.database.entities.PlanExercise;
import com.gym.fitconnectpro.database.entities.WorkoutLog;

import java.util.ArrayList;
import java.util.List;

public class ExecutionAdapter extends RecyclerView.Adapter<ExecutionAdapter.ViewHolder> {

    private List<PlanExercise> planExercises;
    private List<WorkoutLog> logs;

    public ExecutionAdapter(List<PlanExercise> planExercises) {
        this.planExercises = planExercises;
        this.logs = new ArrayList<>();
        
        // Initialize logs with defaults
        for (PlanExercise pe : planExercises) {
            WorkoutLog log = new WorkoutLog();
            log.setExerciseId(pe.getExerciseId());
            log.setSetNumber(pe.getSets()); // Default to target sets
            log.setReps(Integer.parseInt(pe.getReps().replaceAll("[^0-9]", ""))); // Parse simple int
            log.setWeight(pe.getWeightKg());
            logs.add(log);
        }
    }

    public List<WorkoutLog> getLogs() {
        return logs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_execution_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlanExercise pe = planExercises.get(position);
        WorkoutLog log = logs.get(position);

        String exName = (pe.getExercise() != null) ? pe.getExercise().getName() : "Exercise";
        holder.tvExName.setText(exName);
        holder.tvExTarget.setText("Target: " + pe.getSets() + " x " + pe.getReps() + " @ " + pe.getWeightKg() + "kg");

        // Bind data
        holder.etSets.setText(String.valueOf(log.getSetNumber()));
        holder.etReps.setText(String.valueOf(log.getReps()));
        holder.etWeight.setText(String.valueOf(log.getWeight()));
        holder.etNotes.setText(log.getNotes());

        // Listeners updating log object
        holder.etSets.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    log.setSetNumber(Integer.parseInt(s.toString()));
                } catch (NumberFormatException e) { log.setSetNumber(0); }
            }
        });
        
        holder.etReps.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    log.setReps(Integer.parseInt(s.toString()));
                } catch (NumberFormatException e) { log.setReps(0); }
            }
        });
        
         holder.etWeight.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    log.setWeight(Double.parseDouble(s.toString()));
                } catch (NumberFormatException e) { log.setWeight(0); }
            }
        });
         
         holder.etNotes.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                log.setNotes(s.toString());
            }
        });
    }

    @Override
    public int getItemCount() {
        return planExercises.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvExName, tvExTarget;
        EditText etSets, etReps, etWeight, etNotes;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExName = itemView.findViewById(R.id.tvExName);
            tvExTarget = itemView.findViewById(R.id.tvExTarget);
            etSets = itemView.findViewById(R.id.etSets);
            etReps = itemView.findViewById(R.id.etReps);
            etWeight = itemView.findViewById(R.id.etWeight);
            etNotes = itemView.findViewById(R.id.etNotes);
        }
    }
    
    abstract class SimpleTextWatcher implements TextWatcher {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void afterTextChanged(Editable s) {}
    }
}
