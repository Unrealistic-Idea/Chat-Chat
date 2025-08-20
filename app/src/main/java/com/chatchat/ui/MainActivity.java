package com.chatchat.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.chatchat.R;
import com.chatchat.auth.JwtUtils;
import com.chatchat.ui.auth.LoginActivity;
import com.chatchat.database.AppDatabase;
import com.chatchat.database.ChatGroupDao;
import com.chatchat.model.ChatGroup;
import com.chatchat.service.CloudSyncManager;
import com.chatchat.utils.GpuOptimizationManager;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private SharedPreferences sharedPreferences;
    private ExecutorService executor;
    private CloudSyncManager cloudSyncManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 启用GPU硬件加速
        GpuOptimizationManager.enableHardwareAcceleration(this);
        
        // Check authentication first
        sharedPreferences = getSharedPreferences("ChatChatPrefs", MODE_PRIVATE);
        if (!isUserLoggedIn()) {
            redirectToLogin();
            return;
        }
        
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDemoGroupChat();
                Snackbar.make(view, "已创建演示群聊", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        
        // 为导航视图启用GPU加速
        GpuOptimizationManager.enableGpuLayerForView(navigationView);
        
        // Update navigation header with user info
        updateNavigationHeader(navigationView);
        
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_chats, R.id.nav_profile)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        
        // Initialize cloud sync
        cloudSyncManager = new CloudSyncManager(this);
        cloudSyncManager.startPeriodicSync();
    }

    private boolean isUserLoggedIn() {
        String token = sharedPreferences.getString("jwt_token", null);
        return token != null && JwtUtils.validateToken(token) && !JwtUtils.isTokenExpired(token);
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void updateNavigationHeader(NavigationView navigationView) {
        View headerView = navigationView.getHeaderView(0);
        TextView textViewUsername = headerView.findViewById(R.id.textViewUsername);
        TextView textViewTravelerId = headerView.findViewById(R.id.textViewTravelerId);
        
        String currentUserId = sharedPreferences.getString("current_user_id", "未知用户");
        textViewUsername.setText(currentUserId);
        textViewTravelerId.setText("旅者ID: " + currentUserId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void createDemoGroupChat() {
        if (executor == null) {
            executor = Executors.newSingleThreadExecutor();
        }
        
        executor.execute(() -> {
            AppDatabase database = AppDatabase.getDatabase(this);
            ChatGroupDao chatGroupDao = database.chatGroupDao();
            
            String currentUserId = sharedPreferences.getString("current_user_id", "user1");
            
            // Create a demo group chat
            ChatGroup demoGroup = new ChatGroup(
                UUID.randomUUID().toString(),
                "演示群聊 " + System.currentTimeMillis() % 1000,
                currentUserId
            );
            
            demoGroup.setDescription("这是一个演示群聊");
            demoGroup.setMemberIds(Arrays.asList(currentUserId, "ai_assistant", "demo_user"));
            demoGroup.setAdminIds(Arrays.asList(currentUserId));
            
            chatGroupDao.insertChatGroup(demoGroup);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
        if (cloudSyncManager != null) {
            cloudSyncManager.stopPeriodicSync();
        }
    }
}