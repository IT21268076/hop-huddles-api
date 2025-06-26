package com.hqc.hophuddles.entity;

import com.hqc.hophuddles.enums.AgencyType;
import com.hqc.hophuddles.enums.SubscriptionPlan;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "agencies", indexes = {
        @Index(name = "idx_agency_ccn", columnList = "ccn", unique = true),
        @Index(name = "idx_agency_type_active", columnList = "agency_type, is_active"),
        @Index(name = "idx_agency_active", columnList = "is_active")
})
public class Agency extends BaseEntity {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agency_id")
    private Long agencyId;

    @NotBlank(message = "Agency name is required")
    @Size(max = 255, message = "Agency name must not exceed 255 characters")
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Pattern(regexp = "^[0-9]{6}$", message = "CCN must be 6 digits")
    @Column(name = "ccn", length = 20, unique = true)
    private String ccn; // CMS Certification Number

    @Enumerated(EnumType.STRING)
    @Column(name = "agency_type", nullable = false, length = 50)
    private AgencyType agencyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan", length = 50)
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.BASIC;

    // Contact information
    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(name = "address", length = 500)
    private String address;

    // Bidirectional relationship with UserAssignment
    @OneToMany(mappedBy = "agency", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserAssignment> userAssignments = new ArrayList<>();

    // Constructors
    public Agency() {}

    public Agency(String name, String ccn, AgencyType agencyType) {
        this.name = name;
        this.ccn = ccn;
        this.agencyType = agencyType;
    }

    // Helper methods
    public void addUserAssignment(UserAssignment assignment) {
        userAssignments.add(assignment);
        assignment.setAgency(this);
    }

    public void removeUserAssignment(UserAssignment assignment) {
        userAssignments.remove(assignment);
        assignment.setAgency(null);
    }

    @Override
    public String toString() {
        return "Agency{" +
                "agencyId=" + agencyId +
                ", name='" + name + '\'' +
                ", ccn='" + ccn + '\'' +
                ", agencyType=" + agencyType +
                '}';
    }
}