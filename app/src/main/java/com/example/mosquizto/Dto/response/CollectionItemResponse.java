package com.example.mosquizto.Dto.response;

import android.os.Parcel;
import android.os.Parcelable;

public class CollectionItemResponse implements Parcelable {
    private Integer id ;
    private String term ;
    private String definition ;
    private String imageUrl ;
    private Integer orderIndex ;
    private Integer collectionId ;
    private String createAt ;
    private String updateAt ;

    public CollectionItemResponse() {
    }

    public CollectionItemResponse(Integer id, String term, String definition, String imageUrl, Integer orderIndex, Integer collectionId, String createAt, String updateAt) {
        this.id = id;
        this.term = term;
        this.definition = definition;
        this.imageUrl = imageUrl;
        this.orderIndex = orderIndex;
        this.collectionId = collectionId;
        this.createAt = createAt;
        this.updateAt = updateAt;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

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

    public String getCreateAt() {
        return createAt;
    }

    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }

    public String getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(String updateAt) {
        this.updateAt = updateAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    protected CollectionItemResponse(Parcel in) {
        if (in.readByte() == 0) id = null;
        else id = in.readInt();
        term = in.readString();
        definition = in.readString();
        imageUrl = in.readString();
        if (in.readByte() == 0) orderIndex = null;
        else orderIndex = in.readInt();
        if (in.readByte() == 0) collectionId = null;
        else collectionId = in.readInt();
        createAt = in.readString();
        updateAt = in.readString();
    }

    public static final Creator<CollectionItemResponse> CREATOR = new Creator<CollectionItemResponse>() {
        @Override
        public CollectionItemResponse createFromParcel(Parcel in) {
            return new CollectionItemResponse(in);
        }

        @Override
        public CollectionItemResponse[] newArray(int size) {
            return new CollectionItemResponse[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) dest.writeByte((byte) 0);
        else {
            dest.writeByte((byte) 1);
            dest.writeInt(id);
        }
        dest.writeString(term);
        dest.writeString(definition);
        dest.writeString(imageUrl);
        if (orderIndex == null) dest.writeByte((byte) 0);
        else {
            dest.writeByte((byte) 1);
            dest.writeInt(orderIndex);
        }
        if (collectionId == null) dest.writeByte((byte) 0);
        else {
            dest.writeByte((byte) 1);
            dest.writeInt(collectionId);
        }
        dest.writeString(createAt);
        dest.writeString(updateAt);
    }

    @Override
    public int describeContents() { return 0; }
}
