package com.example.mosquizto.Dto.response;
import java.util.List;
public class PageResponse<T> {
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private List<T> content; // Đây là danh sách dữ liệu thật

    public List<T> getContent() { return content; }
}