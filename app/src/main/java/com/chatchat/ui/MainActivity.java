package com.chatchat.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentContainerView;
import com.chatchat.R;
import com.chatchat.auth.JwtUtils;
import com.chatchat.ui.auth.LoginActivity;
import com.chatchat.ui.settings.SettingsActivity;
import com.chatchat.database.AppDatabase;
import com.chatchat.database.ChatGroupDao;
import com.chatchat.model.ChatGroup;
import com.chatchat.service.CloudSyncManager;
import com.chatchat.utils.GpuOptimizationManager;
import com.chatchat.utils.PermissionManager;
import java.util.Arrays;
import java.util.List;
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
        
        // 检查是否为首次启动，如果是则请求权限
        if (PermissionManager.isFirstLaunch(this)) {
            showFirstLaunchPermissionDialog();
            return;
        }
        
        setContentView(R.layout.activity_main);
        setupUI();
    }
    
    /**
     * 显示首次启动权限对话框
     */
    private void showFirstLaunchPermissionDialog() {
        new AlertDialog.Builder(this)
            .setTitle("欢迎使用Chat-Chat")
            .setMessage("为了提供更好的用户体验，我们需要获取一些必要的权限：\n\n" +
                       "• 相机权限：用于拍摄和分享照片\n" +
                       "• 存储权限：用于保存和读取文件\n" +
                       "• 录音权限：用于语音消息\n" +
                       "• 通知权限：用于接收消息提醒\n\n" +
                       "您可以随时在设置中修改这些权限。")
            .setPositiveButton("授权", (dialog, which) -> {
                PermissionManager.requestPermissions(this);
            })
            .setNegativeButton("跳过", (dialog, which) -> {
                PermissionManager.markFirstLaunchCompleted(this);
                setContentView(R.layout.activity_main);
                setupUI();
                Toast.makeText(this, "您可以在设置中手动授权权限", Toast.LENGTH_LONG).show();
            })
            .setCancelable(false)
            .show();
    }
    
    /**
     * 设置UI组件
     */
    private void setupUI() {
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

        // 修改获取NavController的方式
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);
        NavController navController = navHostFragment.getNavController();

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PermissionManager.PERMISSION_REQUEST_CODE) {
            PermissionManager.markFirstLaunchCompleted(this);
            
            // 检查权限授权结果
            List<String> deniedPermissions = PermissionManager.getUngrantedPermissions(this);
            
            if (deniedPermissions.isEmpty()) {
                Toast.makeText(this, "权限授权成功", Toast.LENGTH_SHORT).show();
            } else {
                StringBuilder message = new StringBuilder("以下权限被拒绝：\n");
                for (String permission : deniedPermissions) {
                    message.append("• ").append(PermissionManager.getPermissionDisplayName(permission)).append("\n");
                }
                message.append("\n您可以在设置中手动授权这些权限");
                
                new AlertDialog.Builder(this)
                    .setTitle("权限提醒")
                    .setMessage(message.toString())
                    .setPositiveButton("知道了", null)
                    .show();
            }
            
            // 无论权限是否全部授权，都继续初始化应用
            setContentView(R.layout.activity_main);
            setupUI();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search_users) {
            Intent intent = new Intent(this, com.chatchat.ui.search.UserSearchActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);
        NavController navController = navHostFragment.getNavController();
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