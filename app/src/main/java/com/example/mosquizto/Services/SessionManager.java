package com.example.mosquizto.Services;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.mosquizto.Models.User;
import com.google.gson.Gson;

import dagger.hilt.android.qualifiers.ApplicationContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class SessionManager {
    private static final String PREF_NAME = "MosquiztoSession";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_USER = "user_json";

    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String accessToken;
    private String refreshToken ;
    private User currUser;
    private Gson gson;

    public SessionManager(@ApplicationContext   Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
        this.gson = new Gson();

        // Load lại dữ liệu từ "cache" khi khởi tạo
        this.accessToken = sharedPreferences.getString(KEY_TOKEN, null);
        this.refreshToken = sharedPreferences.getString(KEY_REFRESH_TOKEN, null);
        String userJson = sharedPreferences.getString(KEY_USER, null);
        if (userJson != null) {
            this.currUser = gson.fromJson(userJson, User.class);
        }
    }

    public void saveSession(String token, User user, String refreshToken) {
        this.accessToken = token;
        this.currUser = user;

        // Lưu vào SharedPreferences (File XML/JSON nội bộ)
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USER, gson.toJson(user));
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.apply(); // Chạy bất đồng bộ để không block UI
    }

    public String getAccessToken() {
        return accessToken;
    }

    public User getCurrUser() {
        return currUser;
    }

    public void logout() {
        this.accessToken = null;
        this.currUser = null;
        editor.clear();
        editor.apply();
    }

    // Kiểm tra xem session còn hiệu lực không (token không null)
    public boolean isLoggedIn() {
        return accessToken != null;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setCurrUser(User currUser) {
        this.currUser = currUser;
    }
}