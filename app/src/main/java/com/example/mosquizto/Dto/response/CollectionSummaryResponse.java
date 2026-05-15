package com.example.mosquizto.Dto.response;

import java.io.Serializable;

public class CollectionSummaryResponse implements Serializable {
    private Integer id;
    private String title;
    private Integer orderIndex;

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
}
