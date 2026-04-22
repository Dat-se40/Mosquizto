package com.example.mosquizto.Dto.response;

import com.google.gson.annotations.SerializedName;

public class SearchResultItem {

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
    private double count;

    // ===== Getters =====
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public boolean isVisibility() { return visibility; }
    public String getCreatedByUsername() { return createdByUsername; }
    public double getCount() { return count; }

    public String getSubtitle() {
        return count + " thuật ngữ • " + createdByUsername;
    }
}