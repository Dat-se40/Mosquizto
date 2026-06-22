package com.example.mosquizto.ViewModels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mosquizto.Dto.request.ResetPasswordRequest;
import com.example.mosquizto.Dto.request.VerifyCodeRequest;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.ResetPasswordTokenResponse;
import com.example.mosquizto.Network.itf.UserApi;
import com.example.mosquizto.R;
import com.example.mosquizto.Util.ApiErrorHelper;
import com.google.gson.Gson;

import java.io.IOException;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class ResetPassViewModel extends ViewModel {
    private final UserApi userApi;
    private final Context appContext;
    private final Gson gson = new Gson();

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;

    private final MutableLiveData<Boolean> _isOtpSent = new MutableLiveData<>(false);
    public final LiveData<Boolean> isOtpSent = _isOtpSent;

    private final MutableLiveData<Boolean> _isOtpVerified = new MutableLiveData<>(false);
    public final LiveData<Boolean> isOtpVerified = _isOtpVerified;

    private final MutableLiveData<Boolean> _isPasswordReset = new MutableLiveData<>(false);
    public final LiveData<Boolean> isPasswordReset = _isPasswordReset;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;

    // Kept only in this ViewModel's memory. It is cleared after a successful reset.
    private String resetSecretKey;

    @Inject
    public ResetPassViewModel(UserApi userApi, @ApplicationContext Context appContext) {
        this.userApi = userApi;
        this.appContext = appContext;
    }

    public void sendOtp(String email) {
        beginRequest();
        RequestBody emailBody = RequestBody.create(
                email,
                MediaType.get("application/json; charset=utf-8")
        );
        userApi.forgotPassword(emailBody).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                _isLoading.postValue(false);
                if (response.isSuccessful()) {
                    _isOtpSent.postValue(true);
                } else {
                    postApiError(response);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable throwable) {
                postNetworkError();
            }
        });
    }

    public void verifyOtp(String email, String code) {
        beginRequest();
        VerifyCodeRequest request = new VerifyCodeRequest(email, code);
        userApi.verifyForgotPasswordCode(request)
                .enqueue(new Callback<ApiResponse<ResetPasswordTokenResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<ResetPasswordTokenResponse>> call,
                            Response<ApiResponse<ResetPasswordTokenResponse>> response
                    ) {
                        _isLoading.postValue(false);
                        ApiResponse<ResetPasswordTokenResponse> body = response.body();
                        ResetPasswordTokenResponse token = body == null ? null : body.getData();
                        if (response.isSuccessful()
                                && token != null
                                && token.getSecretKey() != null
                                && !token.getSecretKey().isEmpty()) {
                            resetSecretKey = token.getSecretKey();
                            _isOtpVerified.postValue(true);
                        } else if (response.isSuccessful()) {
                            _errorMessage.postValue(appContext.getString(R.string.reset_error_invalid_token));
                        } else {
                            postApiError(response);
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<ResetPasswordTokenResponse>> call,
                            Throwable throwable
                    ) {
                        postNetworkError();
                    }
                });
    }

    public void resetPassword(String newPassword, String confirmPassword) {
        if (resetSecretKey == null || resetSecretKey.isEmpty()) {
            _errorMessage.setValue(appContext.getString(R.string.reset_error_invalid_token));
            return;
        }

        beginRequest();
        ResetPasswordRequest request = new ResetPasswordRequest(
                resetSecretKey,
                newPassword,
                confirmPassword
        );
        userApi.resetPassword(request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                _isLoading.postValue(false);
                if (response.isSuccessful()) {
                    resetSecretKey = null;
                    _isPasswordReset.postValue(true);
                } else {
                    postApiError(response);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable throwable) {
                postNetworkError();
            }
        });
    }

    public void consumeOtpSent() {
        _isOtpSent.setValue(false);
    }

    public void consumeOtpVerified() {
        _isOtpVerified.setValue(false);
    }

    public void consumePasswordReset() {
        _isPasswordReset.setValue(false);
    }

    public boolean hasResetAuthorization() {
        return resetSecretKey != null && !resetSecretKey.isEmpty();
    }

    public void discardResetAuthorization() {
        resetSecretKey = null;
        _isOtpVerified.setValue(false);
    }

    private void beginRequest() {
        _errorMessage.setValue(null);
        _isLoading.setValue(true);
    }

    private void postNetworkError() {
        _isLoading.postValue(false);
        _errorMessage.postValue(ApiErrorHelper.networkError(appContext));
    }

    private void postApiError(Response<?> response) {
        String retryAfter = response.headers().get("Retry-After");
        ErrorPayload payload = readErrorPayload(response.errorBody());
        String code = payload == null ? null : payload.code;
        String backendMessage = payload == null ? null : payload.message;
        _errorMessage.postValue(mapError(response.code(), code, backendMessage, retryAfter));
    }

    private ErrorPayload readErrorPayload(ResponseBody errorBody) {
        if (errorBody == null) {
            return null;
        }
        try {
            return gson.fromJson(errorBody.string(), ErrorPayload.class);
        } catch (IOException | RuntimeException ignored) {
            return null;
        }
    }

    private String mapError(int httpStatus, String code, String backendMessage, String retryAfter) {
        if ("RATE_LIMIT_EXCEEDED".equals(code) || httpStatus == 429) {
            return formatRateLimitMessage(retryAfter);
        }
        if ("VALIDATION_ERROR".equals(code) || httpStatus == 400) {
            return appContext.getString(R.string.reset_error_validation);
        }
        if ("TOKEN_EXPIRED".equals(code)) {
            return appContext.getString(R.string.reset_error_token_expired);
        }
        if ("INVALID_TOKEN".equals(code)) {
            return appContext.getString(R.string.reset_error_invalid_token);
        }
        if ("RESOURCE_NOT_FOUND".equals(code) || httpStatus == 404) {
            return appContext.getString(R.string.reset_error_email_not_found);
        }
        if ("INVALID_VERIFICATION_CODE".equals(code)) {
            return appContext.getString(R.string.reset_error_invalid_otp);
        }
        if ("PASSWORD_MISMATCH".equals(code)) {
            return appContext.getString(R.string.reset_error_password_mismatch);
        }
        if (backendMessage != null && !backendMessage.trim().isEmpty()) {
            return backendMessage;
        }
        return "HTTP " + httpStatus;
    }

    private String formatRateLimitMessage(String retryAfter) {
        if (retryAfter == null || retryAfter.trim().isEmpty()) {
            return appContext.getString(R.string.reset_error_rate_limit_generic);
        }
        try {
            long seconds = Math.max(0, Long.parseLong(retryAfter.trim()));
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            String waitTime = minutes > 0
                    ? appContext.getString(R.string.reset_wait_minutes, minutes, remainingSeconds)
                    : appContext.getString(R.string.reset_wait_seconds, remainingSeconds);
            return appContext.getString(R.string.reset_error_rate_limit, waitTime);
        } catch (NumberFormatException ignored) {
            return appContext.getString(R.string.reset_error_rate_limit, retryAfter);
        }
    }

    private static class ErrorPayload {
        String code;
        String message;
    }
}
