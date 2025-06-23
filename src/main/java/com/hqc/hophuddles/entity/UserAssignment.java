package com.hqc.hophuddles.entity;

import com.hqc.hophuddles.enums.Discipline;
import com.hqc.hophuddles.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_assignments", indexes = {
        @Index(name = "idx_assignment_user_agency", columnList = "user_id, agency_id, is_active"),
        @Index(name = "idx_assignment_agency_role", columnList = "agency_id, role, is_active"),
        @Index(name = "idx_assignment_user_primary", columnList = "user_id, is_primary, is_active"),
        @Index(name = "idx_assignment_discipline", columnList = "discipline, is_active"),
        @Index(name = "idx_assignment_role", columnList = "role, is_active")
})
public class UserAssignment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private Long assignmentId;

    // User relationship
    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Agency relationship (required)
    @NotNull(message = "Agency is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id", nullable = false)
    private Agency agency;

    // Branch relationship (optional - null means agency-wide access)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    // Team relationship (optional - null means branch-wide access)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(name = "discipline", length = 50)
    private Discipline discipline;

    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private UserRole role;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    // Who assigned this role
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private User assignedBy;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    // Constructors
    public UserAssignment() {}

    public UserAssignment(User user, Agency agency, UserRole role) {
        this.user = user;
        this.agency = agency;
        this.role = role;
        this.assignedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Agency getAgency() { return agency; }
    public void setAgency(Agency agency) { this.agency = agency; }

    public Branch getBranch() { return branch; }
    public void setBranch(Branch branch) { this.branch = branch; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    public Discipline getDiscipline() { return discipline; }
    public void setDiscipline(Discipline discipline) { this.discipline = discipline; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public Boolean getIsPrimary() { return isPrimary; }
    public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }

    public User getAssignedBy() { return assignedBy; }
    public void setAssignedBy(User assignedBy) { this.assignedBy = assignedBy; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    // Business methods
    public boolean hasAgencyAccess() {
        return branch == null && team == null;
    }

    public boolean hasBranchAccess() {
        return branch != null && team == null;
    }

    public boolean hasTeamAccess() {
        return team != null;
    }

    public String getAccessScope() {
        if (hasTeamAccess()) return "TEAM";
        if (hasBranchAccess()) return "BRANCH";
        return "AGENCY";
    }

    // Role-based permission helpers
    public boolean canCreateContent() {
        return role.canCreateContent();
    }

    public boolean canManageUsers() {
        return role.canManageUsers();
    }

    public boolean canViewAnalytics() {
        return role.canViewAnalytics();
    }

    @Override
    public String toString() {
        return "UserAssignment{" +
                "assignmentId=" + assignmentId +
                ", role=" + role +
                ", discipline=" + discipline +
                ", isPrimary=" + isPrimary +
                '}';
    }
}