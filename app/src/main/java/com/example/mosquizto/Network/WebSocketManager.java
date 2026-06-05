package com.example.mosquizto.Network;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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

    @Inject
    public WebSocketManager(@ApplicationContext Context context) {
        this.context = context;
    }

    public LiveData<String> getNotifications() {
        return _notifications;
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
                    Log.i(context.getString(R.string.log_tag_stomp), "STOMP Connected");
                    break;
                case ERROR:
                    Log.e(context.getString(R.string.log_tag_stomp), "STOMP Error", lifecycleEvent.getException());
                    break;
                case CLOSED:
                    Log.w(context.getString(R.string.log_tag_stomp), "STOMP Closed");
                    break;
            }
        });

        stompClient.connect(headers);

        // Subscribe các kênh và đẩy data vào LiveData (Không cần runOnUiThread ở đây vì LiveData sẽ tự lo)
        String topicInvitation = context.getString(R.string.ws_topic_invitation);
        stompClient.topic(topicInvitation).subscribe(message -> {
            _notifications.postValue(context.getString(R.string.notification_prefix) + message.getPayload());
        }, throwable -> Log.e("STOMP", "Error receiving invitation", throwable));

        String topicReport = context.getString(R.string.ws_topic_report);
        stompClient.topic(topicReport).subscribe(message -> {
            _notifications.postValue(context.getString(R.string.notification_prefix) + message.getPayload());
        }, throwable -> Log.e("STOMP", "Error receiving report", throwable));
    }

    public void disconnect() {
        if (stompClient != null && stompClient.isConnected()) {
            stompClient.disconnect();
        }
    }
}