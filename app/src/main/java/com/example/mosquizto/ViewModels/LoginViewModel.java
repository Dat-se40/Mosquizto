package com.example.mosquizto.ViewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mosquizto.Dto.request.LoginRequest;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.LoginResponse;
import com.example.mosquizto.Services.SessionManager;
import com.example.mosquizto.Services.itf.UserApi;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.lifecycle.HiltViewModel;
import jakarta.inject.Inject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class LoginViewModel extends ViewModel {
    private final UserApi userApi;
    private final MutableLiveData<ApiResponse<LoginResponse>> loginResult = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    @Inject
    public LoginViewModel(UserApi userApi) {
        this.userApi = userApi;
    }
    public LiveData<ApiResponse<LoginResponse>> getLoginResult() {
        return loginResult;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    public void login(String username, String password) {
        LoginRequest request = new LoginRequest(username, password);
        userApi.login(request).enqueue(new Callback<ApiResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoginResponse>> call, Response<ApiResponse<LoginResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    loginResult.setValue(response.body());
                } else {
                    errorMessage.setValue("Đăng nhập thất bại. Vui lòng kiểm tra lại!");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}
