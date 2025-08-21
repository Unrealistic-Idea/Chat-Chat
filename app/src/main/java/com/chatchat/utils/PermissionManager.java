package com.chatchat.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

/**
 * 权限管理工具类
 * 处理应用的权限请求和管理
 */
public class PermissionManager {
    
    public static final int PERMISSION_REQUEST_CODE = 1001;
    
    // 需要的权限列表
    private static final String[] REQUIRED_PERMISSIONS = {
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.INTERNET
    };
    
    // Android 13及以上需要的通知权限
    private static final String[] NOTIFICATION_PERMISSIONS = {
        Manifest.permission.POST_NOTIFICATIONS
    };
    
    /**
     * 检查是否是首次启动
     */
    public static boolean isFirstLaunch(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("ChatChatPrefs", Context.MODE_PRIVATE);
        return prefs.getBoolean("is_first_launch", true);
    }
    
    /**
     * 标记首次启动已完成
     */
    public static void markFirstLaunchCompleted(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("ChatChatPrefs", Context.MODE_PRIVATE);
        prefs.edit().putBoolean("is_first_launch", false).apply();
    }
    
    /**
     * 获取需要请求的权限列表
     */
    public static String[] getRequiredPermissions() {
        List<String> permissions = new ArrayList<>();
        
        // 添加基础权限
        for (String permission : REQUIRED_PERMISSIONS) {
            permissions.add(permission);
        }
        
        // Android 13及以上添加通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            for (String permission : NOTIFICATION_PERMISSIONS) {
                permissions.add(permission);
            }
        }
        
        return permissions.toArray(new String[0]);
    }
    
    /**
     * 检查所有必需权限是否已授权
     */
    public static boolean hasAllPermissions(Context context) {
        String[] permissions = getRequiredPermissions();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) 
                != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 获取未授权的权限列表
     */
    public static List<String> getUngrantedPermissions(Context context) {
        List<String> ungrantedPermissions = new ArrayList<>();
        String[] permissions = getRequiredPermissions();
        
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) 
                != PackageManager.PERMISSION_GRANTED) {
                ungrantedPermissions.add(permission);
            }
        }
        
        return ungrantedPermissions;
    }
    
    /**
     * 请求权限
     */
    public static void requestPermissions(Activity activity) {
        List<String> ungrantedPermissions = getUngrantedPermissions(activity);
        if (!ungrantedPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                activity, 
                ungrantedPermissions.toArray(new String[0]), 
                PERMISSION_REQUEST_CODE
            );
        }
    }
    
    /**
     * 检查权限是否被永久拒绝
     */
    public static boolean isPermissionPermanentlyDenied(Activity activity, String permission) {
        return !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }
    
    /**
     * 获取权限的友好名称
     */
    public static String getPermissionDisplayName(String permission) {
        switch (permission) {
            case Manifest.permission.CAMERA:
                return "相机权限";
            case Manifest.permission.RECORD_AUDIO:
                return "录音权限";
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                return "读取存储权限";
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return "写入存储权限";
            case Manifest.permission.ACCESS_NETWORK_STATE:
                return "网络状态权限";
            case Manifest.permission.INTERNET:
                return "网络权限";
            case Manifest.permission.POST_NOTIFICATIONS:
                return "通知权限";
            default:
                return permission;
        }
    }
}