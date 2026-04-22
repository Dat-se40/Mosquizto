package com.example.mosquizto.repository;

import com.example.mosquizto.model.SearchApiResponse;
import com.example.mosquizto.network.ApiService;
import com.example.mosquizto.network.RetrofitClient;

import retrofit2.Callback;

public class SearchRepository {

    private final ApiService apiService;

    public SearchRepository() {
        apiService = RetrofitClient.getInstance().create(ApiService.class);
    }

    public void search(String query, int page, int size, String author,
                       Callback<SearchApiResponse> callback) {
        apiService.searchCollections(query, page, size, author).enqueue(callback);
    }
}