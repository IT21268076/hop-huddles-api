package com.hqc.hophuddles.repository;

import com.hqc.hophuddles.entity.User;
import com.hqc.hophuddles.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Auth0 integration
    Optional<User> findByAuth0IdAndIsActiveTrue(String auth0Id);

    Optional<User> findByEmailAndIsActiveTrue(String email);

    // Basic finders
    List<User> findByIsActiveTrueOrderByNameAsc();

    // Multi-tenant queries
    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN u.assignments ua " +
            "WHERE u.isActive = true " +
            "AND ua.isActive = true " +
            "AND ua.agency.agencyId = :agencyId " +
            "ORDER BY u.name ASC")
    List<User> findActiveUsersByAgency(@Param("agencyId") Long agencyId);

    // Search with pagination and agency filter
    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN u.assignments ua " +
            "WHERE u.isActive = true " +
            "AND ua.isActive = true " +
            "AND ua.agency.agencyId = :agencyId " +
            "AND (:searchTerm IS NULL OR " +
            "     LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "     LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY u.name ASC")
    Page<User> findUsersByAgencyWithSearch(
            @Param("agencyId") Long agencyId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

    // Performance optimization
    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :loginTime WHERE u.userId = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);

    // Existence checks
    boolean existsByEmailAndIsActiveTrue(String email);

    boolean existsByAuth0IdAndIsActiveTrue(String auth0Id);

    // User activity analytics
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true AND u.lastLogin >= :since")
    long countActiveUsersSince(@Param("since") LocalDateTime since);

    // Count active users by agency
    @Query("SELECT COUNT(DISTINCT u) FROM User u " +
            "JOIN u.assignments ua " +
            "WHERE u.isActive = true " +
            "AND ua.isActive = true " +
            "AND ua.agency.agencyId = :agencyId")
    long countActiveUsersByAgency(@Param("agencyId") Long agencyId);

    // Count users by agency and role
    @Query("SELECT COUNT(DISTINCT u) FROM User u " +
            "JOIN u.assignments ua " +
            "WHERE u.isActive = true " +
            "AND ua.isActive = true " +
            "AND ua.agency.agencyId = :agencyId " +
            "AND ua.role = :role")
    long countUsersByAgencyAndRole(@Param("agencyId") Long agencyId, @Param("role") UserRole role);
}