package com.example.mosquizto.Models;

import com.example.mosquizto.Dto.response.UserResponse;
import com.example.mosquizto.Util.UserStatus;

public class User {
    private Long id;
    private String fullName;
    private String email;
    private String username;
    private UserStatus status;
    private String role;
    private String password;


    public User(String username )
    {
        this.username = username ;
    }

    public User(){}
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public static User fromResponse(UserResponse response, String password) {
        User user = new User();
        user.setId(response.getId());
        user.setFullName(response.getFullName());
        user.setEmail(response.getEmail());
        user.setUsername(response.getUsername());
        user.setStatus(response.getStatus());
        user.setRole(response.getRole());
        user.setPassword(password);
        return user;
    }

    public void setId(Long id) { this.id = id; }
    public Long getId() { return id; }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", status=" + status +
                ", role='" + role ;
    }
}