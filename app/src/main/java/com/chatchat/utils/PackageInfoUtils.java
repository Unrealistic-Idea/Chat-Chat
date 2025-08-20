package com.chatchat.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

/**
 * 包信息读取工具类
 * 专门处理在物理安卓设备上读取安装包信息的问题
 */
public class PackageInfoUtils {
    
    private static final String TAG = "PackageInfoUtils";
    
    /**
     * 安全地获取应用包信息
     * 处理不同Android版本的兼容性问题
     */
    public static PackageInfo getPackageInfoSafely(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            String packageName = context.getPackageName();
            
            // 根据不同Android版本使用不同的API
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+ (API 33+) - 使用新的PackageInfoFlags
                return packageManager.getPackageInfo(packageName, 
                    PackageManager.PackageInfoFlags.of(0));
            } else {
                // Android 12及以下 - 使用旧的flags
                return packageManager.getPackageInfo(packageName, 0);
            }
            
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Package not found: " + e.getMessage());
            return null;
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception when reading package info: " + e.getMessage());
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error when reading package info: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取应用名称
     */
    public static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = getPackageInfoSafely(context);
            if (packageInfo != null) {
                return packageManager.getApplicationLabel(packageInfo.applicationInfo).toString();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting app name: " + e.getMessage());
        }
        return "未知应用";
    }
    
    /**
     * 获取版本名称
     */
    public static String getVersionName(Context context) {
        try {
            PackageInfo packageInfo = getPackageInfoSafely(context);
            if (packageInfo != null && packageInfo.versionName != null) {
                return packageInfo.versionName;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting version name: " + e.getMessage());
        }
        return "未知版本";
    }
    
    /**
     * 获取版本号，兼容新旧API
     */
    public static long getVersionCode(Context context) {
        try {
            PackageInfo packageInfo = getPackageInfoSafely(context);
            if (packageInfo != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    return packageInfo.getLongVersionCode();
                } else {
                    return packageInfo.versionCode;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting version code: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * 获取目标SDK版本
     */
    public static int getTargetSdkVersion(Context context) {
        try {
            PackageInfo packageInfo = getPackageInfoSafely(context);
            if (packageInfo != null && packageInfo.applicationInfo != null) {
                return packageInfo.applicationInfo.targetSdkVersion;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting target SDK version: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * 检查是否能够正常读取包信息
     * 用于诊断物理设备上的问题
     */
    public static boolean canReadPackageInfo(Context context) {
        PackageInfo packageInfo = getPackageInfoSafely(context);
        return packageInfo != null;
    }
    
    /**
     * 获取详细的错误信息，用于调试
     */
    public static String getPackageInfoDiagnostics(Context context) {
        StringBuilder diagnostics = new StringBuilder();
        diagnostics.append("包信息诊断报告:\n");
        diagnostics.append("Android版本: ").append(Build.VERSION.RELEASE).append(" (API ").append(Build.VERSION.SDK_INT).append(")\n");
        diagnostics.append("设备型号: ").append(Build.MODEL).append("\n");
        diagnostics.append("制造商: ").append(Build.MANUFACTURER).append("\n");
        
        try {
            PackageManager packageManager = context.getPackageManager();
            diagnostics.append("PackageManager可用: 是\n");
            
            String packageName = context.getPackageName();
            diagnostics.append("包名: ").append(packageName).append("\n");
            
            PackageInfo packageInfo = getPackageInfoSafely(context);
            if (packageInfo != null) {
                diagnostics.append("包信息读取: 成功\n");
                diagnostics.append("版本名称: ").append(packageInfo.versionName).append("\n");
                diagnostics.append("目标SDK: ").append(packageInfo.applicationInfo.targetSdkVersion).append("\n");
            } else {
                diagnostics.append("包信息读取: 失败\n");
            }
            
        } catch (Exception e) {
            diagnostics.append("诊断过程出错: ").append(e.getMessage()).append("\n");
        }
        
        return diagnostics.toString();
    }
}