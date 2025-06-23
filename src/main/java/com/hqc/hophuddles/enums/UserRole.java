package com.hqc.hophuddles.enums;

public enum UserRole {
    ADMIN("Administrator"),
    EDUCATOR("Educator"),
    MANAGER("Manager"),
    FIELD_CLINICIAN("Field Clinician"),
    PRECEPTOR("Preceptor"),
    LEARNER("Learner");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Role-based permissions (matches your UI requirements)
    public boolean canCreateContent() {
        return this == ADMIN || this == EDUCATOR;
    }

    public boolean canManageUsers() {
        return this == ADMIN || this == MANAGER;
    }

    public boolean canViewAnalytics() {
        return this == ADMIN || this == EDUCATOR || this == MANAGER;
    }
}