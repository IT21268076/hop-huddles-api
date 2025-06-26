package com.hqc.hophuddles.repository;

import com.hqc.hophuddles.entity.AIGenerationJob;
import com.hqc.hophuddles.enums.GenerationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AIGenerationJobRepository extends JpaRepository<AIGenerationJob, Long> {

    Optional<AIGenerationJob> findByExternalJobId(String externalJobId);

    List<AIGenerationJob> findBySequenceSequenceIdOrderByCreatedAtDesc(Long sequenceId);

    @Query("SELECT job FROM AIGenerationJob job " +
            "WHERE job.jobStatus = :status " +
            "AND job.isActive = true " +
            "ORDER BY job.createdAt ASC")
    List<AIGenerationJob> findByStatus(@Param("status") GenerationStatus status);

    @Query("SELECT job FROM AIGenerationJob job " +
            "WHERE job.jobStatus IN ('PENDING', 'IN_PROGRESS') " +
            "AND job.createdAt < :cutoff " +
            "AND job.isActive = true")
    List<AIGenerationJob> findStaleJobs(@Param("cutoff") LocalDateTime cutoff);

    @Query("SELECT job FROM AIGenerationJob job " +
            "WHERE job.jobStatus = 'FAILED' " +
            "AND job.retryCount < job.maxRetries " +
            "AND job.isActive = true " +
            "ORDER BY job.createdAt ASC")
    List<AIGenerationJob> findRetryableJobs();

    @Query("SELECT COUNT(job) FROM AIGenerationJob job " +
            "WHERE job.sequence.agency.agencyId = :agencyId " +
            "AND job.jobStatus = :status " +
            "AND job.createdAt >= :since")
    long countByAgencyAndStatusSince(@Param("agencyId") Long agencyId,
                                     @Param("status") GenerationStatus status,
                                     @Param("since") LocalDateTime since);
}