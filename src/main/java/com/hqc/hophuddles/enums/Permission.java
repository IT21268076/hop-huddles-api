package com.hqc.hophuddles.enums;

import java.util.Set;
import java.util.EnumSet;

public enum Permission {
    // Agency Management
    MANAGE_AGENCY_SETTINGS("Manage Agency Settings"),
    VIEW_AGENCY_ANALYTICS("View Agency Analytics"),
    MANAGE_AGENCY_USERS("Manage Agency Users"),
    CONFIGURE_AGENCY_BILLING("Configure Agency Billing"),

    // Branch Management
    MANAGE_BRANCH_SETTINGS("Manage Branch Settings"),
    VIEW_BRANCH_ANALYTICS("View Branch Analytics"),
    MANAGE_BRANCH_USERS("Manage Branch Users"),
    CREATE_BRANCHES("Create Branches"),

    // Team Management
    MANAGE_TEAM_SETTINGS("Manage Team Settings"),
    VIEW_TEAM_ANALYTICS("View Team Analytics"),
    MANAGE_TEAM_USERS("Manage Team Users"),
    CREATE_TEAMS("Create Teams"),

    // Content Management
    CREATE_HUDDLES("Create Huddles"),
    EDIT_HUDDLES("Edit Huddles"),
    PUBLISH_HUDDLES("Publish Huddles"),
    DELETE_HUDDLES("Delete Huddles"),
    MANAGE_CONTENT_LIBRARY("Manage Content Library"),
    SCHEDULE_HUDDLES("Schedule Huddles"),
    APPROVE_CONTENT("Approve Content"),

    // User Management
    CREATE_USERS("Create Users"),
    EDIT_USERS("Edit Users"),
    DELETE_USERS("Delete Users"),
    ASSIGN_ROLES("Assign Roles"),
    VIEW_USER_PROGRESS("View User Progress"),
    MANAGE_USER_ASSIGNMENTS("Manage User Assignments"),

    // Learning & Progress
    ACCESS_HUDDLES("Access Huddles"),
    COMPLETE_ASSESSMENTS("Complete Assessments"),
    VIEW_OWN_PROGRESS("View Own Progress"),
    DOWNLOAD_CERTIFICATES("Download Certificates"),
    ACCESS_OFFLINE_CONTENT("Access Offline Content"),

    // Analytics & Reporting
    VIEW_DETAILED_ANALYTICS("View Detailed Analytics"),
    EXPORT_REPORTS("Export Reports"),
    VIEW_COMPLIANCE_REPORTS("View Compliance Reports"),
    VIEW_FINANCIAL_REPORTS("View Financial Reports"),
    GENERATE_CUSTOM_REPORTS("Generate Custom Reports"),

    // System Administration
    MANAGE_INTEGRATIONS("Manage Integrations"),
    VIEW_SYSTEM_LOGS("View System Logs"),
    MANAGE_API_ACCESS("Manage API Access"),
    CONFIGURE_NOTIFICATIONS("Configure Notifications");

    private final String displayName;

    Permission(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Permission groups for easier management
    public static Set<Permission> getAgencyAdminPermissions() {
        return EnumSet.of(
                MANAGE_AGENCY_SETTINGS, VIEW_AGENCY_ANALYTICS, MANAGE_AGENCY_USERS,
                CONFIGURE_AGENCY_BILLING, CREATE_BRANCHES, MANAGE_INTEGRATIONS,
                VIEW_FINANCIAL_REPORTS, GENERATE_CUSTOM_REPORTS
        );
    }

    public static Set<Permission> getBranchManagerPermissions() {
        return EnumSet.of(
                MANAGE_BRANCH_SETTINGS, VIEW_BRANCH_ANALYTICS, MANAGE_BRANCH_USERS,
                CREATE_TEAMS, CREATE_HUDDLES, EDIT_HUDDLES, PUBLISH_HUDDLES,
                VIEW_USER_PROGRESS, EXPORT_REPORTS, VIEW_COMPLIANCE_REPORTS
        );
    }

    public static Set<Permission> getEducatorPermissions() {
        return EnumSet.of(
                CREATE_HUDDLES, EDIT_HUDDLES, SCHEDULE_HUDDLES, MANAGE_CONTENT_LIBRARY,
                VIEW_USER_PROGRESS, VIEW_DETAILED_ANALYTICS, EXPORT_REPORTS,
                CONFIGURE_NOTIFICATIONS
        );
    }

    public static Set<Permission> getFieldClinicianPermissions() {
        return EnumSet.of(
                ACCESS_HUDDLES, COMPLETE_ASSESSMENTS, VIEW_OWN_PROGRESS,
                DOWNLOAD_CERTIFICATES, ACCESS_OFFLINE_CONTENT
        );
    }

    public static Set<Permission> getPreceptorPermissions() {
        return EnumSet.of(
                ACCESS_HUDDLES, COMPLETE_ASSESSMENTS, VIEW_OWN_PROGRESS,
                VIEW_USER_PROGRESS, DOWNLOAD_CERTIFICATES, ACCESS_OFFLINE_CONTENT
        );
    }
}