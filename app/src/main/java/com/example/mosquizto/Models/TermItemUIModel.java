package com.example.mosquizto.Models; // Đổi lại package của bạn nếu cần

import com.example.mosquizto.Dto.response.CollectionItemResponse;

public class TermItemUIModel {
    private CollectionItemResponse itemData;
    private boolean isStarred;

    public TermItemUIModel(CollectionItemResponse itemData, boolean isStarred) {
        this.itemData = itemData;
        this.isStarred = isStarred;
    }

    public CollectionItemResponse getItemData() { return itemData; }
    public void setItemData(CollectionItemResponse itemData) { this.itemData = itemData; }

    public boolean isStarred() { return isStarred; }
    public void setStarred(boolean starred) { this.isStarred = starred; }
}