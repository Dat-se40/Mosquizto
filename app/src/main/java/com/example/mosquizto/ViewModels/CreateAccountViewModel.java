package com.example.mosquizto.ViewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mosquizto.Dto.request.SignupRequest;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Services.SessionManager;
import com.example.mosquizto.Services.itf.UserApi;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class CreateAccountViewModel extends ViewModel {

    public enum UiState { IDLE, LOADING, SUCCESS, ERROR }

    private final MutableLiveData<UiState> uiState = new MutableLiveData<>(UiState.IDLE);
    private String errorMessage = "";

    private final UserApi userApi;
    private final SessionManager sessionManager;

    @Inject
    public CreateAccountViewModel(UserApi userApi, SessionManager sessionManager) {
        this.userApi        = userApi;
        this.sessionManager = sessionManager;
    }

    public LiveData<UiState> getUiState() {
        return uiState;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void signup(String email, String password, String birthdate) {
        uiState.setValue(UiState.LOADING);

        SignupRequest request = new SignupRequest("", "", email, password, password, birthdate);

        userApi.signUp(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Lưu token nếu có — tuỳ chỉnh theo ApiResponse của bạn
                    // sessionManager.saveToken(response.body().getData().getToken());
                    uiState.setValue(UiState.SUCCESS);
                } else {
                    errorMessage = "Đăng ký thất bại. Vui lòng thử lại.";
                    uiState.setValue(UiState.ERROR);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                errorMessage = "Lỗi kết nối: " + t.getMessage();
                uiState.setValue(UiState.ERROR);
            }
        });
    }

    public void resetState() {
        uiState.setValue(UiState.IDLE);
    }
}