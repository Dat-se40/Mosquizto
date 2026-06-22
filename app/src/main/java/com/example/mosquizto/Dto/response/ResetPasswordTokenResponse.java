package com.example.mosquizto.Dto.response;

public class ResetPasswordTokenResponse {
    private String email;
    private String secretKey;

    public String getEmail() {
        return email;
    }

    public String getSecretKey() {
        return secretKey;
    }
}
