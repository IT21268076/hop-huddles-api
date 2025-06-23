package com.hqc.hophuddles.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_auth0", columnList = "auth0_id", unique = true),
        @Index(name = "idx_user_email", columnList = "email", unique = true),
        @Index(name = "idx_user_active", columnList = "is_active"),
        @Index(name = "idx_user_last_login", columnList = "last_login")
})
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @NotBlank(message = "Auth0 ID is required")
    @Size(max = 255, message = "Auth0 ID must not exceed 255 characters")
    @Column(name = "auth0_id", nullable = false, unique = true, length = 255)
    private String auth0Id;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Size(max = 50, message = "Phone must not exceed 50 characters")
    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    // Profile picture from Auth0 or uploaded
    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;

    // Bidirectional relationship with UserAssignment
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserAssignment> assignments = new ArrayList<>();

    // Constructors
    public User() {}

    public User(String auth0Id, String email, String name) {
        this.auth0Id = auth0Id;
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

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

    public List<UserAssignment> getAssignments() { return assignments; }
    public void setAssignments(List<UserAssignment> assignments) { this.assignments = assignments; }

    // Helper methods
    public void addAssignment(UserAssignment assignment) {
        assignments.add(assignment);
        assignment.setUser(this);
    }

    public void removeAssignment(UserAssignment assignment) {
        assignments.remove(assignment);
        assignment.setUser(null);
    }

    // Business methods for multi-tenancy
    public UserAssignment getPrimaryAssignment() {
        return assignments.stream()
                .filter(assignment -> assignment.getIsPrimary() && assignment.getIsActive())
                .findFirst()
                .orElse(null);
    }

    public List<UserAssignment> getActiveAssignments() {
        return assignments.stream()
                .filter(UserAssignment::getIsActive)
                .toList();
    }

    public List<Long> getAccessibleAgencyIds() {
        return getActiveAssignments().stream()
                .map(assignment -> assignment.getAgency().getAgencyId())
                .distinct()
                .toList();
    }

    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}