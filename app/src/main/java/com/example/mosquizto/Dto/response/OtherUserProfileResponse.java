package com.example.mosquizto.Dto.response;

import java.io.Serializable;

public class OtherUserProfileResponse implements Serializable {
    private String fullName;
    private String username;
    private boolean followed;
    private long followersCount;
    private long followingCount;

    // Getters and Setters
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isFollowed() {
        return followed;
    }

    public void setFollowed(boolean followed) {
        this.followed = followed;
    }

    public long getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(long followersCount) {
        this.followersCount = followersCount;
    }

    public long getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(long followingCount) {
        this.followingCount = followingCount;
    }

    @Override
    public String toString() {
        return "OtherUserProfileResponse{" +
                "fullName='" + fullName + '\'' +
                ", username='" + username + '\'' +
                ", followed=" + followed +
                ", followersCount=" + followersCount +
                ", followingCount=" + followingCount +
                '}';
    }
}
