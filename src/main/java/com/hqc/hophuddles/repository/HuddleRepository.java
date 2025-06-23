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


    /**
     * Find huddle by sequence and order index
     */
    @Query("SELECT h FROM Huddle h " +
            "WHERE h.sequence.sequenceId = :sequenceId " +
            "AND h.orderIndex = :orderIndex " +
            "AND h.isActive = true")
    Optional<Huddle> findBySequenceAndOrderIndex(@Param("sequenceId") Long sequenceId,
                                                 @Param("orderIndex") Integer orderIndex);

    /**
     * Get maximum order index for a sequence
     */
    @Query("SELECT MAX(h.orderIndex) FROM Huddle h " +
            "WHERE h.sequence.sequenceId = :sequenceId " +
            "AND h.isActive = true")
    Optional<Integer> findMaxOrderIndexBySequenceId(@Param("sequenceId") Long sequenceId);

    /**
     * Find huddles by content type
     */
    @Query("SELECT h FROM Huddle h " +
            "WHERE h.contentType = :contentType " +
            "AND h.isActive = true " +
            "ORDER BY h.createdAt DESC")
    List<Huddle> findByContentType(@Param("contentType") String contentType);

    /**
     * Find huddles by agency and content type
     */
    @Query("SELECT h FROM Huddle h " +
            "WHERE h.sequence.agency.agencyId = :agencyId " +
            "AND h.contentType = :contentType " +
            "AND h.isActive = true " +
            "ORDER BY h.createdAt DESC")
    List<Huddle> findByAgencyAndContentType(@Param("agencyId") Long agencyId,
                                            @Param("contentType") String contentType);

    /**
     * Count huddles by content type for agency
     */
    @Query("SELECT COUNT(h) FROM Huddle h " +
            "WHERE h.sequence.agency.agencyId = :agencyId " +
            "AND h.contentType = :contentType " +
            "AND h.isActive = true")
    long countByAgencyAndContentType(@Param("agencyId") Long agencyId,
                                     @Param("contentType") String contentType);

    /**
     * Find huddles that need review (old content)
     */
    @Query("SELECT h FROM Huddle h " +
            "WHERE h.sequence.agency.agencyId = :agencyId " +
            "AND h.lastReviewedAt IS NULL OR h.lastReviewedAt < :cutoffDate " +
            "AND h.isActive = true " +
            "ORDER BY h.createdAt ASC")
    List<Huddle> findHuddlesNeedingReview(@Param("agencyId") Long agencyId,
                                          @Param("cutoffDate") java.time.LocalDateTime cutoffDate);

    /**
     * Find incomplete huddles (missing content)
     */
    @Query("SELECT h FROM Huddle h " +
            "WHERE h.sequence.agency.agencyId = :agencyId " +
            "AND (h.objective IS NULL OR h.objective = '' OR " +
            "     h.keyContent IS NULL OR h.keyContent = '' OR " +
            "     h.title IS NULL OR h.title = '') " +
            "AND h.isActive = true " +
            "ORDER BY h.createdAt DESC")
    List<Huddle> findIncompleteHuddles(@Param("agencyId") Long agencyId);

    /**
     * Get content statistics for agency
     */
    @Query("SELECT h.contentType, COUNT(h), AVG(h.wordCount), AVG(h.estimatedMinutes) " +
            "FROM Huddle h " +
            "WHERE h.sequence.agency.agencyId = :agencyId " +
            "AND h.isActive = true " +
            "GROUP BY h.contentType")
    List<Object[]> getContentStatisticsByAgency(@Param("agencyId") Long agencyId);

    /**
     * Find huddles by reading level
     */
    @Query("SELECT h FROM Huddle h " +
            "WHERE h.sequence.agency.agencyId = :agencyId " +
            "AND h.readingLevel = :readingLevel " +
            "AND h.isActive = true " +
            "ORDER BY h.title")
    List<Huddle> findByAgencyAndReadingLevel(@Param("agencyId") Long agencyId,
                                             @Param("readingLevel") String readingLevel);

    /**
     * Search huddles by content (full-text search)
     */
    @Query("SELECT h FROM Huddle h " +
            "WHERE h.sequence.agency.agencyId = :agencyId " +
            "AND (LOWER(h.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "     LOWER(h.objective) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "     LOWER(h.keyContent) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND h.isActive = true " +
            "ORDER BY h.updatedAt DESC")
    List<Huddle> searchByContent(@Param("agencyId") Long agencyId,
                                 @Param("searchTerm") String searchTerm);
}