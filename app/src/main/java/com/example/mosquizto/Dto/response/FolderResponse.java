package com.example.mosquizto.Dto.response;

import java.io.Serializable;
import java.util.List;

public class FolderResponse implements Serializable {
    private Long id;
    private String name;
    private String description;
    private List<CollectionSummaryResponse> collections;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<CollectionSummaryResponse> getCollections() { return collections; }
    public void setCollections(List<CollectionSummaryResponse> collections) { this.collections = collections; }
}
