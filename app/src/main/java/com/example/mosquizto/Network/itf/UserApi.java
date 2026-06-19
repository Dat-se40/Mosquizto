package com.example.mosquizto.Network.itf;


import com.example.mosquizto.Dto.request.LoginRequest;
import com.example.mosquizto.Dto.request.ResetPasswordRequest;
import com.example.mosquizto.Dto.request.SignupRequest;
import com.example.mosquizto.Dto.request.UpdateAvatarRequest;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.AvatarResponse;
import com.example.mosquizto.Dto.response.CollectionResponse;
import com.example.mosquizto.Dto.response.LoginResponse;
import com.example.mosquizto.Dto.response.PageResponse;
import com.example.mosquizto.Dto.response.StreakResponse;
import com.example.mosquizto.Models.User;
import com.example.mosquizto.Dto.response.OtherUserProfileResponse;
import com.example.mosquizto.Dto.response.UserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UserApi {
    @POST("auth/login")
    Call<ApiResponse<LoginResponse>> login(@Body LoginRequest request);
    @POST("auth/forgot-password")
    Call<ApiResponse<String>> forgotPassword(@Body ResetPasswordRequest request);
    @POST("auth/register")
    Call<ApiResponse<String>> signUp(@Body SignupRequest signupRequest);
    @POST("auth/logout")
    Call<ApiResponse<String>> logout();
    @GET("user/profile")
    Call<ApiResponse<UserResponse>> getMyProfile();

    @GET("user/search")
    Call<ApiResponse<PageResponse<UserResponse>>> searchUser(@Query("keyword") String keyword ,
                                                                  @Query("page") int page, @Query("size") int size);

    @GET("user/streak")
    Call<ApiResponse<StreakResponse>> getUserStreak();
    @GET("user/profile/{username}")
Call<ApiResponse<OtherUserProfileResponse>> getUserProfile(@Path("username") String username);

    @POST("user/follow/{username}")
    Call<ApiResponse<Void>> followUser(@Path("username") String username);

    @DELETE("user/follow/{username}")
    Call<ApiResponse<Void>> unfollowUser(@Path("username") String username);

    @GET("user/followers")
    Call<ApiResponse<PageResponse<UserResponse>>> getFollowers(@Query("page") int page, @Query("size") int size);

    @GET("user/following")
    Call<ApiResponse<PageResponse<UserResponse>>> getFollowing(@Query("page") int page, @Query("size") int size);

    @GET("user/avatar")
    Call<ApiResponse<AvatarResponse>> getMyAvatar() ;

    @PATCH("user/avatar")
    Call<ApiResponse<String>> updateAvatar(@Body UpdateAvatarRequest request);
}

