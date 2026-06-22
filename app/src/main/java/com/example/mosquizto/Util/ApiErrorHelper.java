package com.example.mosquizto.Util;

import com.example.mosquizto.Dto.response.ApiResponse;
import com.google.gson.Gson;

import retrofit2.Response;

public final class ApiErrorHelper {

    private static final Gson GSON = new Gson();

    private ApiErrorHelper() {
    }

    public static String extractMessage(Response<?> response) {
        if (response.body() instanceof ApiResponse) {
            ApiResponse<?> body = (ApiResponse<?>) response.body();
            if (body.getMessage() != null && !body.getMessage().isEmpty()) {
                return body.getMessage();
            }
        }
        try {
            if (response.errorBody() != null) {
                ApiResponse<?> error = GSON.fromJson(response.errorBody().charStream(), ApiResponse.class);
                if (error != null && error.getMessage() != null && !error.getMessage().isEmpty()) {
                    return error.getMessage();
                }
            }
        } catch (Exception ignored) {
        }
        return "HTTP " + response.code();
    }
}
