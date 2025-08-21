package com.chatchat.ui.settings;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.chatchat.R;
import com.chatchat.utils.PackageInfoUtils;
import com.chatchat.utils.PermissionManager;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private TextView textViewAppName;
    private TextView textViewAppVersion;
    private TextView textViewVersionCode;
    private TextView textViewPackageName;
    private TextView textViewTargetSdk;
    private TextView textViewMinSdk;
    private TextView textViewAndroidVersion;
    private TextView textViewDeviceModel;
    private TextView textViewManufacturer;
    
    // Permission related views
    private TextView textViewPermissionStatus;
    private Button buttonCheckPermissions;
    private Button buttonRequestPermissions;
    private Button buttonOpenAppSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        setupToolbar();
        setupPermissionButtons();
        loadPackageInfo();
        loadDeviceInfo();
        updatePermissionStatus();
    }

    private void initViews() {
        textViewAppName = findViewById(R.id.textViewAppName);
        textViewAppVersion = findViewById(R.id.textViewAppVersion);
        textViewVersionCode = findViewById(R.id.textViewVersionCode);
        textViewPackageName = findViewById(R.id.textViewPackageName);
        textViewTargetSdk = findViewById(R.id.textViewTargetSdk);
        textViewMinSdk = findViewById(R.id.textViewMinSdk);
        textViewAndroidVersion = findViewById(R.id.textViewAndroidVersion);
        textViewDeviceModel = findViewById(R.id.textViewDeviceModel);
        textViewManufacturer = findViewById(R.id.textViewManufacturer);
        
        // Initialize permission views
        textViewPermissionStatus = findViewById(R.id.textViewPermissionStatus);
        buttonCheckPermissions = findViewById(R.id.buttonCheckPermissions);
        buttonRequestPermissions = findViewById(R.id.buttonRequestPermissions);
        buttonOpenAppSettings = findViewById(R.id.buttonOpenAppSettings);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("设置");
        }
    }
    
    /**
     * 设置权限按钮的点击事件
     */
    private void setupPermissionButtons() {
        buttonCheckPermissions.setOnClickListener(v -> updatePermissionStatus());
        
        buttonRequestPermissions.setOnClickListener(v -> {
            List<String> ungrantedPermissions = PermissionManager.getUngrantedPermissions(this);
            if (ungrantedPermissions.isEmpty()) {
                Toast.makeText(this, "所有权限已授权", Toast.LENGTH_SHORT).show();
                updatePermissionStatus();
            } else {
                showPermissionRequestDialog();
            }
        });
        
        buttonOpenAppSettings.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "无法打开应用设置", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * 显示权限请求对话框
     */
    private void showPermissionRequestDialog() {
        List<String> ungrantedPermissions = PermissionManager.getUngrantedPermissions(this);
        StringBuilder message = new StringBuilder("应用需要以下权限来正常运行：\n\n");
        
        for (String permission : ungrantedPermissions) {
            message.append("• ").append(PermissionManager.getPermissionDisplayName(permission)).append("\n");
        }
        
        message.append("\n是否现在授权这些权限？");
        
        new AlertDialog.Builder(this)
            .setTitle("权限授权")
            .setMessage(message.toString())
            .setPositiveButton("授权", (dialog, which) -> PermissionManager.requestPermissions(this))
            .setNegativeButton("取消", null)
            .show();
    }
    
    /**
     * 更新权限状态显示
     */
    private void updatePermissionStatus() {
        boolean hasAllPermissions = PermissionManager.hasAllPermissions(this);
        List<String> ungrantedPermissions = PermissionManager.getUngrantedPermissions(this);
        
        if (hasAllPermissions) {
            textViewPermissionStatus.setText("权限状态: 已授权所有必需权限");
            textViewPermissionStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark, getTheme()));
            buttonRequestPermissions.setEnabled(false);
            buttonRequestPermissions.setText("权限已完整");
        } else {
            textViewPermissionStatus.setText("权限状态: 缺少 " + ungrantedPermissions.size() + " 个权限");
            textViewPermissionStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark, getTheme()));
            buttonRequestPermissions.setEnabled(true);
            buttonRequestPermissions.setText("请求权限 (" + ungrantedPermissions.size() + ")");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PermissionManager.PERMISSION_REQUEST_CODE) {
            // 更新权限状态显示
            updatePermissionStatus();
            
            // 显示结果
            List<String> stillDenied = PermissionManager.getUngrantedPermissions(this);
            if (stillDenied.isEmpty()) {
                Toast.makeText(this, "所有权限已授权完成", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "仍有 " + stillDenied.size() + " 个权限未授权", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadPackageInfo() {
        try {
            // 使用工具类安全地读取包信息
            if (!PackageInfoUtils.canReadPackageInfo(this)) {
                // 如果无法读取包信息，显示诊断信息
                String diagnostics = PackageInfoUtils.getPackageInfoDiagnostics(this);
                Toast.makeText(this, "无法读取包信息\n" + diagnostics, Toast.LENGTH_LONG).show();
                return;
            }

            // 读取并显示包信息
            String appName = PackageInfoUtils.getAppName(this);
            String versionName = PackageInfoUtils.getVersionName(this);
            long versionCode = PackageInfoUtils.getVersionCode(this);
            String packageName = getPackageName();
            int targetSdk = PackageInfoUtils.getTargetSdkVersion(this);

            textViewAppName.setText("应用名称: " + appName);
            textViewAppVersion.setText("版本名称: " + versionName);
            textViewVersionCode.setText("版本号: " + versionCode);
            textViewPackageName.setText("包名: " + packageName);
            textViewTargetSdk.setText("目标SDK: " + targetSdk);
            
            // MinSdk requires additional handling as it's not directly available
            int minSdk = getMinSdkVersion();
            textViewMinSdk.setText("最低SDK: " + minSdk);

        } catch (Exception e) {
            String errorMessage = "读取包信息时发生错误: " + e.getMessage() + 
                                "\n诊断信息:\n" + PackageInfoUtils.getPackageInfoDiagnostics(this);
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    private int getMinSdkVersion() {
        // Get minSdk from build config or use a default value
        // This is set in build.gradle and should match the actual minSdk
        return 30; // Default value matching build.gradle
    }

    private void loadDeviceInfo() {
        try {
            // Android version info
            String androidVersion = "Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")";
            textViewAndroidVersion.setText("Android版本: " + androidVersion);
            
            // Device model
            String deviceModel = Build.MODEL;
            textViewDeviceModel.setText("设备型号: " + deviceModel);
            
            // Manufacturer
            String manufacturer = Build.MANUFACTURER;
            textViewManufacturer.setText("制造商: " + manufacturer);
            
        } catch (Exception e) {
            Toast.makeText(this, "读取设备信息时发生错误: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}