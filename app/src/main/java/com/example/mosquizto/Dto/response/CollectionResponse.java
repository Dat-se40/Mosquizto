package com.example.mosquizto.Dto.response;

import com.example.mosquizto.Models.Collection;

import java.util.Date;

public class CollectionResponse {
    private Integer id;
    private String title;
    private String description;
    private Boolean visibility;
    private Long userId;

    private String userName;
    private Date createdAt;
    private Date updatedAt;
    private Integer count ;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public CollectionResponse(Integer id, Integer count, String title, String description, Boolean visibility, Long userId) {
        this.id = id;
        this.count = count;
        this.title = title;
        this.description = description;
        this.visibility = visibility;
        this.userId = userId;
    }
}
