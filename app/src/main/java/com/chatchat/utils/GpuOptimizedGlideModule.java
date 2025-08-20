package com.chatchat.utils;

import android.content.Context;
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;
//import com.chatchat.BuildConfig;

/**
 * GPU优化的Glide配置模块
 * 为Chat-Chat应用提供高性能图像加载和缓存
 */
@GlideModule
public class GpuOptimizedGlideModule extends AppGlideModule {
    
    private static final int MEMORY_CACHE_SIZE = 20 * 1024 * 1024; // 20MB
    private static final int DISK_CACHE_SIZE = 100 * 1024 * 1024; // 100MB
    
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        // 配置内存缓存
        builder.setMemoryCache(new LruResourceCache(MEMORY_CACHE_SIZE));
        
        // 配置磁盘缓存
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, DISK_CACHE_SIZE));
        
        // 配置默认请求选项以优化GPU性能
        RequestOptions defaultOptions = new RequestOptions()
                .format(DecodeFormat.PREFER_RGB_565) // 使用RGB_565格式减少内存使用
                .diskCacheStrategy(DiskCacheStrategy.ALL) // 缓存所有版本
                .skipMemoryCache(false) // 启用内存缓存
                .timeout(10000); // 10秒超时
        
        builder.setDefaultRequestOptions(defaultOptions);
        
        // 启用日志（仅在调试模式下）
//        if (BuildConfig.DEBUG) {
//            builder.setLogLevel(android.util.Log.VERBOSE);
//        }
    }
    
    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        // 可以在这里注册自定义组件
        super.registerComponents(context, glide, registry);
    }
    
    @Override
    public boolean isManifestParsingEnabled() {
        return false; // 禁用清单解析以提高性能
    }
}