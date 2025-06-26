package com.hqc.hophuddles.enums;

import java.util.Set;
import java.util.EnumSet;

public enum UserRole {
    ADMIN("Administrator", Permission.getAgencyAdminPermissions()),
    BRANCH_MANAGER("Branch Manager", Permission.getBranchManagerPermissions()),
    EDUCATOR("Educator", Permission.getEducatorPermissions()),
    MANAGER("Manager", Permission.getBranchManagerPermissions()),
    FIELD_CLINICIAN("Field Clinician", Permission.getFieldClinicianPermissions()),
    PRECEPTOR("Preceptor", Permission.getPreceptorPermissions()),
    LEARNER("Learner", Permission.getFieldClinicianPermissions()),
    SCHEDULER("Scheduler", EnumSet.of(
            Permission.SCHEDULE_HUDDLES, Permission.VIEW_USER_PROGRESS,
            Permission.CONFIGURE_NOTIFICATIONS, Permission.ACCESS_HUDDLES
    )),
    INTAKE_COORDINATOR("Intake Coordinator", EnumSet.of(
            Permission.ACCESS_HUDDLES, Permission.COMPLETE_ASSESSMENTS,
            Permission.VIEW_OWN_PROGRESS, Permission.DOWNLOAD_CERTIFICATES
    ));

    private final String displayName;
    private final Set<Permission> permissions;

    UserRole(String displayName, Set<Permission> permissions) {
        this.displayName = displayName;
        this.permissions = permissions;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    // Legacy methods for backward compatibility
    public boolean canCreateContent() {
        return permissions.contains(Permission.CREATE_HUDDLES);
    }

    public boolean canManageUsers() {
        return permissions.contains(Permission.MANAGE_AGENCY_USERS) ||
                permissions.contains(Permission.MANAGE_BRANCH_USERS);
    }

    public boolean canViewAnalytics() {
        return permissions.contains(Permission.VIEW_DETAILED_ANALYTICS) ||
                permissions.contains(Permission.VIEW_AGENCY_ANALYTICS);
    }
}