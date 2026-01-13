package com.gym.fitconnectpro.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.database.entities.PlanExercise;

import java.util.List;

public class WorkoutPlanExerciseAdapter extends RecyclerView.Adapter<WorkoutPlanExerciseAdapter.ViewHolder> {

    private List<PlanExercise> exercises;

    public WorkoutPlanExerciseAdapter(List<PlanExercise> exercises) {
        this.exercises = exercises;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_exercise_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlanExercise exercise = exercises.get(position);
        
        String name = "Exercise";
        String muscle = "";
        
        if (exercise.getExercise() != null) {
            name = exercise.getExercise().getName();
            muscle = exercise.getExercise().getMuscleGroup() != null ? exercise.getExercise().getMuscleGroup() : "";
        }
        
        holder.tvExerciseName.setText(name + (!muscle.isEmpty() ? " (" + muscle + ")" : ""));
        
        // Target: Sets x Reps @ Weight
        StringBuilder target = new StringBuilder();
        target.append(exercise.getSets()).append(" Sets x ").append(exercise.getReps()).append(" Reps");
        if (exercise.getWeightKg() > 0) {
            target.append(" @ ").append(exercise.getWeightKg()).append("kg");
        }
        holder.tvTargets.setText(target.toString());
        
        holder.tvRest.setText("Rest: " + exercise.getRestSeconds() + "s");
        
        if (exercise.getNotes() != null && !exercise.getNotes().isEmpty()) {
            holder.tvNotes.setText("Notes: " + exercise.getNotes());
            holder.tvNotes.setVisibility(View.VISIBLE);
        } else {
            holder.tvNotes.setVisibility(View.GONE);
        }
        
        // Checkbox logic - purely visual for now effectively
        holder.cbCompleted.setOnCheckedChangeListener(null);
        holder.cbCompleted.setChecked(false); // Default unchecked unless we persist state (not requested)
    }

    @Override
    public int getItemCount() {
        return exercises != null ? exercises.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvExerciseName, tvTargets, tvRest, tvNotes;
        CheckBox cbCompleted;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            tvTargets = itemView.findViewById(R.id.tvTargets);
            tvRest = itemView.findViewById(R.id.tvRest);
            tvNotes = itemView.findViewById(R.id.tvNotes);
            cbCompleted = itemView.findViewById(R.id.cbExerciseCompleted);
        }
    }
}
