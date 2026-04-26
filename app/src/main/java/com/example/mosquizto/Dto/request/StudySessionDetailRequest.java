package com.example.mosquizto.Dto.request;

public class StudySessionDetailRequest {
    private Long sessionId  ;

    private Integer itemId ;

    private Boolean isCorrect;

    private Double responseTimeMs;

    public Double getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Double responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public Boolean getCorrect() {
        return isCorrect;
    }

    public void setCorrect(Boolean correct) {
        isCorrect = correct;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }
}
