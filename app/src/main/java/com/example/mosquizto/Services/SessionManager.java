package com.example.mosquizto.Services;

import android.content.Context;

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

    public void saveToken(String _accessToken) {
        accessToken = _accessToken ;
    }
    public String getAccessToken()
    {
        return accessToken ;
    }

    public LoginRequest getCurrentUserProfile() {
        return currentUserProfile;
    }

    public void setCurrentUserProfile(LoginRequest currentUserProfile) {
        this.currentUserProfile = currentUserProfile;
    }
}
