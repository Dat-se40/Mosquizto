package com.example.mosquizto.Models;

public class User {
    private String username;
    private String avatarUrl;
    public User(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}