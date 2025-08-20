package com.chatchat.service;

import android.content.Context;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;

public class CloudSyncManager {
    
    private static final String SYNC_WORK_TAG = "cloud_sync_work";
    private Context context;
    private WorkManager workManager;

    public CloudSyncManager(Context context) {
        this.context = context;
        this.workManager = WorkManager.getInstance(context);
    }

    public void startPeriodicSync() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest syncRequest = new PeriodicWorkRequest.Builder(
                CloudSyncWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .addTag(SYNC_WORK_TAG)
                .build();

        workManager.enqueue(syncRequest);
    }

    public void stopPeriodicSync() {
        workManager.cancelAllWorkByTag(SYNC_WORK_TAG);
    }

    public void syncNow() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        androidx.work.OneTimeWorkRequest syncRequest = 
                new androidx.work.OneTimeWorkRequest.Builder(CloudSyncWorker.class)
                .setConstraints(constraints)
                .build();

        workManager.enqueue(syncRequest);
    }
}