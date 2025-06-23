// UserAssignmentResponse.java
package com.hqc.hophuddles.dto.response;

import com.hqc.hophuddles.enums.Discipline;
import com.hqc.hophuddles.enums.UserRole;

import java.time.LocalDateTime;

public class UserAssignmentResponse {
    private Long assignmentId;
    private Long userId;
    private String userName;
    private Long agencyId;
    private String agencyName;
    private Long branchId;
    private String branchName;
    private Long teamId;
    private String teamName;
    private Discipline discipline;
    private UserRole role;
    private Boolean isPrimary;
    private String accessScope;
    private LocalDateTime assignedAt;

    // Constructors
    public UserAssignmentResponse() {}

    // Getters and Setters
    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public Long getAgencyId() { return agencyId; }
    public void setAgencyId(Long agencyId) { this.agencyId = agencyId; }

    public String getAgencyName() { return agencyName; }
    public void setAgencyName(String agencyName) { this.agencyName = agencyName; }

    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public Discipline getDiscipline() { return discipline; }
    public void setDiscipline(Discipline discipline) { this.discipline = discipline; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public Boolean getIsPrimary() { return isPrimary; }
    public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }

    public String getAccessScope() { return accessScope; }
    public void setAccessScope(String accessScope) { this.accessScope = accessScope; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
}