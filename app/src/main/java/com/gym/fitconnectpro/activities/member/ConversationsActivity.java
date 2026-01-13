package com.gym.fitconnectpro.activities.member;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.dao.SocialDAO;
import com.gym.fitconnectpro.services.Session;

import java.util.List;

public class ConversationsActivity extends AppCompatActivity {

    private RecyclerView rvConversations;
    private TextView tvNoConversations;
    private SocialDAO socialDAO;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);
        
        currentUserId = Session.getInstance(this).getUserId();

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        rvConversations = findViewById(R.id.rvConversations);
        tvNoConversations = findViewById(R.id.tvNoConversations);
        
        socialDAO = new SocialDAO(this);
        
        rvConversations.setLayoutManager(new LinearLayoutManager(this));
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadConversations();
    }
    
    private void loadConversations() {
        List<SocialDAO.Conversation> conversations = socialDAO.getConversations(currentUserId);
        
        if (conversations.isEmpty()) {
            tvNoConversations.setVisibility(View.VISIBLE);
            rvConversations.setVisibility(View.GONE);
        } else {
            tvNoConversations.setVisibility(View.GONE);
            rvConversations.setVisibility(View.VISIBLE);
            
            ConversationsAdapter adapter = new ConversationsAdapter(this, conversations);
            rvConversations.setAdapter(adapter);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
