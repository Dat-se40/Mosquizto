package com.example.mosquizto.Network.itf;

import com.example.mosquizto.Dto.request.StartStudySessionRequest;
import com.example.mosquizto.Dto.request.StudySessionDetailRequest;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.StudySessionResponse;
import com.example.mosquizto.Dto.response.StudySessionResultResponse;
import com.example.mosquizto.Models.Collection;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface StudyApi {
    // Lấy danh sách các bộ thẻ mở gần đây (Recents)
    @GET("study/recents")
    Call<ApiResponse<List<Collection>>> getRecents();


    @POST("study-session/start")
    Call<ApiResponse<Long>> startStudySession(@Body StartStudySessionRequest request);

//     sau khi hoàn thành thì gửi kết quả lên server
    @POST("study-session/{sessionId}/complete-bath")
    Call<ApiResponse<StudySessionResultResponse>> completeStudySession(@Path("sessionId") Long sessionId, @Body List<StudySessionDetailRequest> request);

//    @POST("study-sessions/{sessionId}/complete")
//    Call<ApiResponse<Void>> completeStudySession(
//            @Path("sessionId") Long sessionId,
//            @Body List<StudySessionDetailRequest> details
//    );

    @GET("study-session/jump-back-in")
    Call<ApiResponse<List<StudySessionResponse>>> getJumpBackIn();
}