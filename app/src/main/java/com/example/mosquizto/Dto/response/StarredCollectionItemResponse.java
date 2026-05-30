package com.example.mosquizto.Dto.response;

public class StarredCollectionItemResponse {
    Integer itemId;
    Integer collectionId ;

    String term ;
    String definition ;

    String imageUrl ;

    Integer orderIndex ;

    String starredAt ;

    public Integer getItemId() {
        return itemId;
    }
}
