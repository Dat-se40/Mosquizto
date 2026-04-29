package com.example.mosquizto.Dto.request;

import java.util.List;

/**
 * DTO đại diện cho toàn bộ bộ thẻ khi gửi lên Server để tạo mới
 */
public class CollectionRequest {
    private String title;
    private String description;

    private Boolean visibility;

    public CollectionRequest(String title, String description, Boolean visibility) {
        this.title = title;
        this.description = description;
        this.visibility = visibility;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getVisibility() {
        return visibility;
    }

    public void setVisibility(Boolean visibility) {
        this.visibility = visibility;
    }
}