package com.hqc.hophuddles.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class BranchCreateRequest {

    @NotNull(message = "Agency ID is required")
    private Long agencyId;

    @NotBlank(message = "Branch name is required")
    @Size(max = 255, message = "Branch name must not exceed 255 characters")
    private String name;

    @Size(max = 500, message = "Location must not exceed 500 characters")
    private String location;

    // Constructors
    public BranchCreateRequest() {}

    public BranchCreateRequest(Long agencyId, String name, String location) {
        this.agencyId = agencyId;
        this.name = name;
        this.location = location;
    }

    // Getters and Setters
    public Long getAgencyId() { return agencyId; }
    public void setAgencyId(Long agencyId) { this.agencyId = agencyId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
