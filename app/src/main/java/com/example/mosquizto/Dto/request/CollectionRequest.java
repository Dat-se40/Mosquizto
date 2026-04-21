package com.example.mosquizto.Dto.request;

import java.util.List;

/**
 * DTO đại diện cho toàn bộ bộ thẻ khi gửi lên Server để tạo mới
 */
public class CollectionRequest {
    private String title;
    private String description;
    private String accessStatus; // PUBLIC hoặc PRIVATE
    private List<CollectionItemRequest> items;

    public CollectionRequest(String title, String description, String accessStatus, List<CollectionItemRequest> items) {
        this.title = title;
        this.description = description;
        this.accessStatus = accessStatus;
        this.items = items;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAccessStatus() { return accessStatus; }
    public void setAccessStatus(String accessStatus) { this.accessStatus = accessStatus; }

    public List<CollectionItemRequest> getItems() { return items; }
    public void setItems(List<CollectionItemRequest> items) { this.items = items; }
}