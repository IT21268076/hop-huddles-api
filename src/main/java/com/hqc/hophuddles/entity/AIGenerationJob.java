package com.hqc.hophuddles.entity;

import com.hqc.hophuddles.enums.GenerationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_generation_jobs", indexes = {
        @Index(name = "idx_generation_job_status", columnList = "job_status, created_at"),
        @Index(name = "idx_generation_sequence", columnList = "sequence_id, job_status"),
        @Index(name = "idx_generation_job_id", columnList = "external_job_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class AIGenerationJob extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "generation_job_id")
    private Long generationJobId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sequence_id", nullable = false)
    @ToString.Exclude
    private HuddleSequence sequence;

    @Column(name = "external_job_id", unique = true, length = 100)
    private String externalJobId; // ID from AI service

    @Enumerated(EnumType.STRING)
    @Column(name = "job_status", nullable = false, length = 30)
    @Builder.Default
    private GenerationStatus jobStatus = GenerationStatus.PENDING;

    @Column(name = "request_payload", columnDefinition = "TEXT")
    private String requestPayload; // JSON of the original request

    @Column(name = "response_payload", columnDefinition = "TEXT")
    private String responsePayload; // JSON of the AI response

    @Column(name = "generated_content", columnDefinition = "TEXT")
    private String generatedContent; // Generated huddle content

    @Column(name = "generated_files", columnDefinition = "TEXT")
    private String generatedFiles; // JSON array of generated file URLs

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "processing_time_seconds")
    private Float processingTimeSeconds;

    @Column(name = "tokens_used")
    private Integer tokensUsed;

    @Column(name = "model_version", length = 50)
    private String modelVersion;

    @Column(name = "quality_score")
    private Float qualityScore;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "max_retries")
    @Builder.Default
    private Integer maxRetries = 3;

    // Business methods
    public boolean isTerminal() {
        return jobStatus.isTerminal();
    }

    public boolean canRetry() {
        return retryCount < maxRetries && (jobStatus == GenerationStatus.FAILED);
    }

    public void markStarted() {
        this.jobStatus = GenerationStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
    }

    public void markCompleted(String content, String files) {
        this.jobStatus = GenerationStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.generatedContent = content;
        this.generatedFiles = files;

        if (startedAt != null) {
            this.processingTimeSeconds = (float) java.time.Duration.between(startedAt, completedAt).toSeconds();
        }
    }

    public void markFailed(String errorMessage, String errorCode) {
        this.jobStatus = GenerationStatus.FAILED;
        this.completedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
        this.retryCount++;

        if (startedAt != null) {
            this.processingTimeSeconds = (float) java.time.Duration.between(startedAt, completedAt).toSeconds();
        }
    }
}