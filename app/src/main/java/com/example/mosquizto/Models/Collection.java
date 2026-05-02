package com.example.mosquizto.Models;

import com.example.mosquizto.Dto.response.CollectionResponse;
import com.example.mosquizto.Models.User;

public class Collection {
    private int id;
    private String title;
    private int count;
    private User createdBy;


    public Collection(int id, String title, int count, User createdBy) {
        this.id = id;
        this.title = title;
        this.count = count;
        this.createdBy = createdBy;
    }

    public String getTitle() { return title; }
    public int getCount() { return count; }
    public User getCreatedBy() { return createdBy; }
    public static Collection fromResponse(CollectionResponse res) {
        if (res == null) return null;

        User user = new User();
        user.setId((res.getUserId() != null ? res.getUserId() : 0));
        user.setUsername(res.getUserId() != null ? res.getUserName() : "Unknown user");
        return new Collection(
                res.getId() != null ? res.getId() : 0,
                res.getTitle() != null ? res.getTitle() : "Untitled",
                res.getCount() != null ? res.getCount() : 0,
                user
        );
    }
    public static CollectionResponse toResponse(Collection collection)
    {
        return new CollectionResponse(collection.getId(), collection.getTitle(),
                null, null, collection.getCreatedBy().getId(), collection.getCreatedBy().getUsername(), collection.getCount());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
}