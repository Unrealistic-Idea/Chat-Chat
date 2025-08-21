package com.chatchat.ui.profile;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.chatchat.R;

/**
 * 编辑个人资料页面
 */
public class EditProfileActivity extends AppCompatActivity {
    
    private EditText editTextUsername;
    private EditText editTextEmail;
    private EditText editTextBio;
    private Button buttonSave;
    private Button buttonCancel;
    
    private SharedPreferences sharedPreferences;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        
        sharedPreferences = getSharedPreferences("ChatChatPrefs", MODE_PRIVATE);
        
        initViews();
        setupToolbar();
        setupButtons();
        loadCurrentProfile();
    }
    
    private void initViews() {
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextBio = findViewById(R.id.editTextBio);
        buttonSave = findViewById(R.id.buttonSave);
        buttonCancel = findViewById(R.id.buttonCancel);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("编辑资料");
        }
    }
    
    private void setupButtons() {
        buttonSave.setOnClickListener(v -> saveProfile());
        buttonCancel.setOnClickListener(v -> finish());
    }
    
    private void loadCurrentProfile() {
        String currentUserId = sharedPreferences.getString("current_user_id", "");
        String username = sharedPreferences.getString("username", currentUserId);
        String email = sharedPreferences.getString("user_email", "");
        String bio = sharedPreferences.getString("user_bio", "");
        
        editTextUsername.setText(username);
        editTextEmail.setText(email);
        editTextBio.setText(bio);
    }
    
    private void saveProfile() {
        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String bio = editTextBio.getText().toString().trim();
        
        // Validate input
        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError("用户名不能为空");
            editTextUsername.requestFocus();
            return;
        }
        
        if (!TextUtils.isEmpty(email) && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("请输入有效的邮箱地址");
            editTextEmail.requestFocus();
            return;
        }
        
        // Save to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username);
        editor.putString("user_email", email);
        editor.putString("user_bio", bio);
        editor.apply();
        
        Toast.makeText(this, "资料保存成功", Toast.LENGTH_SHORT).show();
        finish();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}