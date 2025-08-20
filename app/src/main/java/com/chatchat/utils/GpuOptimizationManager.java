package com.chatchat.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.util.ViewPreloadSizeProvider;

/**
 * GPU优化管理器
 * 为Chat-Chat应用提供GPU加速和性能优化功能
 */
public class GpuOptimizationManager {
    
    private static final String TAG = "GpuOptimizationManager";
    
    /**
     * 为Activity启用GPU硬件加速
     */
    public static void enableHardwareAcceleration(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // 确保硬件加速已启用
            activity.getWindow().setFlags(
                android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            );
        }
    }
    
    /**
     * 为RecyclerView优化GPU渲染性能
     */
    public static void optimizeRecyclerViewForGpu(RecyclerView recyclerView) {
        if (recyclerView == null) return;
        
        // 启用硬件加速
        recyclerView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        
        // 启用缓存以提高滚动性能
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        
        // 优化滚动性能
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        
        // 启用嵌套滚动
        recyclerView.setNestedScrollingEnabled(true);
    }
    
    /**
     * 为View启用GPU图层加速
     */
    public static void enableGpuLayerForView(View view) {
        if (view == null) return;
        
        // 为视图启用硬件层以提高动画和渲染性能
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        
        // 启用抗锯齿以提高文本和图形质量
        if (view instanceof android.widget.TextView) {
            ((android.widget.TextView) view).getPaint().setFlags(
                Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        }
    }
    
    /**
     * 为ViewGroup批量启用GPU加速
     */
    public static void enableGpuAccelerationForViewGroup(ViewGroup viewGroup) {
        if (viewGroup == null) return;
        
        enableGpuLayerForView(viewGroup);
        
        // 递归为子视图启用GPU加速
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup) {
                enableGpuAccelerationForViewGroup((ViewGroup) child);
            } else {
                enableGpuLayerForView(child);
            }
        }
    }
    
    /**
     * 获取GPU优化的Glide请求管理器
     */
    public static RequestManager getOptimizedGlideManager(Context context) {
        return Glide.with(context)
                .applyDefaultRequestOptions(
                    com.bumptech.glide.request.RequestOptions
                        .diskCacheStrategyOf(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                        .skipMemoryCache(false)
                );
    }
    
    /**
     * 为RecyclerView配置图像预加载以提高滚动性能
     */
    public static <T> void setupImagePreloading(RecyclerView recyclerView, 
                                               RecyclerView.Adapter<? extends RecyclerView.ViewHolder> adapter,
                                               Context context) {
        if (recyclerView == null || adapter == null) return;
        
        // 创建视图预加载大小提供者
        ViewPreloadSizeProvider<String> sizeProvider = new ViewPreloadSizeProvider<>();
        
        // 创建预加载器
        RecyclerViewPreloader<String> preloader = new RecyclerViewPreloader<>(
            Glide.with(context),
            new ImagePreloadModelProvider(context),
            sizeProvider,
            10 // 预加载项目数量
        );
        
        // 添加滚动监听器
        recyclerView.addOnScrollListener(preloader);
    }
    
    /**
     * 检查设备是否支持硬件加速
     */
    public static boolean isHardwareAccelerationSupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }
    
    /**
     * 获取GPU加速状态信息
     */
    public static String getGpuAccelerationInfo(View view) {
        if (view == null) return "View is null";
        
        StringBuilder info = new StringBuilder();
        info.append("Hardware Acceleration: ");
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            info.append(view.isHardwareAccelerated() ? "Enabled" : "Disabled");
            info.append("\nLayer Type: ");
            
            switch (view.getLayerType()) {
                case View.LAYER_TYPE_NONE:
                    info.append("None");
                    break;
                case View.LAYER_TYPE_SOFTWARE:
                    info.append("Software");
                    break;
                case View.LAYER_TYPE_HARDWARE:
                    info.append("Hardware");
                    break;
                default:
                    info.append("Unknown");
                    break;
            }
        } else {
            info.append("Not supported (API < 11)");
        }
        
        return info.toString();
    }
    
    /**
     * 图像预加载模型提供者
     */
    private static class ImagePreloadModelProvider implements com.bumptech.glide.ListPreloader.PreloadModelProvider<String> {
        private final Context context;
        
        public ImagePreloadModelProvider(Context context) {
            this.context = context;
        }
        
        @Override
        public java.util.List<String> getPreloadItems(int position) {
            // 返回要预加载的图像URL列表
            return java.util.Collections.emptyList();
        }
        
        @Override
        public com.bumptech.glide.RequestBuilder<android.graphics.drawable.Drawable> getPreloadRequestBuilder(String item) {
            // Return a simple Glide request builder for the item
            return Glide.with(context).load(item);
        }
    }
}