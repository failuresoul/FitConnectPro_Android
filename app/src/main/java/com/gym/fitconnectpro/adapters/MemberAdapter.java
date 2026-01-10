package com.gym.fitconnectpro.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.database.entities.Member;

import java.util.ArrayList;
import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private List<Member> memberList;
    private OnMemberActionListener listener;

    public interface OnMemberActionListener {
        void onEdit(Member member);
        void onSuspend(Member member);
        void onDelete(Member member);
    }

    public MemberAdapter(OnMemberActionListener listener) {
        this.memberList = new ArrayList<>();
        this.listener = listener;
    }

    public void setMembers(List<Member> members) {
        this.memberList = members;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        Member member = memberList.get(position);
        holder.bind(member);
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    class MemberViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMemberName, tvMemberId, tvEmail, tvMembershipType;
        private TextView tvExpiryDate, tvAssignedTrainer, tvStatus;
        private Button btnEdit, btnSuspend, btnDelete;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);

            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            tvMemberId = itemView.findViewById(R.id.tvMemberId);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvMembershipType = itemView.findViewById(R.id.tvMembershipType);
            tvExpiryDate = itemView.findViewById(R.id.tvExpiryDate);
            tvAssignedTrainer = itemView.findViewById(R.id.tvAssignedTrainer);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnSuspend = itemView.findViewById(R.id.btnSuspend);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Member member) {
            tvMemberName.setText(member.getFullName());
            tvMemberId.setText("ID: M" + String.format("%03d", member.getMemberId()));
            tvEmail.setText(member.getEmail());
            tvMembershipType.setText("Membership: " + member.getMembershipType());
            tvExpiryDate.setText("Expires: " + member.getMembershipEndDate());
            tvAssignedTrainer.setText("Trainer: " +
                    (member.getAssignedTrainer() != null ? member.getAssignedTrainer() : "Not Assigned"));
            tvStatus.setText(member.getStatus());

            // Set status color
            if ("Active".equals(member.getStatus())) {
                tvStatus.setBackgroundResource(R.drawable.bg_gradient_primary);
            } else if ("Suspended".equals(member.getStatus())) {
                tvStatus.setBackgroundColor(itemView.getContext().getColor(android.R.color.holo_orange_dark));
            } else if ("Expired".equals(member.getStatus())) {
                tvStatus.setBackgroundColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
            }

            // Set button text based on status
            if ("Active".equals(member.getStatus())) {
                btnSuspend.setText("Suspend");
            } else {
                btnSuspend.setText("Activate");
            }

            // Button click listeners
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEdit(member);
                }
            });

            btnSuspend.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSuspend(member);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(member);
                }
            });
        }
    }
}
