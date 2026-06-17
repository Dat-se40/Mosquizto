package com.example.mosquizto.ViewModels;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.mosquizto.Network.WebSocketManager;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MainViewModel extends ViewModel {

    private final WebSocketManager webSocketManager;

    @Inject
    public MainViewModel(WebSocketManager webSocketManager) {
        this.webSocketManager = webSocketManager;
    }
    public LiveData<String> getNotifications() {
        return webSocketManager.getNotifications();
    }
    public LiveData<Integer> getNotificationCount()
    {
        return webSocketManager.getNotificationCount();
    }
    public void connectStomp(String token) {
        webSocketManager.connect(token);
    }
}