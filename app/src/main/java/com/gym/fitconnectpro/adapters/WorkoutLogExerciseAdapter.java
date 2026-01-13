package com.gym.fitconnectpro.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.models.WorkoutLog;

import java.util.List;

public class WorkoutLogExerciseAdapter extends RecyclerView.Adapter<WorkoutLogExerciseAdapter.ExerciseViewHolder> {
    private List<WorkoutLog> exercises;

    public WorkoutLogExerciseAdapter(List<WorkoutLog> exercises) {
        this.exercises = exercises;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_log_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        WorkoutLog log = exercises.get(position);
        holder.tvExerciseName.setText(log.getExerciseName() != null ? log.getExerciseName() : "Exercise");
        holder.tvSetsReps.setText("Set " + log.getSetNumber() + ": " + log.getReps() + " reps");
        holder.tvWeight.setText(String.format("%.1fkg", log.getWeight()));
    }

    @Override
    public int getItemCount() {
        return exercises != null ? exercises.size() : 0;
    }

    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView tvExerciseName, tvSetsReps, tvWeight;

        ExerciseViewHolder(View itemView) {
            super(itemView);
            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            tvSetsReps = itemView.findViewById(R.id.tvSetsReps);
            tvWeight = itemView.findViewById(R.id.tvWeight);
        }
    }
}
