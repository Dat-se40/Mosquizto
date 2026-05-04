package com.example.mosquizto.Dto.response;

import java.io.Serializable;
import java.util.List;

public class FolderResponse implements Serializable {
    private Long id;
    private String title;
    private String description;
    private List<CollectionResponse> collections;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<CollectionResponse> getCollections() { return collections; }
    public void setCollections(List<CollectionResponse> collections) { this.collections = collections; }
}