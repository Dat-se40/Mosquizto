package com.example.mosquizto.Network.itf;

import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.CollectionItemResponse;
import com.example.mosquizto.Dto.response.CollectionResponse;
import com.example.mosquizto.Dto.response.PageResponse;
import com.example.mosquizto.Dto.response.SearchApiResponse;
import com.example.mosquizto.Models.Collection;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Body;
import retrofit2.http.POST;
import com.example.mosquizto.Dto.request.CollectionRequest;

public interface CollectionApi {

    // Lấy danh sách collection (có phân trang để app không bị lag nếu data lớn)
    @GET("collection/public")
    Call<ApiResponse<List<Collection>>> getAllCollections(
            @Query("page") int page,
            @Query("size") int size
    );

    // Lấy chi tiết 1 thông tin bộ thẻ theo ID
    @GET("collection/{id}")
    Call<ApiResponse<Collection>> getCollectionById(@Path("id") int id);

    // Lấy ra danh sách các item của 1 bộ thẻ
    @GET("collection/item/{id}")
    Call<ApiResponse<List<CollectionItemResponse>>> getCollectionItemById(@Path("id") int id);

    // Lấy danh sách thẻ do user tự tạo (cho mục Library/Thư viện)
    @GET("collection/my-collections")
    Call<ApiResponse<List<Collection>>> getMyCollections();

    // lấy ra danh sách bộ thẻ được mở gần đây (Recents)
    @GET("collection/recent-opened")
    Call<ApiResponse<List<CollectionResponse>>> getRecentOpenedCollections();
    @GET("collection/my-list")
    Call<ApiResponse<PageResponse<CollectionResponse>>> getMyCollections(
            @Query("page") int page,
            @Query("size") int size
    );
    // API Tạo bộ sưu tập mới
    @POST("collection")
    Call<ApiResponse<Integer>> createCollection(@Body CollectionRequest request);

    @GET("collection/search")
    Call<SearchApiResponse> searchCollections(
            @Query("q") String q,
            @Query("page") int page,
            @Query("size") int size,
            @Query("author") String author
    );
}