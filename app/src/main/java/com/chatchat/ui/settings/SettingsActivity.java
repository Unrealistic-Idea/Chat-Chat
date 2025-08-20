package com.chatchat.ui.settings;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.chatchat.R;
import com.chatchat.utils.PackageInfoUtils;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        setupToolbar();
        loadPackageInfo();
        loadDeviceInfo();
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
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("应用信息");
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