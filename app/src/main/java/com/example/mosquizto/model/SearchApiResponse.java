package com.example.mosquizto.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SearchApiResponse {

    @SerializedName("status")
    private int status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private SearchPaginatedData data;

    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public SearchPaginatedData getData() { return data; }

    // ===== Inner class map với SearchResultPaginated =====
    public static class SearchPaginatedData {

        @SerializedName("hits")
        private List<SearchResultItem> hits;

        @SerializedName("page")
        private int page;

        @SerializedName("hitsPerPage")
        private int hitsPerPage;

        @SerializedName("totalHits")
        private int totalHits;

        @SerializedName("totalPages")
        private int totalPages;

        public List<SearchResultItem> getHits() { return hits; }
        public int getPage() { return page; }
        public int getHitsPerPage() { return hitsPerPage; }
        public int getTotalHits() { return totalHits; }
        public int getTotalPages() { return totalPages; }
    }
}