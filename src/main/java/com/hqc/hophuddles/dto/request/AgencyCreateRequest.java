// AgencyCreateRequest.java
package com.hqc.hophuddles.dto.request;

import com.hqc.hophuddles.enums.AgencyType;
import com.hqc.hophuddles.enums.SubscriptionPlan;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AgencyCreateRequest {

    @NotBlank(message = "Agency name is required")
    @Size(max = 255, message = "Agency name must not exceed 255 characters")
    private String name;

    @Pattern(regexp = "^[0-9]{6}$", message = "CCN must be 6 digits")
    private String ccn;

    private AgencyType agencyType;

    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.BASIC;

    @Email(message = "Contact email should be valid")
    private String contactEmail;

    @Size(max = 50, message = "Contact phone must not exceed 50 characters")
    private String contactPhone;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    // Constructors
    public AgencyCreateRequest() {}

    // Getters and Setters
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
}