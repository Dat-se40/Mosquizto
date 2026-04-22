package com.example.mosquizto.model;

public class RecentSearchItem {
    private String text;

    public RecentSearchItem(String text) {
        this.text = text;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}