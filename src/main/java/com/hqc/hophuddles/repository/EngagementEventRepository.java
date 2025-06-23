package com.hqc.hophuddles.repository;

import com.hqc.hophuddles.entity.EngagementEvent;
import com.hqc.hophuddles.enums.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EngagementEventRepository extends JpaRepository<EngagementEvent, Long> {

    // Find events by user
    List<EngagementEvent> findByUserUserIdAndIsActiveTrueOrderByCreatedAtDesc(Long userId);

    // Find events by huddle
    List<EngagementEvent> findByHuddleHuddleIdAndIsActiveTrueOrderByCreatedAtDesc(Long huddleId);

    // Find events by sequence
    List<EngagementEvent> findBySequenceSequenceIdAndIsActiveTrueOrderByCreatedAtDesc(Long sequenceId);

    // Find events by session
    List<EngagementEvent> findBySessionIdAndIsActiveTrueOrderByCreatedAtAsc(String sessionId);

    // Analytics queries
    @Query("SELECT ee.eventType, COUNT(ee) FROM EngagementEvent ee " +
            "WHERE ee.huddle.huddleId = :huddleId " +
            "AND ee.isActive = true " +
            "GROUP BY ee.eventType")
    List<Object[]> countEventsByTypeForHuddle(@Param("huddleId") Long huddleId);

    @Query("SELECT ee.eventType, COUNT(ee) FROM EngagementEvent ee " +
            "WHERE ee.sequence.sequenceId = :sequenceId " +
            "AND ee.isActive = true " +
            "GROUP BY ee.eventType")
    List<Object[]> countEventsByTypeForSequence(@Param("sequenceId") Long sequenceId);

    @Query("SELECT COUNT(ee) FROM EngagementEvent ee " +
            "WHERE ee.agency.agencyId = :agencyId " +
            "AND ee.eventType = :eventType " +
            "AND ee.createdAt >= :since " +
            "AND ee.isActive = true")
    long countEventsByAgencyAndTypeSince(
            @Param("agencyId") Long agencyId,
            @Param("eventType") EventType eventType,
            @Param("since") LocalDateTime since
    );

    // Engagement patterns
    @Query("SELECT DATE(ee.createdAt), COUNT(ee) FROM EngagementEvent ee " +
            "WHERE ee.agency.agencyId = :agencyId " +
            "AND ee.createdAt >= :since " +
            "AND ee.isActive = true " +
            "GROUP BY DATE(ee.createdAt) " +
            "ORDER BY DATE(ee.createdAt)")
    List<Object[]> getDailyEngagementByAgency(@Param("agencyId") Long agencyId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(DISTINCT ee.user.userId) FROM EngagementEvent ee " +
            "WHERE ee.agency.agencyId = :agencyId " +
            "AND ee.createdAt >= :since " +
            "AND ee.isActive = true")
    long countActiveUsersByAgencySince(@Param("agencyId") Long agencyId, @Param("since") LocalDateTime since);
}