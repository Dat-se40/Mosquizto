package com.example.mosquizto.Dto.request;

public class VerifyCodeRequest {
    private final String email;
    private final String code;

    public VerifyCodeRequest(String email, String code) {
        this.email = email;
        this.code = code;
    }

    public String getEmail() {
        return email;
    }

    public String getCode() {
        return code;
    }
}
