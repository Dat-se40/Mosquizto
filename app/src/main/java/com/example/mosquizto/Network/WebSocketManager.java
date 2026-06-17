package com.example.mosquizto.Network;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mosquizto.MainActivity;
import com.example.mosquizto.R;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;

@Singleton
public class WebSocketManager {

    private StompClient stompClient;
    private final Context context;

    private final MutableLiveData<String> _notifications = new MutableLiveData<>();
    private final MutableLiveData<Integer> _notificationCount = new MutableLiveData<>(0);
    private static final String CHANNEL_ID = "MOSQUIZTO_ALERTS";
    private static final String PREF_NAME = "NOTIFICATION_PREFS";
    private static final String KEY_COUNT = "NOTIFICATION_COUNT";
    private static final String KEY_PUSH_ENABLED = "PUSH_NOTIFICATIONS_ENABLED";

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;
    private boolean pushNotificationsEnabled;

    @Inject
    public WebSocketManager(@ApplicationContext Context context) {
        this.context = context;
        // 1. Initialize SharedPreferences FIRST
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
        
        createNotificationChannel();

        pushNotificationsEnabled = sharedPreferences.getBoolean(KEY_PUSH_ENABLED, true);
        
        // 2. Set initial value to LiveData from saved preferences
        int savedCount = sharedPreferences.getInt(KEY_COUNT, 0);
        _notificationCount.postValue(savedCount);
    }

    public LiveData<String> getNotifications() {
        return _notifications;
    }

    public LiveData<Integer> getNotificationCount() {
        return _notificationCount;
    }

    public boolean isPushNotificationEnabled() {
        return pushNotificationsEnabled;
    }

    public void setPushNotificationEnabled(boolean enabled) {
        pushNotificationsEnabled = enabled;
        editor.putBoolean(KEY_PUSH_ENABLED, enabled).apply();
    }

    @SuppressLint("CheckResult")
    public void connect(String token) {
        if (stompClient != null && stompClient.isConnected()) return;

        String url = context.getString(R.string.ws_base_url) + context.getString(R.string.ws_endpoint);
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, url);

        List<StompHeader> headers = new ArrayList<>();
        headers.add(new StompHeader("Authorization", "Bearer " + token));

        stompClient.lifecycle().subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType()) {
                case OPENED:
                    Log.i("STOMP", "Connected");
                    break;
                case ERROR:
                    Log.e("STOMP", "Error", lifecycleEvent.getException());
                    break;
                case CLOSED:
                    Log.w("STOMP", "Closed");
                    break;
            }
        });

        stompClient.connect(headers);

        stompClient.topic(context.getString(R.string.ws_topic_invitation)).subscribe(message -> {
            _notifications.postValue(context.getString(R.string.notification_prefix) + message.getPayload());
            handleNewIncomingNotification(message.getPayload());
        }, t -> Log.e("STOMP", "Err Inv", t));

        stompClient.topic(context.getString(R.string.ws_topic_report)).subscribe(message -> {
            _notifications.postValue(context.getString(R.string.notification_prefix) + message.getPayload());
            handleNewIncomingNotification(message.getPayload());
        }, t -> Log.e("STOMP", "Err Rep", t));
    }

    private void handleNewIncomingNotification(String message) {
        incrementNotificationCount();
        showSystemNotification("Mosquizto", message);
    }

    public void incrementNotificationCount() {
        updateNotificationCount(1);
    }

    public void readNotification() {
        updateNotificationCount(-1);
    }

    public void clearNotificationCount() {
        int current = sharedPreferences.getInt(KEY_COUNT, 0);
        updateNotificationCount(-current);
    }

    public void updateNotificationCount(int amount) {
        int count = sharedPreferences.getInt(KEY_COUNT, 0);
        count += amount;
        if (count < 0) count = 0;
        
        editor.putInt(KEY_COUNT, count);
        editor.apply();
        
        // CRITICAL: Update LiveData so UI can reflect changes immediately
        _notificationCount.postValue(count);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Alerts", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    public void showSystemNotification(String title, String content) {
        if (!pushNotificationsEnabled) return;

        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (intent != null) intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    public void disconnect() {
        if (stompClient != null) stompClient.disconnect();
    }
}
