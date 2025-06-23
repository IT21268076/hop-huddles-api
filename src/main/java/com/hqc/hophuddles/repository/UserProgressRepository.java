package com.hqc.hophuddles.repository;

import com.hqc.hophuddles.entity.UserProgress;
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
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {

    // Find progress by user and huddle
    Optional<UserProgress> findByUserUserIdAndHuddleHuddleIdAndIsActiveTrue(Long userId, Long huddleId);

    // Find all progress for a user in a sequence
    List<UserProgress> findByUserUserIdAndSequenceSequenceIdAndIsActiveTrueOrderByHuddleOrderIndexAsc(
            Long userId, Long sequenceId
    );

    // Find progress by user
    List<UserProgress> findByUserUserIdAndIsActiveTrueOrderByLastAccessedDesc(Long userId);

    // Find progress by huddle
    List<UserProgress> findByHuddleHuddleIdAndIsActiveTrueOrderByCompletedAtDesc(Long huddleId);

    // Analytics queries
    @Query("SELECT COUNT(up) FROM UserProgress up " +
            "WHERE up.huddle.sequence.sequenceId = :sequenceId " +
            "AND up.progressStatus = :status " +
            "AND up.isActive = true")
    long countBySequenceAndStatus(@Param("sequenceId") Long sequenceId, @Param("status") ProgressStatus status);

    @Query("SELECT AVG(up.completionPercentage) FROM UserProgress up " +
            "WHERE up.huddle.sequence.sequenceId = :sequenceId " +
            "AND up.isActive = true")
    BigDecimal getAverageCompletionBySequence(@Param("sequenceId") Long sequenceId);

    @Query("SELECT AVG(up.timeSpentMinutes) FROM UserProgress up " +
            "WHERE up.huddle.sequence.sequenceId = :sequenceId " +
            "AND up.progressStatus = 'COMPLETED' " +
            "AND up.isActive = true")
    BigDecimal getAverageTimeSpentBySequence(@Param("sequenceId") Long sequenceId);

    @Query("SELECT AVG(up.assessmentScore) FROM UserProgress up " +
            "WHERE up.huddle.sequence.sequenceId = :sequenceId " +
            "AND up.assessmentScore IS NOT NULL " +
            "AND up.isActive = true")
    BigDecimal getAverageAssessmentScoreBySequence(@Param("sequenceId") Long sequenceId);

    // User analytics
    @Query("SELECT COUNT(up) FROM UserProgress up " +
            "WHERE up.user.userId = :userId " +
            "AND up.progressStatus = 'COMPLETED' " +
            "AND up.isActive = true")
    long countCompletedByUser(@Param("userId") Long userId);

    @Query("SELECT SUM(up.timeSpentMinutes) FROM UserProgress up " +
            "WHERE up.user.userId = :userId " +
            "AND up.isActive = true")
    BigDecimal getTotalTimeSpentByUser(@Param("userId") Long userId);

    // Agency analytics
    @Query("SELECT COUNT(up) FROM UserProgress up " +
            "WHERE up.sequence.agency.agencyId = :agencyId " +
            "AND up.progressStatus = 'COMPLETED' " +
            "AND up.completedAt >= :since " +
            "AND up.isActive = true")
    long countCompletedByAgencySince(@Param("agencyId") Long agencyId, @Param("since") LocalDateTime since);

    // Multi-tenant security
    @Query("SELECT COUNT(up) > 0 FROM UserProgress up " +
            "WHERE up.progressId = :progressId " +
            "AND up.sequence.agency.agencyId = :agencyId " +
            "AND up.isActive = true")
    boolean existsByIdAndAgency(@Param("progressId") Long progressId, @Param("agencyId") Long agencyId);
}