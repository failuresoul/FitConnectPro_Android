package com.gym.fitconnectpro.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.models.WaterLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WaterLogAdapter extends RecyclerView.Adapter<WaterLogAdapter.WaterLogViewHolder> {
    private List<WaterLog> logs;
    private OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(WaterLog log);
    }

    public WaterLogAdapter(List<WaterLog> logs, OnDeleteClickListener deleteListener) {
        this.logs = logs;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public WaterLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_water_log, parent, false);
        return new WaterLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WaterLogViewHolder holder, int position) {
        WaterLog log = logs.get(position);
        
        // Format time
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            Date date = inputFormat.parse(log.getLogTime());
            if (date != null) {
                holder.tvLogTime.setText(outputFormat.format(date));
            }
        } catch (Exception e) {
            holder.tvLogTime.setText(log.getLogTime());
        }
        
        holder.tvLogAmount.setText(log.getAmountMl() + "ml");
        
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(log);
            }
        });
    }

    @Override
    public int getItemCount() {
        return logs != null ? logs.size() : 0;
    }

    public void updateLogs(List<WaterLog> newLogs) {
        this.logs = newLogs;
        notifyDataSetChanged();
    }

    static class WaterLogViewHolder extends RecyclerView.ViewHolder {
        TextView tvLogTime, tvLogAmount;
        Button btnDelete;

        WaterLogViewHolder(View itemView) {
            super(itemView);
            tvLogTime = itemView.findViewById(R.id.tvLogTime);
            tvLogAmount = itemView.findViewById(R.id.tvLogAmount);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
