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
import com.gym.fitconnectpro.database.entities.Salary;

import java.util.ArrayList;
import java.util.List;

public class SalaryAdapter extends RecyclerView.Adapter<SalaryAdapter.SalaryViewHolder> {

    private Context context;
    private List<Salary> salaryList;
    private OnSalaryActionListener listener;

    public interface OnSalaryActionListener {
        void onPay(Salary salary);
        void onEdit(Salary salary);
        void onViewDetails(Salary salary);
    }

    public SalaryAdapter(Context context, OnSalaryActionListener listener) {
        this.context = context;
        this.listener = listener;
        this.salaryList = new ArrayList<>();
    }

    public void setSalaries(List<Salary> salaries) {
        this.salaryList = salaries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SalaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_salary, parent, false);
        return new SalaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SalaryViewHolder holder, int position) {
        Salary salary = salaryList.get(position);
        holder.bind(salary);
    }

    @Override
    public int getItemCount() {
        return salaryList.size();
    }

    class SalaryViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvDate, tvStatus, tvBase, tvBonus, tvDeductions, tvNet;
        Button btnPay, btnEdit, btnDetails;

        public SalaryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvTrainerName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvBase = itemView.findViewById(R.id.tvBaseSalary);
            tvBonus = itemView.findViewById(R.id.tvBonus);
            tvDeductions = itemView.findViewById(R.id.tvDeductions);
            tvNet = itemView.findViewById(R.id.tvNetSalary);

            btnPay = itemView.findViewById(R.id.btnPay);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDetails = itemView.findViewById(R.id.btnViewDetails);
        }

        public void bind(Salary salary) {
            tvName.setText(salary.getTrainerName());
            
            // Format Month/Year (e.g., "January 2026")
            String[] months = new String[]{"", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
            String monthName = (salary.getMonth() >= 1 && salary.getMonth() <= 12) ? months[salary.getMonth()] : String.valueOf(salary.getMonth());
            tvDate.setText(monthName + " " + salary.getYear());

            tvStatus.setText(salary.getStatus());
            if ("PAID".equalsIgnoreCase(salary.getStatus())) {
                tvStatus.setBackgroundResource(R.drawable.bg_spinner); // Reuse likely wrapper
                tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Green text
                btnPay.setVisibility(View.GONE); // Already paid
                btnEdit.setVisibility(View.GONE); // Cannot edit paid salary
            } else {
                tvStatus.setTextColor(Color.parseColor("#FF9800")); // Orange/Pending
                btnPay.setVisibility(View.VISIBLE);
                btnEdit.setVisibility(View.VISIBLE);
            }

            tvBase.setText(String.format("$%.2f", salary.getBaseSalary()));
            tvBonus.setText(String.format("$%.2f", salary.getBonus()));
            tvDeductions.setText(String.format("$%.2f", salary.getDeductions()));
            tvNet.setText(String.format("$%.2f", salary.getNetSalary()));

            btnPay.setOnClickListener(v -> listener.onPay(salary));
            btnEdit.setOnClickListener(v -> listener.onEdit(salary));
            btnDetails.setOnClickListener(v -> listener.onViewDetails(salary));
        }
    }
}
