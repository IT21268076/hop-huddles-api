package com.hqc.hophuddles.repository;

import com.hqc.hophuddles.entity.Huddle;
import com.hqc.hophuddles.enums.HuddleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HuddleRepository extends JpaRepository<Huddle, Long> {

    // Huddles by sequence
    List<Huddle> findBySequenceSequenceIdAndIsActiveTrueOrderByOrderIndexAsc(Long sequenceId);

    List<Huddle> findBySequenceSequenceIdAndHuddleTypeAndIsActiveTrueOrderByOrderIndexAsc(
            Long sequenceId, HuddleType huddleType
    );

    // Find by order index
    Optional<Huddle> findBySequenceSequenceIdAndOrderIndexAndIsActiveTrue(Long sequenceId, Integer orderIndex);

    // Get next order index
    @Query("SELECT COALESCE(MAX(h.orderIndex), 0) + 1 FROM Huddle h " +
            "WHERE h.sequence.sequenceId = :sequenceId AND h.isActive = true")
    Integer getNextOrderIndex(@Param("sequenceId") Long sequenceId);

    // Analytics
    @Query("SELECT h.huddleType, COUNT(h) FROM Huddle h " +
            "WHERE h.sequence.sequenceId = :sequenceId AND h.isActive = true " +
            "GROUP BY h.huddleType")
    List<Object[]> countByTypeInSequence(@Param("sequenceId") Long sequenceId);

    @Query("SELECT COUNT(h) FROM Huddle h " +
            "WHERE h.sequence.sequenceId = :sequenceId " +
            "AND h.isActive = true " +
            "AND h.contentJson IS NOT NULL " +
            "AND h.voiceScript IS NOT NULL")
    long countCompleteHuddles(@Param("sequenceId") Long sequenceId);

    // Security check
    @Query("SELECT COUNT(h) > 0 FROM Huddle h " +
            "WHERE h.huddleId = :huddleId " +
            "AND h.sequence.agency.agencyId = :agencyId " +
            "AND h.isActive = true")
    boolean existsByIdAndAgency(@Param("huddleId") Long huddleId, @Param("agencyId") Long agencyId);

}