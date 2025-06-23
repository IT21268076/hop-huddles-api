package com.hqc.hophuddles.repository;

import com.hqc.hophuddles.entity.HuddleSequence;
import com.hqc.hophuddles.enums.SequenceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HuddleSequenceRepository extends JpaRepository<HuddleSequence, Long> {

    // Multi-tenant queries
    List<HuddleSequence> findByAgencyAgencyIdAndIsActiveTrueOrderByCreatedAtDesc(Long agencyId);

    List<HuddleSequence> findByAgencyAgencyIdAndSequenceStatusAndIsActiveTrueOrderByCreatedAtDesc(
            Long agencyId, SequenceStatus status
    );

    // Search with filters
    @Query("SELECT hs FROM HuddleSequence hs WHERE hs.agency.agencyId = :agencyId " +
            "AND hs.isActive = true " +
            "AND (:title IS NULL OR LOWER(hs.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:status IS NULL OR hs.sequenceStatus = :status) " +
            "ORDER BY hs.createdAt DESC")
    Page<HuddleSequence> findSequencesWithFilters(
            @Param("agencyId") Long agencyId,
            @Param("title") String title,
            @Param("status") SequenceStatus status,
            Pageable pageable
    );

    // Created by user
    List<HuddleSequence> findByCreatedByUserUserIdAndIsActiveTrueOrderByCreatedAtDesc(Long userId);

    // Published sequences
    List<HuddleSequence> findByAgencyAgencyIdAndSequenceStatusAndIsActiveTrueOrderByPublishedAtDesc(
            Long agencyId, SequenceStatus status
    );

    // Analytics queries
    @Query("SELECT hs.sequenceStatus, COUNT(hs) FROM HuddleSequence hs " +
            "WHERE hs.agency.agencyId = :agencyId AND hs.isActive = true " +
            "GROUP BY hs.sequenceStatus")
    List<Object[]> countByStatusInAgency(@Param("agencyId") Long agencyId);

    @Query("SELECT COUNT(hs) FROM HuddleSequence hs " +
            "WHERE hs.agency.agencyId = :agencyId " +
            "AND hs.sequenceStatus = 'PUBLISHED' " +
            "AND hs.isActive = true " +
            "AND hs.publishedAt >= :since")
    long countPublishedSince(@Param("agencyId") Long agencyId, @Param("since") LocalDateTime since);

    // Security check
    @Query("SELECT COUNT(hs) > 0 FROM HuddleSequence hs " +
            "WHERE hs.sequenceId = :sequenceId " +
            "AND hs.agency.agencyId = :agencyId " +
            "AND hs.isActive = true")
    boolean existsByIdAndAgency(@Param("sequenceId") Long sequenceId, @Param("agencyId") Long agencyId);
}