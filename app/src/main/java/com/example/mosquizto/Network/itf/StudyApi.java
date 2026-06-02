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
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface StudyApi {
    // Lấy danh sách các bộ thẻ mở gần đây (Recents)
    @GET("study/recents")
    Call<ApiResponse<List<Collection>>> getRecents();


    @POST("study-session/start")
    Call<ApiResponse<Long>> startStudySession(
            @Header("Idempotency-Key") String idempotencyKey, // Thêm dòng này
            @Body StartStudySessionRequest request
    );

//     sau khi hoàn thành thì gửi kết quả lên server
    @POST("study-session/{sessionId}/complete-batch")
    Call<ApiResponse<StudySessionResultResponse>> completeStudySession(@Path("sessionId") Long sessionId,
                                                                       @Body List<StudySessionDetailRequest> request,
                                                                        @Query("isFullTest ") Boolean isFullTest);

//    @POST("study-sessions/{sessionId}/complete")
//    Call<ApiResponse<Void>> completeStudySession(
//            @Path("sessionId") Long sessionId,
//            @Body List<StudySessionDetailRequest> details
//    );

    @GET("study-session/get-jump-back-in")
    Call<ApiResponse<List<StudySessionResponse>>> getJumpBackIn();

    @DELETE("study-session/{id}")
    Call<ApiResponse<Void>> deleteStudySession(@Path("id") Long id);
}