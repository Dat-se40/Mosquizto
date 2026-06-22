package com.example.mosquizto.Dto.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CollectionSummaryResponse implements Serializable {
    private Integer id;
    private String title;
    private Integer orderIndex;
    private String userName;
    private Integer count;

    @SerializedName("createdByUsername")
    private String createdByUsername;

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

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public String getUserName() {
        if (userName != null && !userName.isEmpty()) {
            return userName;
        }
        return createdByUsername;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
