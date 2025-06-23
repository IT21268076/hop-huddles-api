package com.hqc.hophuddles.repository;

import com.hqc.hophuddles.entity.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    // Find assessment by huddle
    Optional<Assessment> findByHuddleHuddleIdAndIsActiveTrue(Long huddleId);

    // Find assessments by sequence
    @Query("SELECT a FROM Assessment a " +
            "WHERE a.huddle.sequence.sequenceId = :sequenceId " +
            "AND a.isActive = true " +
            "ORDER BY a.huddle.orderIndex")
    List<Assessment> findBySequenceIdAndIsActiveTrue(@Param("sequenceId") Long sequenceId);

    // Find assessments by agency
    @Query("SELECT a FROM Assessment a " +
            "WHERE a.huddle.sequence.agency.agencyId = :agencyId " +
            "AND a.isActive = true " +
            "ORDER BY a.huddle.sequence.title, a.huddle.orderIndex")
    List<Assessment> findByAgencyIdAndIsActiveTrue(@Param("agencyId") Long agencyId);

    // Multi-tenant security
    @Query("SELECT COUNT(a) > 0 FROM Assessment a " +
            "WHERE a.assessmentId = :assessmentId " +
            "AND a.huddle.sequence.agency.agencyId = :agencyId " +
            "AND a.isActive = true")
    boolean existsByIdAndAgency(@Param("assessmentId") Long assessmentId, @Param("agencyId") Long agencyId);
}