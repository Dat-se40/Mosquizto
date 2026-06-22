package com.example.mosquizto.ViewModels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Network.itf.UserApi;
import com.example.mosquizto.Util.ApiErrorHelper;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class ResetPassViewModel extends ViewModel {
    private final UserApi userApi;
    private final Context appContext;
    private MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    private MutableLiveData<Boolean> _isResetLinkSent = new MutableLiveData<>();
    public LiveData<Boolean> isResetLinkSet = _isResetLinkSent ;

    @Inject
    public ResetPassViewModel(UserApi userApi, @ApplicationContext Context appContext) {
        this.userApi = userApi;
        this.appContext = appContext;
    }

    public void ForgetPassword(String email)
    {
        userApi.forgotPassword(email).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    _isResetLinkSent.postValue(true);
                } else {
                    _errorMessage.postValue(ApiErrorHelper.extractMessage(response));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                _errorMessage.postValue(ApiErrorHelper.networkError(appContext));
            }
        });
    }
}
