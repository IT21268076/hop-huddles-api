package com.hqc.hophuddles.repository;

import com.hqc.hophuddles.entity.SequenceProgress;
import com.hqc.hophuddles.enums.ProgressStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SequenceProgressRepository extends JpaRepository<SequenceProgress, Long> {

    // Find progress by user and sequence
    Optional<SequenceProgress> findByUserUserIdAndSequenceSequenceIdAndIsActiveTrue(Long userId, Long sequenceId);

    // Find progress by user
    List<SequenceProgress> findByUserUserIdAndIsActiveTrueOrderByLastAccessedDesc(Long userId);

    // Find progress by sequence
    List<SequenceProgress> findBySequenceSequenceIdAndIsActiveTrueOrderByCompletionPercentageDesc(Long sequenceId);

    // Find progress by agency
    List<SequenceProgress> findByAgencyAgencyIdAndIsActiveTrueOrderByLastAccessedDesc(Long agencyId);

    // Analytics queries
    @Query("SELECT sp.sequenceStatus, COUNT(sp) FROM SequenceProgress sp " +
            "WHERE sp.sequence.sequenceId = :sequenceId " +
            "AND sp.isActive = true " +
            "GROUP BY sp.sequenceStatus")
    List<Object[]> countByStatusInSequence(@Param("sequenceId") Long sequenceId);

    @Query("SELECT AVG(sp.completionPercentage) FROM SequenceProgress sp " +
            "WHERE sp.sequence.sequenceId = :sequenceId " +
            "AND sp.isActive = true")
    BigDecimal getAverageCompletionBySequence(@Param("sequenceId") Long sequenceId);

    @Query("SELECT COUNT(sp) FROM SequenceProgress sp " +
            "WHERE sp.agency.agencyId = :agencyId " +
            "AND sp.sequenceStatus = :status " +
            "AND sp.isActive = true")
    long countByAgencyAndStatus(@Param("agencyId") Long agencyId, @Param("status") ProgressStatus status);

    @Query("SELECT COUNT(sp) FROM SequenceProgress sp " +
            "WHERE sp.agency.agencyId = :agencyId " +
            "AND sp.sequenceStatus = 'COMPLETED' " +
            "AND sp.completedAt >= :since " +
            "AND sp.isActive = true")
    long countCompletedByAgencySince(@Param("agencyId") Long agencyId, @Param("since") LocalDateTime since);

    // Leaderboard queries
    @Query("SELECT sp FROM SequenceProgress sp " +
            "WHERE sp.agency.agencyId = :agencyId " +
            "AND sp.sequenceStatus = 'COMPLETED' " +
            "AND sp.isActive = true " +
            "ORDER BY sp.totalTimeSpentMinutes ASC")
    List<SequenceProgress> findFastestCompletionsByAgency(@Param("agencyId") Long agencyId);

    @Query("SELECT sp FROM SequenceProgress sp " +
            "WHERE sp.agency.agencyId = :agencyId " +
            "AND sp.averageScore IS NOT NULL " +
            "AND sp.isActive = true " +
            "ORDER BY sp.averageScore DESC")
    List<SequenceProgress> findTopScoresByAgency(@Param("agencyId") Long agencyId);
}