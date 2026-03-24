package com.example.mosquizto.services.itf;


import com.example.mosquizto.dto.request.LoginRequest;
import com.example.mosquizto.dto.response.ApiResponse;
import com.example.mosquizto.dto.response.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserApi {
    @POST("auth/login")
    Call<ApiResponse<LoginResponse>> signUp(@Body LoginRequest request);


}
