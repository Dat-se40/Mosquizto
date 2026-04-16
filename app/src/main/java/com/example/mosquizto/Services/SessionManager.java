package com.example.mosquizto.Services;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mosquizto.Activities.Login;
import com.example.mosquizto.Dto.request.LoginRequest;

public class SessionManager {
    private Context context ;
    private String accessToken  ;
    // Chữa cháy bằng các lưu thông tin người dùng như này, mốt phải code User Class riêng
    public LoginRequest currentUserProfile ;
    public SessionManager(Context _context)
    {
        context = _context;
    }

    public void saveAuthToken(String token) {
        SharedPreferences prefs = context.getSharedPreferences("MosquiztoPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("USER_TOKEN", token);
        editor.apply();
    }

    // Thêm luôn hàm này để lấy Token ra xài (dành cho AuthInterceptor)
    public String fetchAuthToken() {
        SharedPreferences prefs = context.getSharedPreferences("MosquiztoPrefs", Context.MODE_PRIVATE);
        return prefs.getString("USER_TOKEN", null);
    }

    public LoginRequest getCurrentUserProfile() {
        return currentUserProfile;
    }

    public void setCurrentUserProfile(LoginRequest currentUserProfile) {
        this.currentUserProfile = currentUserProfile;
    }
}
