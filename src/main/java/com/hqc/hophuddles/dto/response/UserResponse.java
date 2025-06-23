// UserResponse.java
package com.hqc.hophuddles.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class UserResponse {
    private Long userId;
    private String auth0Id;
    private String email;
    private String name;
    private String phone;
    private String profilePictureUrl;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private List<UserAssignmentResponse> assignments;

    // Constructors
    public UserResponse() {}

    public UserResponse(Long userId, String email, String name) {
        this.userId = userId;
        this.email = email;
        this.name = name;
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getAuth0Id() { return auth0Id; }
    public void setAuth0Id(String auth0Id) { this.auth0Id = auth0Id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<UserAssignmentResponse> getAssignments() { return assignments; }
    public void setAssignments(List<UserAssignmentResponse> assignments) { this.assignments = assignments; }
}