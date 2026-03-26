package com.example.mosquizto.Dto.response;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("access_token")
    private String accessToken;
    @SerializedName("user_id")
    private int userId;

    public LoginResponse(String accessToken, int userId) {
        this.accessToken = accessToken;
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "accessToken='" + accessToken + '\'' +
                ", userId=" + userId +
                '}';
    }
}