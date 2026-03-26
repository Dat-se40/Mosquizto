package com.example.mosquizto.Services.itf;


import com.example.mosquizto.Dto.request.LoginRequest;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserApi {
    @POST("auth/login")
    Call<ApiResponse<LoginResponse>> signUp(@Body LoginRequest request);


}
