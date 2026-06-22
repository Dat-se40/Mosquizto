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

import com.example.mosquizto.Activities.NotificationActivity;
import com.example.mosquizto.Dto.response.NotificationResponse;
import com.example.mosquizto.MainActivity;
import com.example.mosquizto.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private final MutableLiveData<Boolean> _forceRefreshTrigger = new MutableLiveData<>(false);
    private static final String CHANNEL_ID = "MOSQUIZTO_ALERTS";
    private static final String PREF_NAME = "NOTIFICATION_PREFS";
    private static final String KEY_COUNT = "NOTIFICATION_COUNT";
    private static final String KEY_PUSH_ENABLED = "PUSH_NOTIFICATIONS_ENABLED";

    private boolean hasReceivedInitialBatch = false;
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;
    private final Gson gson;

    private final Map<String, Long> unreadNotificationMap = new HashMap<>();
    private final Set<Long> seenNotificationIds = new HashSet<>();
    private boolean pushNotificationsEnabled;

    @Inject
    public WebSocketManager(@ApplicationContext Context context, Gson gson) {
        this.context = context;
        this.gson = gson;
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
    public LiveData<Boolean> getForceRefreshTrigger() {
        return _forceRefreshTrigger;
    }

    public boolean hasReceivedInitialBatch() {
        return hasReceivedInitialBatch;
    }
    private String getNotificationKey(String type, Long referenceId) {
        return type + "_" + referenceId;
    }

    public Long getNotificationIdForReference(String type, Long refId) {
        return unreadNotificationMap.get(getNotificationKey(type, refId));
    }

    public void removeNotificationFromMap(String type, Long refId) {
        unreadNotificationMap.remove(getNotificationKey(type, refId));
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

        String notificationTopic = "/user/queue/notifications";

        stompClient.topic(notificationTopic).subscribe(message -> {
            String jsonPayload = message.getPayload();

            Type listType = new TypeToken<List<NotificationResponse>>(){}.getType();
            try {
                List<NotificationResponse> incomingNotifs = gson.fromJson(jsonPayload, listType);

                if (incomingNotifs != null && !incomingNotifs.isEmpty()) {

                    int newItems = 0;
                    List<NotificationResponse> genuinelyNew = new ArrayList<>();

                    for (NotificationResponse notif : incomingNotifs) {
                        if (notif.getId() == null) continue;

                        boolean isNew = !seenNotificationIds.contains(notif.getId());
                        if (isNew) {
                            newItems++;
                            genuinelyNew.add(notif);
                            seenNotificationIds.add(notif.getId());
                        }

                        if (notif.getNotificationType() != null) {
                            String mapKey = notif.getReferenceId() != null
                                    ? getNotificationKey(notif.getNotificationType().name(), notif.getReferenceId())
                                    : "NOTIF_" + notif.getId();
                            unreadNotificationMap.put(mapKey, notif.getId());
                        }
                    }

                    if (!hasReceivedInitialBatch) {
                        hasReceivedInitialBatch = true;
                        setNotificationCount(incomingNotifs.size());
                        Log.i("STOMP", "Received initial batch of " + incomingNotifs.size() + " notifications");
                        _forceRefreshTrigger.postValue(true);
                    } else if (newItems > 0) {
                        updateNotificationCount(newItems);

                        for (NotificationResponse notif : genuinelyNew) {
                            handleNewIncomingNotification(notif);
                            if (notif.getMessage() != null && !notif.getMessage().isEmpty()) {
                                _notifications.postValue(notif.getMessage());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("STOMP", "Error parsing notification JSON: " + jsonPayload, e);
            }
        }, t -> Log.e("STOMP", "Error receiving notification", t));
    }

    private void handleNewIncomingNotification(NotificationResponse notif) {
        showSystemNotification("Mosquizto", notif.getMessage());
    }

    public void incrementNotificationCount() {
        updateNotificationCount(1);
    }

    public void readNotification() {
        updateNotificationCount(-1);
    }

    public void setNotificationCount(int count) {
        if (count < 0) count = 0;
        editor.putInt(KEY_COUNT, count);
        editor.apply();
        _notificationCount.postValue(count);
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

        Intent intent = new Intent(context, NotificationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent,
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
        if (stompClient != null) {
            stompClient.disconnect();
            stompClient = null;
        }


        hasReceivedInitialBatch = false;
        unreadNotificationMap.clear();
        seenNotificationIds.clear();

        // Xóa số đếm thông báo trên icon
        editor.putInt(KEY_COUNT, 0).apply();
        _notificationCount.postValue(0);

        // Clear LiveData
        _forceRefreshTrigger.postValue(false);
        Log.i("STOMP", "WebSocket disconnected & cleaned up for Logout");
    }

    public void clearLocalPreferences() {
        pushNotificationsEnabled = true;
        editor.clear();
        editor.apply();
        _notificationCount.postValue(0);
        _forceRefreshTrigger.postValue(false);
    }
}
