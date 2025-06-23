package com.hqc.hophuddles.repository;

import com.hqc.hophuddles.entity.UserAssignment;
import com.hqc.hophuddles.enums.UserRole;
import com.hqc.hophuddles.enums.Discipline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAssignmentRepository extends JpaRepository<UserAssignment, Long> {

    // User-centric queries
    Optional<UserAssignment> findByUserUserIdAndIsPrimaryTrueAndIsActiveTrue(Long userId);

    List<UserAssignment> findByUserUserIdAndIsActiveTrueOrderByIsPrimaryDescAssignedAtDesc(Long userId);

    // Agency-centric queries
    List<UserAssignment> findByAgencyAgencyIdAndIsActiveTrueOrderByUserNameAsc(Long agencyId);

    List<UserAssignment> findByAgencyAgencyIdAndRoleAndIsActiveTrueOrderByUserNameAsc(
            Long agencyId, UserRole role
    );

    List<UserAssignment> findByAgencyAgencyIdAndDisciplineAndIsActiveTrueOrderByUserNameAsc(
            Long agencyId, Discipline discipline
    );

    // Multi-tenant security queries
    @Query("SELECT ua.agency.agencyId FROM UserAssignment ua " +
            "WHERE ua.user.userId = :userId AND ua.isActive = true")
    List<Long> findAgencyIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(ua) > 0 FROM UserAssignment ua " +
            "WHERE ua.user.userId = :userId " +
            "AND ua.agency.agencyId = :agencyId " +
            "AND ua.isActive = true")
    boolean hasUserAccessToAgency(@Param("userId") Long userId, @Param("agencyId") Long agencyId);

    @Query("SELECT COUNT(ua) > 0 FROM UserAssignment ua " +
            "WHERE ua.user.userId = :userId " +
            "AND ua.agency.agencyId = :agencyId " +
            "AND ua.role = :role " +
            "AND ua.isActive = true")
    boolean hasUserRoleInAgency(
            @Param("userId") Long userId,
            @Param("agencyId") Long agencyId,
            @Param("role") UserRole role
    );

    // Analytics queries
    @Query("SELECT ua.role, COUNT(ua) FROM UserAssignment ua " +
            "WHERE ua.agency.agencyId = :agencyId AND ua.isActive = true " +
            "GROUP BY ua.role")
    List<Object[]> countUsersByRoleInAgency(@Param("agencyId") Long agencyId);

    @Query("SELECT ua.discipline, COUNT(ua) FROM UserAssignment ua " +
            "WHERE ua.agency.agencyId = :agencyId AND ua.isActive = true " +
            "AND ua.discipline IS NOT NULL " +
            "GROUP BY ua.discipline")
    List<Object[]> countUsersByDisciplineInAgency(@Param("agencyId") Long agencyId);
}