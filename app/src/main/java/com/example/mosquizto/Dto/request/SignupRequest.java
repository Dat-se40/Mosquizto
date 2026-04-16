package com.example.mosquizto.Dto.request;

import androidx.annotation.Size;

import org.jetbrains.annotations.NotNull;

public class SignupRequest {
    @NotNull
    @Size(max = 150)
    private String fullName;

    @NotNull
    @Size(max = 100)
    private String username;

    @NotNull
    @Size(max = 255)
    private String email;

    @NotNull
    private String password;

    @NotNull
    private String confirmPassword;

    public SignupRequest(@NotNull String fullName, @NotNull String username, @NotNull String email, @NotNull String password, @NotNull String confirmPassword) {
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }
}
