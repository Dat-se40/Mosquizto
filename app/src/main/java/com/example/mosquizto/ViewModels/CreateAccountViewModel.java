package com.example.mosquizto.ViewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mosquizto.Dto.request.SignupRequest;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Services.SessionManager;
import com.example.mosquizto.Network.itf.UserApi;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class CreateAccountViewModel extends ViewModel {

    public enum UiState { IDLE, LOADING, SUCCESS, ERROR }

    private final MutableLiveData<UiState> uiState = new MutableLiveData<>(UiState.IDLE);
    private final MutableLiveData<String> _message = new MutableLiveData<String>()  ;
    public LiveData<String> message = _message ;
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
        return message.getValue();
    }

    public void signup(String fullName , String userName,String email, String password,
                       String confirmPassword) {
        uiState.setValue(UiState.LOADING);

        SignupRequest request = new SignupRequest(fullName, userName, email, password, confirmPassword);

        userApi.signUp(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Lưu token nếu có — tuỳ chỉnh theo ApiResponse của bạn
                    // sessionManager.saveToken(response.body().getData().getToken());
                    _message.postValue(response.body().getData());
                    uiState.setValue(UiState.SUCCESS);
                } else {
                    _message.postValue("Đăng kí thất bại");
                    uiState.postValue(UiState.ERROR);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                _message.postValue("Đăng kí thất bại");
                uiState.setValue(UiState.ERROR);
            }
        });
    }

    public void resetState() {
        uiState.setValue(UiState.IDLE);
    }
}