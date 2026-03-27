package com.example.mosquizto.Services;

import android.content.Context;

public class SessionManager {
    private Context context ;
    private String accessToken  ;
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
}
