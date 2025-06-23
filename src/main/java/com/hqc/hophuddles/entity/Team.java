package com.hqc.hophuddles.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "teams", indexes = {
        @Index(name = "idx_team_branch", columnList = "branch_id, is_active")
})
public class Team extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id")
    private Long teamId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @NotBlank(message = "Team name is required")
    @Size(max = 255, message = "Team name must not exceed 255 characters")
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column(name = "description", length = 1000)
    private String description;

    // Constructors
    public Team() {}

    public Team(Branch branch, String name) {
        this.branch = branch;
        this.name = name;
    }

    // Getters and Setters
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }

    public Branch getBranch() { return branch; }
    public void setBranch(Branch branch) { this.branch = branch; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}