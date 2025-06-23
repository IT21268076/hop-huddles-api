// UserCreateRequest.java
package com.hqc.hophuddles.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserCreateRequest {

    @NotBlank(message = "Auth0 ID is required")
    private String auth0Id;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Size(max = 50, message = "Phone must not exceed 50 characters")
    private String phone;

    private String profilePictureUrl;

    // Constructors
    public UserCreateRequest() {}

    // Getters and Setters
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
}