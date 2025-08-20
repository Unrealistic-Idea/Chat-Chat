package com.chatchat.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.chatchat.R;
import com.chatchat.ui.MainActivity;
import com.chatchat.model.Message;

public class NotificationService {
    
    private static final String CHANNEL_ID = "chat_notifications";
    private static final String CHANNEL_NAME = "Chat Messages";
    private static final int NOTIFICATION_ID = 1001;
    
    private Context context;
    private NotificationManager notificationManager;

    public NotificationService(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("通知新的聊天消息");
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void showMessageNotification(Message message, String senderName) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, 
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0
        );

        String contentText = getContentText(message);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(senderName)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private String getContentText(Message message) {
        switch (message.getType()) {
            case EMOJI:
                return message.getContent();
            case IMAGE:
                return "[图片]";
            case VOICE:
                return "[语音]";
            case MARKDOWN:
                return "[Markdown消息]";
            case CHART:
                return "[图表]";
            default:
                return message.getContent();
        }
    }

    public void cancelNotifications() {
        notificationManager.cancelAll();
    }
}