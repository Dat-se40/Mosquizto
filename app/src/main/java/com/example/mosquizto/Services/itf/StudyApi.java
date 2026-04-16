package com.example.mosquizto.Services.itf;

import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Models.Collection;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface StudyApi {

    // Lấy danh sách các bộ thẻ đang học dở (Jump back in)
    @GET("study/jump-back-in")
    Call<ApiResponse<List<Collection>>> getJumpBackIn();

    // Lấy danh sách các bộ thẻ mở gần đây (Recents)
    @GET("study/recents")
    Call<ApiResponse<List<Collection>>> getRecents();

    // Gợi ý học tập (Based on your recent studying)
    @GET("study/recommendations")
    Call<ApiResponse<List<Collection>>> getRecommendations();
}