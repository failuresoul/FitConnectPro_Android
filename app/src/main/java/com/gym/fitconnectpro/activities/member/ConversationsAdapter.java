package com.gym.fitconnectpro.activities.member;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.dao.SocialDAO;

import java.util.List;

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ConversationViewHolder> {

    private Context context;
    private List<SocialDAO.Conversation> conversations;

    public ConversationsAdapter(Context context, List<SocialDAO.Conversation> conversations) {
        this.context = context;
        this.conversations = conversations;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        SocialDAO.Conversation conv = conversations.get(position);
        
        holder.tvName.setText(conv.name);
        holder.tvLastMsg.setText(conv.lastMessage);
        
        if (conv.lastMessageTime != null && !conv.lastMessageTime.isEmpty()) {
            holder.tvTime.setText(conv.lastMessageTime.substring(11, 16)); // Simple substring for time HH:mm
            holder.tvTime.setVisibility(View.VISIBLE);
        } else {
            holder.tvTime.setVisibility(View.GONE);
        }
        
        if (conv.unreadCount > 0) {
            holder.tvUnreadCount.setText(String.valueOf(conv.unreadCount));
            holder.tvUnreadCount.setVisibility(View.VISIBLE);
        } else {
            holder.tvUnreadCount.setVisibility(View.GONE);
        }
        
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("OTHER_USER_ID", conv.userId);
            intent.putExtra("OTHER_USER_NAME", conv.name);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public static class ConversationViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLastMsg, tvTime, tvUnreadCount;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvConvName);
            tvLastMsg = itemView.findViewById(R.id.tvConvLastMsg);
            tvTime = itemView.findViewById(R.id.tvConvTime);
            tvUnreadCount = itemView.findViewById(R.id.tvUnreadCount);
        }
    }
}
