package com.example.mosquizto.Event;

public class LoginSuccessEvent {
    public final String token;

    public LoginSuccessEvent(String token) {
        this.token = token;
    }
}