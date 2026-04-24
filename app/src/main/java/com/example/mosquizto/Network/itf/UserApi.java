package com.example.mosquizto.Network.itf;


import com.example.mosquizto.Dto.request.LoginRequest;
import com.example.mosquizto.Dto.request.SignupRequest;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.LoginResponse;
import com.example.mosquizto.Models.User;
import com.example.mosquizto.Dto.response.UserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface UserApi {
    @POST("auth/login")
    Call<ApiResponse<LoginResponse>> login(@Body LoginRequest request);
    @POST("auth/forgot-password")
    Call<ApiResponse<?>> forgotPassword(@Body String email);
    @POST("auth/register")
    Call<ApiResponse<String>> signUp(@Body SignupRequest signupRequest);
    @POST("auth/logout")
    Call<ApiResponse<String>> logout();
    @GET("user/profile")
    Call<ApiResponse<UserResponse>> getMyProfile();
}
