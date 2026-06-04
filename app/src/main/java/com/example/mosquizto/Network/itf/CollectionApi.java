package com.example.mosquizto.Network.itf;

import com.example.mosquizto.Dto.request.CollectionItemRequest;
import com.example.mosquizto.Dto.request.CollectionReportRequest;
import com.example.mosquizto.Dto.request.ShareCollectionRequest;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.CollectionItemResponse;
import com.example.mosquizto.Dto.response.CollectionReportResponse;
import com.example.mosquizto.Dto.response.CollectionResponse;
import com.example.mosquizto.Dto.response.PageResponse;
import com.example.mosquizto.Dto.response.SearchApiResponse;
import com.example.mosquizto.Dto.response.ShareCollectionResponse;
import com.example.mosquizto.Dto.response.StarredCollectionItemResponse;
import com.example.mosquizto.Models.Collection;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.PUT;
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
    Call<ApiResponse<CollectionResponse>> getCollectionById(@Path("id") int id);

    @POST("user-collection/share/{collectionId}")
    Call<ApiResponse<Void>> shareCollection(@Path("collectionId") int collectionId,@Body ShareCollectionRequest request);

    // Lấy ra danh sách các item của 1 bộ thẻ
    @GET("collection/item/{id}")
    Call<ApiResponse<List<CollectionItemResponse>>> getCollectionItemById(@Path("id") int id);

    // Lấy danh sách thẻ do user tự tạo (cho mục Library/Thư viện)
    @GET("collection/my-collections")
    Call<ApiResponse<List<Collection>>> getMyCollections();

    // lấy ra danh sách bộ thẻ được mở gần đây (Recents)
    @GET("collection/recent-opened")
    Call<ApiResponse<List<CollectionResponse>>> getRecentOpenedCollections();
    @DELETE("user-collection/collection/{collectionId}/recent")
    Call<ApiResponse<Void>> deleteRecentOpenedCollection(@Path("collectionId") int collectionId);
    @GET("collection/my-list")
    Call<ApiResponse<PageResponse<CollectionResponse>>> getMyCollections(
            @Query("page") int page,
            @Query("size") int size
    );
    // API Tạo bộ sưu tập mới
    @POST("collection")
    Call<ApiResponse<Integer>> createCollection(@Body CollectionRequest request);
    @POST("collection/item")
    Call<ApiResponse<CollectionItemResponse>> createCollectionItem(@Body CollectionItemRequest request);
    @GET("collection/search")
    Call<SearchApiResponse> searchCollections(
            @Query("q") String q,
            @Query("page") int page,
            @Query("size") int size,
            @Query("author") String author
    );
    @PUT("collection/{id}") // Thay đổi endpoint cho khớp với BE
    Call<ApiResponse<Integer>> updateCollection(@Path("id") int id, @Body CollectionRequest request);

    // Cập nhật từng Item trong Collection
    @PUT("collection/item/{id}") // Thay đổi endpoint cho khớp với BE
    Call<ApiResponse<CollectionItemResponse>> updateCollectionItem(@Path("id") int id, @Body CollectionItemRequest request);

    @DELETE("collection/{id}")
    Call<ApiResponse<Void>> deleteCollection(@Path("id") int id);
    @DELETE("collection/item/{id}")
    Call<ApiResponse<CollectionItemResponse>> deleteCollectionItem(@Path("id") int id);
    @GET("collection/item/starred")
    Call<ApiResponse<List<StarredCollectionItemResponse>>> getStarredCollections();
    @PUT("collection/item/{id}/star")
    Call<ApiResponse<StarredCollectionItemResponse>> starCollectionItem(@Path("id") int id);
    @DELETE("collection/item/{id}/star")
    Call<ApiResponse<Void>> unstarCollectionItem(@Path("id") int id);

    @POST("reports/collections/{collectionId}")
    Call<ApiResponse<CollectionReportResponse>> reportCollection(@Path("collectionId") int collectionId, @Body CollectionReportRequest request);
    @GET("recommendation/collections")
    Call<ApiResponse<PageResponse<CollectionResponse>>> recommendCollection(@Query("page") int page , @Query("size") int size);

    // Lấy danh sách Report
    @GET("reports/collections") // Sửa lại path theo Controller của bạn
    Call<ApiResponse<List<CollectionReportResponse>>> getMyPendingReports();

    // Xử lý Report
    @PATCH("reports/{reportId}")
    Call<ApiResponse<Void>> processReport(
            @Path("reportId") Long reportId,
            @Query("status") String status // Dùng String (vd: "DISMISSED", "RESOLVED")
    );

    // Lấy danh sách Lời mời
    @GET("user-collection/invitations")
    Call<ApiResponse<List<ShareCollectionResponse>>> getMyInvitations();

    // Xử lý Lời mời
    @PATCH("user-collection/respond/invitation")
    Call<ApiResponse<Void>> respondToInvitation(
            @Query("collectionId") Integer collectionId,
            @Query("accessStatus") String accessStatus
    );
}
