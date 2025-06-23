// UserAssignmentCreateRequest.java
package com.hqc.hophuddles.dto.request;

import com.hqc.hophuddles.enums.Discipline;
import com.hqc.hophuddles.enums.UserRole;
import jakarta.validation.constraints.NotNull;

public class UserAssignmentCreateRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Agency ID is required")
    private Long agencyId;

    private Long branchId; // Optional

    private Long teamId; // Optional

    private Discipline discipline;

    @NotNull(message = "Role is required")
    private UserRole role;

    private Boolean isPrimary = false;

    // Constructors
    public UserAssignmentCreateRequest() {}

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getAgencyId() { return agencyId; }
    public void setAgencyId(Long agencyId) { this.agencyId = agencyId; }

    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }

    public Discipline getDiscipline() { return discipline; }
    public void setDiscipline(Discipline discipline) { this.discipline = discipline; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public Boolean getIsPrimary() { return isPrimary; }
    public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }
}