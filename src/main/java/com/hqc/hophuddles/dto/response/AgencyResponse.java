// AgencyResponse.java
package com.hqc.hophuddles.dto.response;

import com.hqc.hophuddles.enums.AgencyType;
import com.hqc.hophuddles.enums.SubscriptionPlan;

import java.time.LocalDateTime;

public class AgencyResponse {
    private Long agencyId;
    private String name;
    private String ccn;
    private AgencyType agencyType;
    private SubscriptionPlan subscriptionPlan;
    private String contactEmail;
    private String contactPhone;
    private String address;
    private LocalDateTime createdAt;
    private int userCount;

    // Constructors
    public AgencyResponse() {}

    public AgencyResponse(Long agencyId, String name, String ccn, AgencyType agencyType) {
        this.agencyId = agencyId;
        this.name = name;
        this.ccn = ccn;
        this.agencyType = agencyType;
    }

    // Getters and Setters
    public Long getAgencyId() { return agencyId; }
    public void setAgencyId(Long agencyId) { this.agencyId = agencyId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCcn() { return ccn; }
    public void setCcn(String ccn) { this.ccn = ccn; }

    public AgencyType getAgencyType() { return agencyType; }
    public void setAgencyType(AgencyType agencyType) { this.agencyType = agencyType; }

    public SubscriptionPlan getSubscriptionPlan() { return subscriptionPlan; }
    public void setSubscriptionPlan(SubscriptionPlan subscriptionPlan) { this.subscriptionPlan = subscriptionPlan; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getUserCount() { return userCount; }
    public void setUserCount(int userCount) { this.userCount = userCount; }
}