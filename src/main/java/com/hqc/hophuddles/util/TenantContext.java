// TenantContext.java
package com.hqc.hophuddles.util;

import java.util.List;

public class TenantContext {

    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();
    private static final ThreadLocal<Long> currentAgencyId = new ThreadLocal<>();
    private static final ThreadLocal<List<Long>> accessibleAgencyIds = new ThreadLocal<>();

    // User context
    public static void setCurrentUserId(Long userId) {
        currentUserId.set(userId);
    }

    public static Long getCurrentUserId() {
        return currentUserId.get();
    }

    // Agency context
    public static void setCurrentAgencyId(Long agencyId) {
        currentAgencyId.set(agencyId);
    }

    public static Long getCurrentAgencyId() {
        return currentAgencyId.get();
    }

    // Accessible agencies
    public static void setAccessibleAgencyIds(List<Long> agencyIds) {
        accessibleAgencyIds.set(agencyIds);
    }

    public static List<Long> getAccessibleAgencyIds() {
        return accessibleAgencyIds.get();
    }

    // Security checks
    public static boolean hasAccessToAgency(Long agencyId) {
        List<Long> accessible = getAccessibleAgencyIds();
        return accessible != null && accessible.contains(agencyId);
    }

    public static boolean isCurrentAgencyAccessible() {
        Long currentAgency = getCurrentAgencyId();
        return currentAgency != null && hasAccessToAgency(currentAgency);
    }

    // Cleanup
    public static void clear() {
        currentUserId.remove();
        currentAgencyId.remove();
        accessibleAgencyIds.remove();
    }
}