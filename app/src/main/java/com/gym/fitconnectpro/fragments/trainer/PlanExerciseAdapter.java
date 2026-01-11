package com.gym.fitconnectpro.fragments.trainer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.database.entities.PlanExercise;

import java.util.ArrayList;
import java.util.List;

public class PlanExerciseAdapter extends RecyclerView.Adapter<PlanExerciseAdapter.ViewHolder> {

    private List<PlanExercise> exercises = new ArrayList<>();
    private OnRemoveListener onRemoveListener;

    public interface OnRemoveListener {
        void onRemove(int position);
    }

    public PlanExerciseAdapter(OnRemoveListener onRemoveListener) {
        this.onRemoveListener = onRemoveListener;
    }

    public void setExercises(List<PlanExercise> exercises) {
        this.exercises = exercises;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlanExercise exercise = exercises.get(position);
        
        String name = "Unknown Exercise";
        if (exercise.getExercise() != null) {
            name = exercise.getExercise().getName();
        }
        
        holder.tvName.setText(name);
        holder.tvDetails.setText(String.format("%d sets x %s reps | %s kg | %ds rest", 
                exercise.getSets(), exercise.getReps(), 
                exercise.getWeightKg() > 0 ? String.valueOf(exercise.getWeightKg()) : "Body",
                exercise.getRestSeconds()));

        holder.btnRemove.setOnClickListener(v -> {
            if (onRemoveListener != null) {
                onRemoveListener.onRemove(position); // Use holder.getAdapterPosition() if strictly needed but position is usually safe in simple lists
            }
        });
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDetails;
        ImageButton btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvExerciseName);
            tvDetails = itemView.findViewById(R.id.tvExerciseDetails);
            btnRemove = itemView.findViewById(R.id.btnRemoveExercise);
        }
    }
}
