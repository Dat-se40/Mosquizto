package com.example.mosquizto.Dto.request;

import org.jetbrains.annotations.NotNull;


public class StartStudySessionRequest {
    @NotNull
    private Integer collectionId;

    public @NotNull Integer getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(@NotNull Integer collectionId) {
        this.collectionId = collectionId;
    }

    public StartStudySessionRequest(@NotNull Integer collectionId) {
        this.collectionId = collectionId;
    }
}
