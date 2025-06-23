package com.hqc.hophuddles.enums;

public enum SubscriptionPlan {
    BASIC("Basic Plan"),
    PREMIUM("Premium Plan"),
    ENTERPRISE("Enterprise Plan"),
    TRIAL("Trial");

    private final String displayName;

    SubscriptionPlan(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}