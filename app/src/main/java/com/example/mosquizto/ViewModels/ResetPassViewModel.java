package com.example.mosquizto.ViewModels;

import static android.provider.Settings.System.getString;

import static com.example.mosquizto.R.string.lostConnectionNoti;

import android.content.Context;
import android.content.Intent;

import androidx.compose.runtime.external.kotlinx.collections.immutable.implementations.persistentOrderedMap.LinkedValue;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mosquizto.Activities.RSPassNoti;
import com.example.mosquizto.Activities.ResetPass;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.R;
import com.example.mosquizto.Services.itf.UserApi;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class ResetPassViewModel extends ViewModel {
    private final UserApi userApi;
    private MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    private MutableLiveData<Boolean> _isResetLinkSent = new MutableLiveData<>();
    public LiveData<Boolean> isResetLinkSet = _isResetLinkSent ;
    @Inject
    public ResetPassViewModel(UserApi userApi) {
        this.userApi = userApi;
    }
    public void ForgetPassword(String email)
    {
        userApi.forgotPassword(email).enqueue(new Callback<ApiResponse<?>>() {
            @Override
            public void onResponse(Call<ApiResponse<?>> call, Response<ApiResponse<?>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    _isResetLinkSent.postValue(true);
                } else {
                    _errorMessage.postValue("Email không tồn tại hoặc lỗi server");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<?>> call, Throwable t) {
                _errorMessage.postValue("Lost connection");
            }
        });
    }
}
