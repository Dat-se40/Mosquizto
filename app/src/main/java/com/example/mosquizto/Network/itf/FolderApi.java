package com.example.mosquizto.Network.itf;

import com.example.mosquizto.Dto.request.CreateFolderRequest;
import com.example.mosquizto.Dto.request.UpdateFolderRequest;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.FolderResponse;
import com.example.mosquizto.Dto.response.FolderSummaryResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FolderApi {
    // Tạo folder mới cho user hiện tại.
    @POST("folder/create")
    Call<ApiResponse<FolderResponse>> createFolder(@Body CreateFolderRequest request);

    // Lấy danh sách folder của user đang đăng nhập.
    @GET("folder/all")
    Call<ApiResponse<List<FolderSummaryResponse>>> getAllOwnFolders();

    // Lấy chi tiết folder, bao gồm các collection bên trong.
    @GET("folder/{folderId}")
    Call<ApiResponse<FolderResponse>> getDetailFolder(@Path("folderId") Long folderId);

    // Cập nhật tên hoặc mô tả folder.
    @PATCH("folder/{folderId}")
    Call<ApiResponse<FolderResponse>> updateFolder(@Path("folderId") Long folderId, @Body UpdateFolderRequest request);

    // Thêm một collection vào folder.
    @POST("folder/{folderId}/collection/{collectionId}")
    Call<ApiResponse<FolderResponse>> addCollectionToFolder(@Path("folderId") Long folderId, @Path("collectionId") Integer collectionId);

    // Xóa một collection khỏi folder.
    @DELETE("folder/{folderId}/collection/{collectionId}")
    Call<ApiResponse<Void>> removeCollectionFromFolder(@Path("folderId") Long folderId, @Path("collectionId") Integer collectionId);

    // Xóa folder hiện tại.
    @DELETE("folder/delete/{folderId}")
    Call<ApiResponse<Void>> deleteFolder(@Path("folderId") Long folderId);
}
