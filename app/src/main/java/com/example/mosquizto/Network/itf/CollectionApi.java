package com.example.mosquizto.Network.itf;

import com.example.mosquizto.Dto.response.SearchApiResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CollectionApi {
    @GET("collection/search")
    Call<SearchApiResponse> searchCollections(
            @Query("q") String q,
            @Query("page") int page,
            @Query("size") int size,
            @Query("author") String author
    );
}
