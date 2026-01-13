package com.gym.fitconnectpro.activities.member;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.dao.SocialDAO;

import java.util.List;

public class FriendRequestsAdapter extends RecyclerView.Adapter<FriendRequestsAdapter.RequestViewHolder> {

    private Context context;
    private List<SocialDAO.FriendRequest> requests;
    private SocialDAO socialDAO;
    private OnRequestActionResultListener listener;

    public interface OnRequestActionResultListener {
        void onActionCompleted();
    }

    public FriendRequestsAdapter(Context context, List<SocialDAO.FriendRequest> requests, OnRequestActionResultListener listener) {
        this.context = context;
        this.requests = requests;
        this.socialDAO = new SocialDAO(context);
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        SocialDAO.FriendRequest req = requests.get(position);
        
        if ("SENT".equals(req.type)) {
            holder.tvName.setText(req.receiverName);
            holder.layoutActionsReceived.setVisibility(View.GONE);
            holder.btnCancel.setVisibility(View.VISIBLE);
            
            holder.btnCancel.setOnClickListener(v -> actionRequest(req.id, "CANCEL", position));
            
        } else {
            // RECEIVED
            holder.tvName.setText(req.senderName);
            holder.layoutActionsReceived.setVisibility(View.VISIBLE);
            holder.btnCancel.setVisibility(View.GONE);
            
            holder.btnAccept.setOnClickListener(v -> actionRequest(req.id, "ACCEPTED", position));
            holder.btnReject.setOnClickListener(v -> actionRequest(req.id, "REJECTED", position));
        }
    }
    
    private void actionRequest(int id, String action, int position) {
        if ("CANCEL".equals(action)) {
            // Logic to delete request
            // We can reuse removeFriend logic if it just deletes based on ID, 
            // but SocialDAO.removeFriend takes member IDs. 
            // We might need a direct delete or respond with 'REJECTED' effectively cancels it? 
            // Let's use REJECTED for now or implement delete in DAO if strictly needed.
            // Actually, respondToRequest("REJECTED") works effectively as ignoring/archiving it.
            // But usually cancel means delete.
            socialDAO.respondToRequest(id, "REJECTED"); // Using REJECTED for now to hide it
        } else {
            socialDAO.respondToRequest(id, action);
        }
        
        // Remove item from list and notify
        requests.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, requests.size());
        
        if (listener != null) listener.onActionCompleted();
        
        Toast.makeText(context, "Action: " + action, Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        LinearLayout layoutActionsReceived;
        Button btnAccept, btnReject, btnCancel;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRequestName);
            layoutActionsReceived = itemView.findViewById(R.id.layoutActionsReceived);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }
}
