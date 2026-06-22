package com.example.mosquizto.ViewModels;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mosquizto.Dto.request.SignupRequest;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Services.SessionManager;
import com.example.mosquizto.Network.itf.UserApi;
import com.example.mosquizto.Util.ApiErrorHelper;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
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
    private final Context appContext;

    @Inject
    public CreateAccountViewModel(UserApi userApi, SessionManager sessionManager,
                                  @ApplicationContext Context appContext) {
        this.userApi        = userApi;
        this.sessionManager = sessionManager;
        this.appContext = appContext;
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
                    _message.postValue(response.body().getData());
                    uiState.setValue(UiState.SUCCESS);
                    Log.e("SIGN_UP", "onResponse: " + response.body().getData());
                } else {
                    _message.postValue(ApiErrorHelper.extractMessage(response));
                    Log.e("SIGN_UP", "onResponse: " + ApiErrorHelper.extractMessage(response));
                    uiState.postValue(UiState.ERROR);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                _message.postValue(ApiErrorHelper.networkError(appContext));
                Log.e("SIGN_UP", "onFailure: " + t.getMessage());
                uiState.setValue(UiState.ERROR);
            }
        });
    }

    public void resetState() {
        uiState.setValue(UiState.IDLE);
    }
}
