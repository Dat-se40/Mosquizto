package com.example.mosquizto.Dto.response;

import com.example.mosquizto.Util.SearchResultWrapper;
import com.example.mosquizto.Util.UserStatus;
import java.io.Serializable;

public class UserResponse implements Serializable , SearchResultWrapper {
    private Long id;
    private String fullName;
    private String email;
    private String username;
    private UserStatus status;
    private String role;
    private String createdAt;
    private String updatedAt;
    private long followersCount;
    private long followingCount;
    private boolean followed;

    // Getters
    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public UserStatus getStatus() { return status; }
    public String getRole() { return role; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public long getFollowersCount() { return followersCount; }
    public long getFollowingCount() { return followingCount; }
    public boolean isFollowed() { return followed; }

    @Override
    public String toString() {
        return "UserResponse{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", status=" + status +
                ", role='" + role + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }

    @Override
    public int getType() {
        return 2;
    }
}