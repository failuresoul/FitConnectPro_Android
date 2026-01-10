package com.gym.fitconnectpro.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.database.entities.Trainer;

import java.util.ArrayList;
import java.util.List;

public class TrainerAdapter extends RecyclerView.Adapter<TrainerAdapter.TrainerViewHolder> {

    private Context context;
    private List<Trainer> trainerList;
    private OnTrainerActionListener listener;

    public interface OnTrainerActionListener {
        void onEdit(Trainer trainer);
        void onViewClients(Trainer trainer);
        void onDelete(Trainer trainer);
    }

    public TrainerAdapter(Context context, OnTrainerActionListener listener) {
        this.context = context;
        this.listener = listener;
        this.trainerList = new ArrayList<>();
    }

    public void setTrainers(List<Trainer> trainers) {
        this.trainerList = trainers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TrainerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trainer, parent, false);
        return new TrainerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrainerViewHolder holder, int position) {
        Trainer trainer = trainerList.get(position);
        holder.bind(trainer);
    }

    @Override
    public int getItemCount() {
        return trainerList.size();
    }

    class TrainerViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvStatus, tvId, tvSpecialization, tvExperience, tvSalary, tvClientCount;
        Button btnEdit, btnViewClients, btnDelete;

        public TrainerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvTrainerName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvId = itemView.findViewById(R.id.tvTrainerId);
            tvSpecialization = itemView.findViewById(R.id.tvSpecialization);
            tvExperience = itemView.findViewById(R.id.tvExperience);
            tvSalary = itemView.findViewById(R.id.tvSalary);
            tvClientCount = itemView.findViewById(R.id.tvClientCount);
            
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnViewClients = itemView.findViewById(R.id.btnViewClients);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Trainer trainer) {
            tvName.setText(trainer.getFullName());
            tvId.setText("ID: " + trainer.getTrainerId());
            tvSpecialization.setText(trainer.getSpecialization());
            tvExperience.setText(trainer.getExperienceYears() + " Years");
            tvSalary.setText(String.format("$%.2f", trainer.getSalary()));
            tvClientCount.setText(String.valueOf(trainer.getAssignedClientsCount()));
            
            tvStatus.setText(trainer.getStatus());
            if ("ACTIVE".equalsIgnoreCase(trainer.getStatus())) {
                tvStatus.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
            } else if ("INACTIVE".equalsIgnoreCase(trainer.getStatus())) {
                tvStatus.setBackgroundColor(Color.parseColor("#F44336")); // Red
            } else {
                tvStatus.setBackgroundColor(Color.parseColor("#FF9800")); // Orange
            }

            btnEdit.setOnClickListener(v -> listener.onEdit(trainer));
            btnViewClients.setOnClickListener(v -> listener.onViewClients(trainer));
            btnDelete.setOnClickListener(v -> listener.onDelete(trainer));
        }
    }
}
