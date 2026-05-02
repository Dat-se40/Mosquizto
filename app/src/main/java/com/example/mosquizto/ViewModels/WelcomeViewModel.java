package com.example.mosquizto.ViewModels;

import android.se.omapi.Session;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.UserResponse;
import com.example.mosquizto.Network.itf.UserApi;
import com.example.mosquizto.Services.SessionManager;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class WelcomeViewModel extends ViewModel {

    private final MutableLiveData<String> _navigateTo = new MutableLiveData<>();
    public LiveData<String> navigateTo = _navigateTo;

    private final MutableLiveData<String> userName = new MutableLiveData<>() ;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;
    public LiveData<String> UserName = userName ;
    private final SessionManager sessionManager ;
    private final UserApi userApi ;
    @Inject
    public WelcomeViewModel(SessionManager sessionManager, UserApi userApi)  {
        this.sessionManager = sessionManager;
        this.userApi = userApi;
    }
//    public void onGoogleClicked() {
//        _navigateTo.setValue("register");
//    }

    public void onEmailClicked() {
        _navigateTo.setValue("register");
    }

    public void onLoginClicked() {
        _navigateTo.setValue("login");
    }

    public void onNavigationDone() {
        _navigateTo.setValue(null);
    }
    public void onWelcomeBackClicked()
    {
        userApi.getMyProfile().enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                if (response != null && response.body() != null)
                {
                    _navigateTo.setValue("main");
                }else
                {
                    //Ko có user profile do token hết hạn
                    _errorMessage.postValue("You are not logged in");
                    sessionManager.logout();
                    userName.setValue("");
                    Log.e("DEBUG_WELCOME", "API Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                Log.e("DEBUG_WELCOME", "API Error: " + t.getMessage());
            }
        }) ;
    }
    public void AfterCreateView()
    {
        if(sessionManager != null && sessionManager.getCurrUser() != null)
        {
            userName.postValue(sessionManager.getCurrUser().getUsername());
        }
    }
}