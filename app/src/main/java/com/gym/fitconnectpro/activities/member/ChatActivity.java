package com.gym.fitconnectpro.activities.member;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.dao.SocialDAO;
import com.gym.fitconnectpro.services.Session;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvChatMessages;
    private EditText etMessageInput;
    private ImageButton btnSend;
    
    private SocialDAO socialDAO;
    private ChatAdapter adapter;
    private List<SocialDAO.Message> messagesList = new ArrayList<>();
    
    private int currentUserId;
    private int otherUserId;
    private String otherUserName;
    
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pollRunnable;
    private boolean isActivityActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        
        currentUserId = Session.getInstance(this).getUserId();
        otherUserId = getIntent().getIntExtra("OTHER_USER_ID", -1);
        otherUserName = getIntent().getStringExtra("OTHER_USER_NAME");
        
        if (otherUserId == -1) {
            Toast.makeText(this, "Error: Invalid user", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(otherUserName != null ? otherUserName : "Chat");
        }
        
        rvChatMessages = findViewById(R.id.rvChatMessages);
        etMessageInput = findViewById(R.id.etMessageInput);
        btnSend = findViewById(R.id.btnSend);
        
        socialDAO = new SocialDAO(this);
        
        rvChatMessages.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new ChatAdapter(this, messagesList, currentUserId);
        rvChatMessages.setAdapter(adapter);
        
        btnSend.setOnClickListener(v -> sendMessage());
        
        // Mark as read immediately when opening
        socialDAO.markAsRead(otherUserId, currentUserId);
        
        setupPolling();
    }
    
    private void setupPolling() {
        pollRunnable = new Runnable() {
            @Override
            public void run() {
                if (isActivityActive) {
                    loadMessages();
                    handler.postDelayed(this, 3000); // Refresh every 3 seconds
                }
            }
        };
    }
    
    private void loadMessages() {
        List<SocialDAO.Message> newMessages = socialDAO.getMessages(currentUserId, otherUserId);
        
        // Simple diff check - if size changed, update all (could be optimized)
        if (newMessages.size() != messagesList.size()) {
            messagesList.clear();
            messagesList.addAll(newMessages);
            adapter.notifyDataSetChanged();
            rvChatMessages.scrollToPosition(messagesList.size() - 1);
            
            // Mark new messages as read
            new Thread(() -> socialDAO.markAsRead(otherUserId, currentUserId)).start();
        }
    }
    
    private void sendMessage() {
        String content = etMessageInput.getText().toString().trim();
        if (content.isEmpty()) return;
        
        boolean success = socialDAO.sendMessage(currentUserId, otherUserId, content);
        if (success) {
            etMessageInput.setText("");
            loadMessages(); // Refresh immediately
        } else {
            Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        isActivityActive = true;
        loadMessages(); // Initial load
        handler.post(pollRunnable); // Start polling
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        isActivityActive = false;
        handler.removeCallbacks(pollRunnable); // Stop polling
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
