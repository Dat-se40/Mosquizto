package com.example.mosquizto.Services;

import com.example.mosquizto.Network.WebSocketManager;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LogoutManager {

    private final SessionManager sessionManager;
    private final WebSocketManager webSocketManager;

    @Inject
    public LogoutManager(SessionManager sessionManager, WebSocketManager webSocketManager) {
        this.sessionManager = sessionManager;
        this.webSocketManager = webSocketManager;
    }

    /**
     * Đăng xuất đầy đủ: ngắt STOMP, reset badge (KEY_COUNT = 0), xóa session prefs.
     */
    public void logout() {
        webSocketManager.disconnect();
        sessionManager.logout();
    }

    /**
     * Xóa session trong bộ nhớ (dùng khi token hết hạn / xóa tài khoản local) — vẫn ngắt socket và reset badge.
     */
    public void clearSession() {
        webSocketManager.disconnect();
        sessionManager.clearSession();
    }
}
