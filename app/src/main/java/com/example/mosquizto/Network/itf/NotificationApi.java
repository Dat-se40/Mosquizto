package com.example.mosquizto.Network.itf;

import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.NotificationResponse;
import com.example.mosquizto.Dto.response.PageResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NotificationApi {
    @GET("notifications")
    Call<ApiResponse<PageResponse<NotificationResponse>>> getMyNotifications(
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("notifications/unread-count")
    Call<ApiResponse<Long>> getUnreadCount();

    @PATCH("notifications/{id}/read")
    Call<ApiResponse<Void>> markAsRead(
            @Path("id") Long id
    );
}
