package com.hqc.hophuddles.repository;

import com.hqc.hophuddles.entity.SequenceTarget;
import com.hqc.hophuddles.enums.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SequenceTargetRepository extends JpaRepository<SequenceTarget, Long> {

    // Targets by sequence
    List<SequenceTarget> findBySequenceSequenceIdAndIsActiveTrueOrderByTargetTypeAsc(Long sequenceId);

    List<SequenceTarget> findBySequenceSequenceIdAndTargetTypeAndIsActiveTrueOrderByTargetValueAsc(
            Long sequenceId, TargetType targetType
    );

    // Find sequences by target
    @Query("SELECT st.sequence.sequenceId FROM SequenceTarget st " +
            "WHERE st.targetType = :targetType " +
            "AND st.targetValue = :targetValue " +
            "AND st.isActive = true " +
            "AND st.sequence.isActive = true " +
            "AND st.sequence.sequenceStatus = 'PUBLISHED'")
    List<Long> findPublishedSequenceIdsByTarget(
            @Param("targetType") TargetType targetType,
            @Param("targetValue") String targetValue
    );

    // Check if target exists
    boolean existsBySequenceSequenceIdAndTargetTypeAndTargetValueAndIsActiveTrue(
            Long sequenceId, TargetType targetType, String targetValue
    );

    // Analytics
    @Query("SELECT st.targetType, COUNT(st) FROM SequenceTarget st " +
            "WHERE st.sequence.sequenceId = :sequenceId AND st.isActive = true " +
            "GROUP BY st.targetType")
    List<Object[]> countByTypeInSequence(@Param("sequenceId") Long sequenceId);
}