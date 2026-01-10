package com.gym.fitconnectpro.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.database.entities.Salary;

import java.util.ArrayList;
import java.util.List;

public class SalaryReportAdapter extends RecyclerView.Adapter<SalaryReportAdapter.ViewHolder> {

    private Context context;
    private List<Salary> salaryList;

    public SalaryReportAdapter(Context context) {
        this.context = context;
        this.salaryList = new ArrayList<>();
    }

    public void setSalaries(List<Salary> salaries) {
        this.salaryList = salaries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_salary_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Salary salary = salaryList.get(position);
        holder.bind(salary);
    }

    @Override
    public int getItemCount() {
        return salaryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTrainerName, tvDate, tvNetSalary, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTrainerName = itemView.findViewById(R.id.tvTrainerName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvNetSalary = itemView.findViewById(R.id.tvNetSalary);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        public void bind(Salary salary) {
            tvTrainerName.setText(salary.getTrainerName());
            tvDate.setText("Paid: " + (salary.getPaymentDate() != null ? salary.getPaymentDate() : "Pending"));
            tvNetSalary.setText(String.format("$%.2f", salary.getNetSalary()));
            tvStatus.setText(salary.getStatus());

            if ("PAID".equalsIgnoreCase(salary.getStatus())) {
                tvStatus.setTextColor(Color.parseColor("#388E3C")); // Green
            } else {
                tvStatus.setTextColor(Color.parseColor("#F57C00")); // Orange
            }
        }
    }
}
