package com.example.mosquizto.Dto.request;

public class CollectionReportRequest {
    private String reason;
    private String description;

    public CollectionReportRequest(String reason, String description) {
        this.reason = reason;
        this.description = description;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}