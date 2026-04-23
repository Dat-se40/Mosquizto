package com.example.mosquizto.Network.itf;

import com.example.mosquizto.Dto.response.ApiResponse;
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
    @GET("collections")
    Call<ApiResponse<List<Collection>>> getAllCollections(
            @Query("page") int page,
            @Query("size") int size
    );

    // Lấy chi tiết 1 bộ thẻ theo ID (khi user click vào thẻ ở màn Home)
    @GET("collections/{id}")
    Call<ApiResponse<Collection>> getCollectionById(@Path("id") int id);

    // Lấy danh sách thẻ do user tự tạo (cho mục Library/Thư viện)
    @GET("collections/my-collections")
    Call<ApiResponse<List<Collection>>> getMyCollections();

    @GET("collections/my-collections")
    Call<ApiResponse<PageResponse<Collection>>> getMyCollections(
            @Query("page") int page,
            @Query("size") int size
    );
    // API Tạo bộ sưu tập mới
    @POST("collections")
    Call<ApiResponse<Integer>> createCollection(@Body CollectionRequest request);

    @GET("collection/search")
    Call<SearchApiResponse> searchCollections(
            @Query("q") String q,
            @Query("page") int page,
            @Query("size") int size,
            @Query("author") String author
    );
}