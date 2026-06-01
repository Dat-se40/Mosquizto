package com.example.mosquizto.Models;

import com.example.mosquizto.Dto.response.CollectionItemResponse;

public class TermSummaryUIModel {
    private CollectionItemResponse itemData;
    private boolean isCorrect;

    public TermSummaryUIModel(CollectionItemResponse itemData, boolean isCorrect) {
        this.itemData = itemData;
        this.isCorrect = isCorrect;
    }

    public CollectionItemResponse getItemData() { return itemData; }
    public void setItemData(CollectionItemResponse itemData) { this.itemData = itemData; }

    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean correct) { isCorrect = correct; }
}
