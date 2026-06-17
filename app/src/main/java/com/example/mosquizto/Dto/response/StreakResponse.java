package com.example.mosquizto.Dto.response;

import com.google.gson.annotations.SerializedName;

public class StreakResponse {

    @SerializedName("currentStreakDays")
    private int currentStreakDays;

    @SerializedName("longestStreakDays")
    private int longestStreakDays;

    @SerializedName("totalStudyDays")
    private int totalStudyDays;

    @SerializedName("totalStudySessions")
    private int totalStudySessions;

    @SerializedName("completedStudySessions")
    private int completedStudySessions;

    @SerializedName("lastStudiedAt")
    private String lastStudiedAt;

    @SerializedName("studiedToday")
    private boolean studiedToday;

    @SerializedName("nextMilestoneDays")
    private int nextMilestoneDays;

    public int getCurrentStreakDays() { return currentStreakDays; }
    public int getLongestStreakDays() { return longestStreakDays; }
    public int getTotalStudyDays()    { return totalStudyDays; }
    public int getTotalStudySessions(){ return totalStudySessions; }
    public int getCompletedStudySessions() { return completedStudySessions; }
    public String getLastStudiedAt()  { return lastStudiedAt; }
    public boolean isStudiedToday()   { return studiedToday; }
    public int getNextMilestoneDays() { return nextMilestoneDays; }
}