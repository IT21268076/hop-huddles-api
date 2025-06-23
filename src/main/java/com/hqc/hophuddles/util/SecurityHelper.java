// SecurityHelper.java
package com.hqc.hophuddles.util;

import com.hqc.hophuddles.enums.UserRole;
import com.hqc.hophuddles.exception.UnauthorizedException;

public class SecurityHelper {

    public static void requireAgencyAccess(Long agencyId) {
        if (!TenantContext.hasAccessToAgency(agencyId)) {
            throw new UnauthorizedException("Access denied to agency: " + agencyId);
        }
    }

    public static void requireRole(UserRole... requiredRoles) {
        // This will be enhanced when we add role context
        // For now, we'll implement this when we add JWT handling
    }

    public static void requireContentCreationPermission() {
        // Check if current user can create content (ADMIN or EDUCATOR)
        // Implementation will come with JWT integration
    }

    public static void requireUserManagementPermission() {
        // Check if current user can manage users (ADMIN or MANAGER)
        // Implementation will come with JWT integration
    }

    public static void requireAnalyticsPermission() {
        // Check if current user can view analytics
        // Implementation will come with JWT integration
    }
}