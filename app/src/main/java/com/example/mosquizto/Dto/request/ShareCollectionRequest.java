package com.example.mosquizto.Dto.request;

import com.example.mosquizto.Util.CollectionRole;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class ShareCollectionRequest implements Serializable {

    private String username;

    private CollectionRole role;

    public ShareCollectionRequest(String username, CollectionRole role) {
        this.username = username;
        this.role = role;
    }
}
