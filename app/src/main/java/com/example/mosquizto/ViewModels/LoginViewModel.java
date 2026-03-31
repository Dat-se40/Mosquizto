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
    private final SessionManager sessionManager;

    // LiveData để Activity "quan sát"
    private MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    @Inject
    public LoginViewModel(UserApi userApi, SessionManager sessionManager) {
        this.userApi = userApi;
        this.sessionManager = sessionManager;
    }

    public void login(String user, String pass) {
        _isLoading.setValue(true);
        userApi.login(new LoginRequest(user, pass)).enqueue(new Callback<ApiResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoginResponse>> call, Response<ApiResponse<LoginResponse>> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    // chống chế
                    sessionManager.saveToken(response.body().getData().getAccessToken());
                    sessionManager.setCurrentUserProfile(new LoginRequest(user,pass));
                } else {
                    _errorMessage.setValue(response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Lỗi kết nối!");
            }
        });
    }
}
