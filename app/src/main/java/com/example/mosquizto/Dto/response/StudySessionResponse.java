package com.example.mosquizto.Dto.response;

import java.util.Date;

public class StudySessionResponse {
    private Long sessionId;
    private String collectionName;
    private Integer collectionId ;
    private Integer collectionCount ;
    private Integer totalScore;
    private Integer totalCorrect;
    private Integer totalWrong;
    private Date startedAt;
    private Date completedAt;

    public Long getSessionId() {
        return sessionId;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public Integer getCollectionId() {
        return collectionId;
    }

    public Integer getCollectionCount() {
        return collectionCount;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public Integer getTotalCorrect() {
        return totalCorrect;
    }

    public Integer getTotalWrong() {
        return totalWrong;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public Date getCompletedAt() {
        return completedAt;
    }
}
