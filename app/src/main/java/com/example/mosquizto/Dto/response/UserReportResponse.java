package com.example.mosquizto.Dto.response;

import com.example.mosquizto.Util.NotificationWrapper;
import com.example.mosquizto.Util.ReportStatus;

public class UserReportResponse implements NotificationWrapper {

    public static final int VIEW_TYPE = 4;

    private Integer id;
    private Integer reportedUserId;
    private Integer reporterId;
    private String reason;
    private String description;
    private ReportStatus status;
    private String createAt;
    private String updateAt;
    private Long notificationId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getReportedUserId() {
        return reportedUserId;
    }

    public void setReportedUserId(Integer reportedUserId) {
        this.reportedUserId = reportedUserId;
    }

    public Integer getReporterId() {
        return reporterId;
    }

    public void setReporterId(Integer reporterId) {
        this.reporterId = reporterId;
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

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
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

    @Override
    public int getType() {
        return VIEW_TYPE;
    }

    @Override
    public Long getNotificationId() {
        return notificationId;
    }

    @Override
    public void setNotificationId(Long id) {
        this.notificationId = id;
    }
}
