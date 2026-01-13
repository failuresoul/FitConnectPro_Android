package com.gym.fitconnectpro.activities.member;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.dao.SocialDAO;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private Context context;
    private List<SocialDAO.Message> messages;
    private int currentUserId;

    public ChatAdapter(Context context, List<SocialDAO.Message> messages, int currentUserId) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        SocialDAO.Message msg = messages.get(position);
        
        if (msg.senderId == currentUserId) {
            // Sent message
            holder.layoutSent.setVisibility(View.VISIBLE);
            holder.layoutReceived.setVisibility(View.GONE);
            holder.tvMessageSent.setText(msg.content);
            if (msg.timestamp != null && msg.timestamp.length() > 16) {
                holder.tvTimeSent.setText(msg.timestamp.substring(11, 16));
            }
        } else {
            // Received message
            holder.layoutSent.setVisibility(View.GONE);
            holder.layoutReceived.setVisibility(View.VISIBLE);
            holder.tvMessageReceived.setText(msg.content);
             if (msg.timestamp != null && msg.timestamp.length() > 16) {
                holder.tvTimeReceived.setText(msg.timestamp.substring(11, 16));
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutSent, layoutReceived;
        TextView tvMessageSent, tvTimeSent, tvMessageReceived, tvTimeReceived;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutSent = itemView.findViewById(R.id.layoutSent);
            layoutReceived = itemView.findViewById(R.id.layoutReceived);
            tvMessageSent = itemView.findViewById(R.id.tvMessageSent);
            tvTimeSent = itemView.findViewById(R.id.tvTimeSent);
            tvMessageReceived = itemView.findViewById(R.id.tvMessageReceived);
            tvTimeReceived = itemView.findViewById(R.id.tvTimeReceived);
        }
    }
}
