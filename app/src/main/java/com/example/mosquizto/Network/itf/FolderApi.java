package com.example.mosquizto.Network.itf;

import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.FolderResponse;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FolderApi {
    @GET("folder/{folderId}")
    Call<ApiResponse<FolderResponse>> getDetailFolder(@Path("folderId") Long folderId);

    @POST("folder/{folderId}/collection/{collectionId}")
    Call<ApiResponse<FolderResponse>> addCollectionToFolder(@Path("folderId") Long folderId, @Path("collectionId") Integer collectionId);

    @DELETE("folder/{folderId}/collection/{collectionId}")
    Call<ApiResponse<Void>> removeCollectionFromFolder(@Path("folderId") Long folderId, @Path("collectionId") Integer collectionId);

    @DELETE("folder/delete/{folderId}")
    Call<ApiResponse<Void>> deleteFolder(@Path("folderId") Long folderId);
}