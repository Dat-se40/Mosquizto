package com.example.mosquizto.Dto.response;

import com.example.mosquizto.Util.SearchResultWrapper;
import com.google.gson.annotations.SerializedName;

public class SearchCollectionResultItem implements SearchResultWrapper {

    @SerializedName("id")
    private Long id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("visibility")
    private boolean visibility;

    @SerializedName("createdByUsername")
    private String createdByUsername;

    @SerializedName("count")
    private Double count;

    // ===== Getters =====
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public boolean isVisibility() { return visibility; }
    public String getCreatedByUsername() { return createdByUsername; }
    public Double getCount() { return count; }

    @Override
    public int getType() {
        return 1;
    }
}