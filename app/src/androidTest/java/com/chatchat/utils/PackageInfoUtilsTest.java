package com.chatchat.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/**
 * 包信息读取功能测试
 * 验证在物理设备上读取安装包信息的功能
 */
@RunWith(AndroidJUnit4.class)
public class PackageInfoUtilsTest {

    @Test
    public void testCanReadPackageInfo() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        
        // 验证能够读取包信息
        boolean canRead = PackageInfoUtils.canReadPackageInfo(appContext);
        assertTrue("应该能够读取包信息", canRead);
    }

    @Test
    public void testGetAppName() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        
        String appName = PackageInfoUtils.getAppName(appContext);
        assertNotNull("应用名称不应为null", appName);
        assertFalse("应用名称不应为空", appName.isEmpty());
        assertNotEquals("应用名称不应为默认值", "未知应用", appName);
    }

    @Test
    public void testGetVersionName() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        
        String versionName = PackageInfoUtils.getVersionName(appContext);
        assertNotNull("版本名称不应为null", versionName);
        assertFalse("版本名称不应为空", versionName.isEmpty());
        assertNotEquals("版本名称不应为默认值", "未知版本", versionName);
    }

    @Test
    public void testGetVersionCode() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        
        long versionCode = PackageInfoUtils.getVersionCode(appContext);
        assertTrue("版本号应大于0", versionCode > 0);
    }

    @Test
    public void testGetTargetSdkVersion() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        
        int targetSdk = PackageInfoUtils.getTargetSdkVersion(appContext);
        assertTrue("目标SDK版本应大于0", targetSdk > 0);
        assertTrue("目标SDK版本应合理", targetSdk >= 30 && targetSdk <= 40);
    }

    @Test
    public void testPackageInfoSafelyNotNull() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        
        PackageInfo packageInfo = PackageInfoUtils.getPackageInfoSafely(appContext);
        assertNotNull("安全获取的包信息不应为null", packageInfo);
    }

    @Test
    public void testDiagnosticsNotEmpty() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        
        String diagnostics = PackageInfoUtils.getPackageInfoDiagnostics(appContext);
        assertNotNull("诊断信息不应为null", diagnostics);
        assertFalse("诊断信息不应为空", diagnostics.isEmpty());
        assertTrue("诊断信息应包含标题", diagnostics.contains("包信息诊断报告"));
    }

    @Test
    public void testPackageNameCorrect() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        
        String expectedPackageName = "com.chatchat";
        String actualPackageName = appContext.getPackageName();
        assertEquals("包名应匹配", expectedPackageName, actualPackageName);
    }
}