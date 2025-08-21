package com.chatchat.ui.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.chatchat.R;
import com.chatchat.ui.chat.ChatActivity;

/**
 * 用户主页查看页面
 */
public class UserProfileActivity extends AppCompatActivity {
    
    private TextView textViewUsername;
    private TextView textViewUserId;
    private TextView textViewEmail;
    private TextView textViewBio;
    private Button buttonSendMessage;
    private Button buttonEditProfile;
    
    private String userId;
    private String username;
    private String email;
    private SharedPreferences sharedPreferences;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        
        sharedPreferences = getSharedPreferences("ChatChatPrefs", MODE_PRIVATE);
        getIntentData();
        initViews();
        setupToolbar();
        setupButtons();
        loadUserProfile();
    }
    
    private void getIntentData() {
        userId = getIntent().getStringExtra("user_id");
        username = getIntent().getStringExtra("username");
        email = getIntent().getStringExtra("email");
        
        if (userId == null) {
            userId = "未知用户";
        }
        if (username == null) {
            username = "未知用户";
        }
    }
    
    private void initViews() {
        textViewUsername = findViewById(R.id.textViewUsername);
        textViewUserId = findViewById(R.id.textViewUserId);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewBio = findViewById(R.id.textViewBio);
        buttonSendMessage = findViewById(R.id.buttonSendMessage);
        buttonEditProfile = findViewById(R.id.buttonEditProfile);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("用户主页");
        }
    }
    
    private void setupButtons() {
        String currentUserId = sharedPreferences.getString("current_user_id", "");
        
        // Check if this is the current user's profile
        if (userId.equals(currentUserId)) {
            // Show edit button, hide send message button
            buttonEditProfile.setVisibility(View.VISIBLE);
            buttonSendMessage.setVisibility(View.GONE);
            
            buttonEditProfile.setOnClickListener(v -> {
                Intent intent = new Intent(this, EditProfileActivity.class);
                startActivity(intent);
            });
        } else {
            // Show send message button, hide edit button
            buttonEditProfile.setVisibility(View.GONE);
            buttonSendMessage.setVisibility(View.VISIBLE);
            
            buttonSendMessage.setOnClickListener(v -> {
                // Start chat with this user
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra(ChatActivity.EXTRA_CHAT_NAME, username);
                intent.putExtra(ChatActivity.EXTRA_CHAT_USER_ID, userId);
                intent.putExtra(ChatActivity.EXTRA_IS_AI_CHAT, false);
                startActivity(intent);
            });
        }
    }
    
    private void loadUserProfile() {
        textViewUsername.setText(username);
        textViewUserId.setText("用户ID: " + userId);
        
        if (email != null && !email.isEmpty()) {
            textViewEmail.setText("邮箱: " + email);
            textViewEmail.setVisibility(View.VISIBLE);
        } else {
            textViewEmail.setVisibility(View.GONE);
        }
        
        // Set a default bio or load from preferences
        String currentUserId = sharedPreferences.getString("current_user_id", "");
        if (userId.equals(currentUserId)) {
            String bio = sharedPreferences.getString("user_bio", "这个人很懒，什么都没留下...");
            textViewBio.setText(bio);
        } else {
            // For other users, show a default bio
            if (userId.equals("ai_assistant")) {
                textViewBio.setText("我是你的AI助手，随时为你服务！");
            } else {
                textViewBio.setText("这个人很懒，什么都没留下...");
            }
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}