package com.example.mosquizto.ViewModels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mosquizto.Dto.request.LoginRequest;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.LoginResponse;
import com.example.mosquizto.Dto.response.UserResponse;
import com.example.mosquizto.Models.User;
import com.example.mosquizto.Services.SessionManager;
import com.example.mosquizto.Network.itf.UserApi;

import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class LoginViewModel extends ViewModel {
    private final UserApi userApi;
    private final SessionManager sessionManager;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    // LiveData để báo cho Activity biết đã đăng nhập & lấy profile xong
    private final MutableLiveData<Boolean> _loginSuccess = new MutableLiveData<>(false);
    public LiveData<Boolean> loginSuccess = _loginSuccess;

    // Giữ lại loginResult để tương thích với LoginActivity nếu UI vẫn đang observe
    private final MutableLiveData<ApiResponse<LoginResponse>> loginResult = new MutableLiveData<>();
    public LiveData<ApiResponse<LoginResponse>> getLoginResult() {
        return loginResult;
    }

    private String password = "";


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
                if (response.isSuccessful() && response.body() != null) {
                    // Trả dữ liệu về biến loginResult đề phòng Activity vẫn đang dùng
                    loginResult.setValue(response.body());

                    String token = response.body().getData().getAccessToken();
                    String refreshToken = response.body().getData().getRefreshToken();

                    sessionManager.setAccessToken(token);
                    sessionManager.setRefreshToken(refreshToken);
                    password = pass;

                    Log.d("LoginViewModel", "on Success:" + token);

                    // Gọi hàm lấy thông tin người dùng ngay sau khi có token
                    fetchUserProfile();
                } else {
                    _isLoading.setValue(false);
                    _errorMessage.setValue("Sai tài khoản hoặc mật khẩu!");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Lỗi kết nối server! " + t.getMessage() + "\n call: " + call.toString() );
                Log.e("LoginViewModel", "onFailure: " + t.getMessage());
            }
        });
    }

    private void fetchUserProfile() {
        userApi.getMyProfile().enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("LoginViewModel", "on Success:" + response.body().getData());
                    UserResponse rawData = response.body().getData();
                    User userProfile = User.fromResponse(rawData, password);

                    // Lưu User vào SessionManager
                    sessionManager.setCurrUser(userProfile);
                    sessionManager.saveSession(
                            sessionManager.getAccessToken(),
                            userProfile,
                            sessionManager.getRefreshToken() ,
                            rawData.getImgUri()
                    );

                    // Báo UI biết toàn bộ quá trình (Đăng nhập + Lấy Profile) đã thành công
                    _loginSuccess.setValue(true);
                } else {
                    _errorMessage.setValue("Không thể lấy thông tin người dùng!");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Lỗi khi tải profile!");
            }
        });
    }
}