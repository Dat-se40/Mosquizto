package com.example.mosquizto.Models;

import com.example.mosquizto.Models.User;

public class Collection {
    private int id;
    private String title;
    private int count;
    private User createdBy;
    private int progress; // Thêm trường này cho UI Jump Back In

    public Collection(int id, String title, int count, User createdBy, int progress) {
        this.id = id;
        this.title = title;
        this.count = count;
        this.createdBy = createdBy;
        this.progress = progress;
    }

    public String getTitle() { return title; }
    public int getCount() { return count; }
    public User getCreatedBy() { return createdBy; }
    public int getProgress() { return progress; }
}