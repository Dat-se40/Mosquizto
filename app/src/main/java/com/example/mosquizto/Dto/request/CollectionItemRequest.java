package com.example.mosquizto.Dto.request;

import org.jetbrains.annotations.NotNull;

/**
 * DTO đại diện cho một thẻ (Term/Definition) khi gửi lên Server
 */
public class CollectionItemRequest {
    private String term;
    private String definition;

    private String imageUrl;

    private Integer orderIndex;

    private Integer collectionId;
    public CollectionItemRequest(String term, String definition) {
        this.term = term;
        this.definition = definition;
    }

    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }

    public String getDefinition() { return definition; }
    public void setDefinition(String definition) { this.definition = definition; }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public Integer getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Integer collectionId) {
        this.collectionId = collectionId;
    }

    @Override
    public String toString() {
        return "CollectionItemRequest{" +
                "term='" + term + '\'' +
                ", definition='" + definition + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", orderIndex=" + orderIndex +
                ", collectionId=" + collectionId +
                '}';
    }
}