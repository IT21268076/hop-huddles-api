package com.hqc.hophuddles.entity;

import com.hqc.hophuddles.enums.ProgressStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_progress",
        indexes = {
                @Index(name = "idx_progress_user_huddle", columnList = "user_id, huddle_id", unique = true),
                @Index(name = "idx_progress_user_sequence", columnList = "user_id, sequence_id"),
                @Index(name = "idx_progress_huddle_status", columnList = "huddle_id, progress_status"),
                @Index(name = "idx_progress_completion", columnList = "completed_at"),
                @Index(name = "idx_progress_user_status", columnList = "user_id, progress_status")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class UserProgress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id")
    private Long progressId;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @NotNull(message = "Huddle is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "huddle_id", nullable = false)
    @ToString.Exclude
    private Huddle huddle;

    // Denormalized for faster queries
    @NotNull(message = "Sequence is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sequence_id", nullable = false)
    @ToString.Exclude
    private HuddleSequence sequence;

    @Enumerated(EnumType.STRING)
    @Column(name = "progress_status", nullable = false, length = 50)
    @Builder.Default
    private ProgressStatus progressStatus = ProgressStatus.NOT_STARTED;

    @DecimalMin(value = "0.00", message = "Completion percentage must be at least 0")
    @DecimalMax(value = "100.00", message = "Completion percentage must not exceed 100")
    @Column(name = "completion_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal completionPercentage = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "Time spent must be non-negative")
    @Column(name = "time_spent_minutes", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal timeSpentMinutes = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "Assessment score must be at least 0")
    @DecimalMax(value = "100.00", message = "Assessment score must not exceed 100")
    @Column(name = "assessment_score", precision = 5, scale = 2)
    private BigDecimal assessmentScore;

    @Column(name = "assessment_attempts")
    @Builder.Default
    private Integer assessmentAttempts = 0;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;

    @Column(name = "feedback", length = 2000)
    private String feedback;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON for additional tracking data

    // Constructor for basic progress creation
    public UserProgress(User user, Huddle huddle, HuddleSequence sequence) {
        this.user = user;
        this.huddle = huddle;
        this.sequence = sequence;
        this.progressStatus = ProgressStatus.NOT_STARTED;
        this.completionPercentage = BigDecimal.ZERO;
        this.timeSpentMinutes = BigDecimal.ZERO;
        this.assessmentAttempts = 0;
    }

    // Business methods
    public void startProgress() {
        if (this.progressStatus == ProgressStatus.NOT_STARTED) {
            this.progressStatus = ProgressStatus.IN_PROGRESS;
            this.startedAt = LocalDateTime.now();
        }
        this.lastAccessed = LocalDateTime.now();
    }

    public void completeProgress() {
        this.progressStatus = ProgressStatus.COMPLETED;
        this.completionPercentage = new BigDecimal("100.00");
        this.completedAt = LocalDateTime.now();
        this.lastAccessed = LocalDateTime.now();
    }

    public void updateProgress(BigDecimal percentage) {
        this.completionPercentage = percentage;
        this.lastAccessed = LocalDateTime.now();

        if (percentage.compareTo(BigDecimal.ZERO) > 0 && this.progressStatus == ProgressStatus.NOT_STARTED) {
            startProgress();
        }

        if (percentage.compareTo(new BigDecimal("100.00")) >= 0 && this.progressStatus != ProgressStatus.COMPLETED) {
            completeProgress();
        }
    }

    public void addTimeSpent(BigDecimal additionalMinutes) {
        this.timeSpentMinutes = this.timeSpentMinutes.add(additionalMinutes);
        this.lastAccessed = LocalDateTime.now();
    }

    public void recordAssessmentAttempt(BigDecimal score) {
        this.assessmentAttempts++;
        this.assessmentScore = score;
        this.lastAccessed = LocalDateTime.now();

        // If passed assessment, mark as completed
        if (score.compareTo(new BigDecimal("70.00")) >= 0) {
            completeProgress();
        }
    }

    public boolean isCompleted() {
        return progressStatus == ProgressStatus.COMPLETED;
    }

    public boolean isInProgress() {
        return progressStatus == ProgressStatus.IN_PROGRESS;
    }
}