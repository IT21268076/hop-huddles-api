package com.hqc.hophuddles.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "branches", indexes = {
        @Index(name = "idx_branch_agency", columnList = "agency_id, is_active")
})
public class Branch extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "branch_id")
    private Long branchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id", nullable = false)
    private Agency agency;

    @NotBlank(message = "Branch name is required")
    @Size(max = 255, message = "Branch name must not exceed 255 characters")
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Size(max = 500, message = "Location must not exceed 500 characters")
    @Column(name = "location", length = 500)
    private String location;

    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Team> teams = new ArrayList<>();

    // Constructors
    public Branch() {}

    public Branch(Agency agency, String name) {
        this.agency = agency;
        this.name = name;
    }

    // Getters and Setters
    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }

    public Agency getAgency() { return agency; }
    public void setAgency(Agency agency) { this.agency = agency; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public List<Team> getTeams() { return teams; }
    public void setTeams(List<Team> teams) { this.teams = teams; }

    // Helper methods
    public void addTeam(Team team) {
        teams.add(team);
        team.setBranch(this);
    }
}