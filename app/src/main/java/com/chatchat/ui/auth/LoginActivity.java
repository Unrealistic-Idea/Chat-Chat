package com.chatchat.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.chatchat.R;
import com.chatchat.auth.JwtUtils;
import com.chatchat.database.AppDatabase;
import com.chatchat.database.UserDao;
import com.chatchat.model.User;
import com.chatchat.ui.MainActivity;
import com.chatchat.utils.CryptoUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.ProgressBar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText editTextTravelerId;
    private TextInputEditText editTextPassword;
    private MaterialButton buttonLogin;
    private ProgressBar progressBarLogin;
    
    private AppDatabase database;
    private UserDao userDao;
    private ExecutorService executor;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        initDatabase();
        checkExistingLogin();
        setupClickListeners();
    }

    private void initViews() {
        editTextTravelerId = findViewById(R.id.editTextTravelerId);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        progressBarLogin = findViewById(R.id.progressBarLogin);
    }

    private void initDatabase() {
        database = AppDatabase.getDatabase(this);
        userDao = database.userDao();
        executor = Executors.newSingleThreadExecutor();
        sharedPreferences = getSharedPreferences("ChatChatPrefs", MODE_PRIVATE);
    }

    private void checkExistingLogin() {
        String savedToken = sharedPreferences.getString("jwt_token", null);
        if (savedToken != null && JwtUtils.validateToken(savedToken) && !JwtUtils.isTokenExpired(savedToken)) {
            navigateToMain();
        }
    }

    private void setupClickListeners() {
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });
    }

    private void handleLogin() {
        String travelerId = editTextTravelerId.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(travelerId)) {
            editTextTravelerId.setError("请输入旅者ID");
            editTextTravelerId.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("请输入密码");
            editTextPassword.requestFocus();
            return;
        }

        showLoading(true);

        executor.execute(() -> {
            try {
                User existingUser = userDao.getUserById(travelerId);
                
                if (existingUser == null) {
                    // Create new user
                    String salt = CryptoUtils.generateSalt();
                    String hashedPassword = CryptoUtils.hashPassword(password, salt);
                    String encryptedPassword = CryptoUtils.encrypt(hashedPassword + ":" + salt);
                    
                    User newUser = new User(travelerId, travelerId);
                    newUser.setEncryptedPassword(encryptedPassword);
                    newUser.setOnline(true);
                    
                    String token = JwtUtils.generateToken(travelerId);
                    newUser.setToken(token);
                    
                    userDao.insertUser(newUser);
                    
                    runOnUiThread(() -> {
                        saveLoginData(travelerId, token);
                        showLoading(false);
                        Toast.makeText(LoginActivity.this, "注册成功，欢迎来到Chat-Chat！", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    });
                } else {
                    // Verify existing user
                    String encryptedPassword = existingUser.getEncryptedPassword();
                    String decryptedData = CryptoUtils.decrypt(encryptedPassword);
                    String[] parts = decryptedData.split(":");
                    String hashedPassword = parts[0];
                    String salt = parts[1];
                    
                    if (CryptoUtils.verifyPassword(password, hashedPassword, salt)) {
                        String token = JwtUtils.generateToken(travelerId);
                        existingUser.setToken(token);
                        existingUser.setOnline(true);
                        existingUser.setLastSeen(System.currentTimeMillis());
                        
                        userDao.updateUser(existingUser);
                        
                        runOnUiThread(() -> {
                            saveLoginData(travelerId, token);
                            showLoading(false);
                            Toast.makeText(LoginActivity.this, "登录成功，欢迎回来！", Toast.LENGTH_SHORT).show();
                            navigateToMain();
                        });
                    } else {
                        runOnUiThread(() -> {
                            showLoading(false);
                            Toast.makeText(LoginActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(LoginActivity.this, "登录失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void saveLoginData(String travelerId, String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("current_user_id", travelerId);
        editor.putString("jwt_token", token);
        editor.apply();
    }

    private void showLoading(boolean show) {
        progressBarLogin.setVisibility(show ? View.VISIBLE : View.GONE);
        buttonLogin.setEnabled(!show);
        editTextTravelerId.setEnabled(!show);
        editTextPassword.setEnabled(!show);
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}