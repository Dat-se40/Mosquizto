package com.example.mosquizto.Dto.response;

import com.example.mosquizto.Util.NotificationWrapper;

public class FollowNotificationResponse implements NotificationWrapper {

    public static final int VIEW_TYPE = 3;

    private Long id;
    private Long followerId;
    private String followerUsername;
    private String followerFullName;
    private String followerImgUri;
    private String followedAt;
    private Long notificationId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFollowerId() {
        return followerId;
    }

    public void setFollowerId(Long followerId) {
        this.followerId = followerId;
    }

    public String getFollowerUsername() {
        return followerUsername;
    }

    public void setFollowerUsername(String followerUsername) {
        this.followerUsername = followerUsername;
    }

    public String getFollowerFullName() {
        return followerFullName;
    }

    public void setFollowerFullName(String followerFullName) {
        this.followerFullName = followerFullName;
    }

    public String getFollowerImgUri() {
        return followerImgUri;
    }

    public void setFollowerImgUri(String followerImgUri) {
        this.followerImgUri = followerImgUri;
    }

    public String getFollowedAt() {
        return followedAt;
    }

    public void setFollowedAt(String followedAt) {
        this.followedAt = followedAt;
    }

    public String getDisplayName() {
        if (followerFullName != null && !followerFullName.trim().isEmpty()) {
            return followerFullName;
        }
        return followerUsername != null ? followerUsername : "Someone";
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
