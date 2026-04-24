package com.example.mosquizto.ViewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mosquizto.Dto.request.ResetPasswordRequest;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Network.itf.UserApi;

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
        ResetPasswordRequest request = new ResetPasswordRequest(email);
        userApi.forgotPassword(request).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    _isResetLinkSent.postValue(true);
                } else {
                    _errorMessage.postValue("Email không tồn tại hoặc lỗi server");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                _errorMessage.postValue("Lost connection");
            }
        });
    }
}
