package com.hqc.hophuddles.enums;

public enum TargetType {
    AGENCY("Agency"),
    BRANCH("Branch"),
    TEAM("Team"),
    DISCIPLINE("Discipline"),
    ROLE("Role"),
    USER("User");

    private final String displayName;

    TargetType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}