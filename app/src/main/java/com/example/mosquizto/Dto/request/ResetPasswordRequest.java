package com.example.mosquizto.Dto.request;

public class ResetPasswordRequest {
    private final String secretKey;
    private final String newPassword;
    private final String confirmPassword;

    public ResetPasswordRequest(String secretKey, String newPassword, String confirmPassword) {
        this.secretKey = secretKey;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }
}
