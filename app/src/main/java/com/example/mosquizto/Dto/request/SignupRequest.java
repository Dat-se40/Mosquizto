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

    @NotNull
    private String birthdate;

    public SignupRequest(String s, String s1, String email, String password, String password1, String birthdate) {
    }
}
