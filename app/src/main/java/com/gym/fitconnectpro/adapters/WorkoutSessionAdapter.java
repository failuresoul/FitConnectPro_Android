package com.gym.fitconnectpro.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.models.WorkoutSession;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WorkoutSessionAdapter extends RecyclerView.Adapter<WorkoutSessionAdapter.SessionViewHolder> {
    private List<WorkoutSession> sessions;

    public WorkoutSessionAdapter(List<WorkoutSession> sessions) {
        this.sessions = sessions;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_log_session, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        WorkoutSession session = sessions.get(position);
        
        // Format date
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            
            Date sessionDate = inputFormat.parse(session.getSessionDate());
            if (sessionDate != null) {
                holder.tvSessionDate.setText(dateFormat.format(sessionDate));
                holder.tvSessionTime.setText(timeFormat.format(new Date())); // Using current time as session time
            }
        } catch (Exception e) {
            holder.tvSessionDate.setText(session.getSessionDate());
            holder.tvSessionTime.setText("");
        }
        
        holder.tvDuration.setText(session.getDurationMinutes() + " min");
        holder.tvCalories.setText(session.getCaloriesBurned() + " cal");
        
        // Set up nested RecyclerView for exercises
        WorkoutLogExerciseAdapter exerciseAdapter = new WorkoutLogExerciseAdapter(session.getExercises());
        holder.rvExercises.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.rvExercises.setAdapter(exerciseAdapter);
    }

    @Override
    public int getItemCount() {
        return sessions != null ? sessions.size() : 0;
    }

    static class SessionViewHolder extends RecyclerView.ViewHolder {
        TextView tvSessionDate, tvSessionTime, tvDuration, tvCalories;
        RecyclerView rvExercises;

        SessionViewHolder(View itemView) {
            super(itemView);
            tvSessionDate = itemView.findViewById(R.id.tvSessionDate);
            tvSessionTime = itemView.findViewById(R.id.tvSessionTime);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvCalories = itemView.findViewById(R.id.tvCalories);
            rvExercises = itemView.findViewById(R.id.rvExercises);
        }
    }
}
