package com.example.mosquizto.Util;

import android.content.Context;

import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.SearchApiResponse;
import com.example.mosquizto.R;
import com.google.gson.Gson;

import retrofit2.Response;

public final class ApiErrorHelper {

    private static final Gson GSON = new Gson();

    private ApiErrorHelper() {
    }

    public static String networkError(Context context) {
        return context.getString(R.string.ntConnectionError);
    }

    public static String extractMessage(Response<?> response) {
        if (response.body() instanceof ApiResponse) {
            ApiResponse<?> body = (ApiResponse<?>) response.body();
            if (body.getMessage() != null && !body.getMessage().isEmpty()) {
                return body.getMessage();
            }
        }
        if (response.body() instanceof SearchApiResponse) {
            SearchApiResponse body = (SearchApiResponse) response.body();
            if (body.getMessage() != null && !body.getMessage().isEmpty()) {
                return body.getMessage();
            }
        }
        try {
            if (response.errorBody() != null) {
                String raw = response.errorBody().string();
                ApiResponse<?> apiError = GSON.fromJson(raw, ApiResponse.class);
                if (apiError != null && apiError.getMessage() != null && !apiError.getMessage().isEmpty()) {
                    return apiError.getMessage();
                }
                SearchApiResponse searchError = GSON.fromJson(raw, SearchApiResponse.class);
                if (searchError != null && searchError.getMessage() != null && !searchError.getMessage().isEmpty()) {
                    return searchError.getMessage();
                }
            }
        } catch (Exception ignored) {
        }
        return "HTTP " + response.code();
    }
}
