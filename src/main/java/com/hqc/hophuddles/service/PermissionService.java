package com.hqc.hophuddles.service;

import com.hqc.hophuddles.entity.User;
import com.hqc.hophuddles.entity.UserAssignment;
import com.hqc.hophuddles.enums.Permission;
import com.hqc.hophuddles.enums.UserRole;
import com.hqc.hophuddles.repository.UserAssignmentRepository;
import com.hqc.hophuddles.repository.UserRepository;
import com.hqc.hophuddles.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final UserRepository userRepository;
    private final UserAssignmentRepository userAssignmentRepository;

    /**
     * Check if current authenticated user has specific permission for a resource
     */
    public boolean hasPermission(Permission permission, Long resourceId, String resourceType) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return false;
        }

        Long userId = getCurrentUserId();
        if (userId == null) {
            return false;
        }

        return hasPermission(userId, permission, resourceId, resourceType);
    }

    /**
     * Check if user has specific permission for a resource
     */
    @Cacheable(value = "userPermissions", key = "#userId + '_' + #permission + '_' + #resourceId + '_' + #resourceType")
    public boolean hasPermission(Long userId, Permission permission, Long resourceId, String resourceType) {
        try {
            Set<Permission> userPermissions = getUserPermissions(userId);

            if (!userPermissions.contains(permission)) {
                return false;
            }

            // Check resource-specific access based on user's assignments
            return hasResourceAccess(userId, resourceId, resourceType);

        } catch (Exception e) {
            log.error("Error checking permission for user {} on resource {}", userId, resourceId, e);
            return false;
        }
    }

    /**
     * Check if user has permission at agency level
     */
    public boolean hasAgencyPermission(Long userId, Permission permission, Long agencyId) {
        return hasPermission(userId, permission, agencyId, "AGENCY");
    }

    /**
     * Check if user has permission at branch level
     */
    public boolean hasBranchPermission(Long userId, Permission permission, Long branchId) {
        return hasPermission(userId, permission, branchId, "BRANCH");
    }

    /**
     * Check if user has permission at team level
     */
    public boolean hasTeamPermission(Long userId, Permission permission, Long teamId) {
        return hasPermission(userId, permission, teamId, "TEAM");
    }

    /**
     * Get all permissions for a user across all their assignments
     */
    @Cacheable(value = "userPermissionSet", key = "#userId")
    public Set<Permission> getUserPermissions(Long userId) {
        List<UserAssignment> assignments = userAssignmentRepository
                .findByUserUserIdAndIsActiveTrueOrderByIsPrimaryDescAssignedAtDesc(userId);

        return assignments.stream()
                .map(UserAssignment::getRole)
                .flatMap(role -> role.getPermissions().stream())
                .collect(Collectors.toSet());
    }

    /**
     * Check if user has access to specific resource based on their assignments
     */
    private boolean hasResourceAccess(Long userId, Long resourceId, String resourceType) {
        List<UserAssignment> assignments = userAssignmentRepository
                .findByUserUserIdAndIsActiveTrueOrderByIsPrimaryDescAssignedAtDesc(userId);

        switch (resourceType.toUpperCase()) {
            case "AGENCY":
                return assignments.stream()
                        .anyMatch(assignment -> assignment.getAgency().getAgencyId().equals(resourceId));

            case "BRANCH":
                return assignments.stream()
                        .anyMatch(assignment ->
                                assignment.getBranch() != null &&
                                        assignment.getBranch().getBranchId().equals(resourceId) ||
                                        // Agency admins have access to all branches
                                        (assignment.getBranch() == null && assignment.getRole() == UserRole.ADMIN)
                        );

            case "TEAM":
                return assignments.stream()
                        .anyMatch(assignment ->
                                assignment.getTeam() != null &&
                                        assignment.getTeam().getTeamId().equals(resourceId) ||
                                        // Branch managers have access to all teams in their branch
                                        (assignment.getTeam() == null &&
                                                assignment.getRole() == UserRole.BRANCH_MANAGER)
                        );

            case "HUDDLE":
            case "SEQUENCE":
                // For huddles/sequences, check if user has access to the agency
                // This would need to be enhanced to check the actual huddle's agency
                return assignments.stream()
                        .anyMatch(assignment -> true); // Simplified for now

            default:
                return true; // Default allow for unknown resource types
        }
    }

    /**
     * Get current user ID from security context
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
            return ((UserPrincipal) auth.getPrincipal()).getId();
        }
        return null;
    }

    /**
     * Check if current user can access specific agency
     */
    public boolean canAccessAgency(Long agencyId) {
        Long userId = getCurrentUserId();
        return userId != null && userAssignmentRepository.hasUserAccessToAgency(userId, agencyId);
    }

    /**
     * Get all agencies current user can access
     */
    public List<Long> getAccessibleAgencyIds() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return List.of();
        }
        return userAssignmentRepository.findAgencyIdsByUserId(userId);
    }

    /**
     * Check if user is admin for specific agency
     */
    public boolean isAgencyAdmin(Long userId, Long agencyId) {
        return userAssignmentRepository.hasUserRoleInAgency(userId, agencyId, UserRole.ADMIN);
    }

    /**
     * Check if user can manage other users
     */
    public boolean canManageUsers(Long targetUserId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null || currentUserId.equals(targetUserId)) {
            return false;
        }

        // Get current user's permissions
        Set<Permission> permissions = getUserPermissions(currentUserId);

        return permissions.contains(Permission.MANAGE_AGENCY_USERS) ||
                permissions.contains(Permission.MANAGE_BRANCH_USERS) ||
                permissions.contains(Permission.MANAGE_TEAM_USERS);
    }
}