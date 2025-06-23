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
@Table(name = "sequence_progress",
        indexes = {
                @Index(name = "idx_seq_progress_user_sequence", columnList = "user_id, sequence_id", unique = true),
                @Index(name = "idx_seq_progress_user", columnList = "user_id"),
                @Index(name = "idx_seq_progress_sequence_status", columnList = "sequence_id, sequence_status"),
                @Index(name = "idx_seq_progress_agency", columnList = "agency_id, sequence_status"),
                @Index(name = "idx_seq_progress_completion", columnList = "completed_at")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class SequenceProgress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sequence_progress_id")
    private Long sequenceProgressId;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @NotNull(message = "Sequence is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sequence_id", nullable = false)
    @ToString.Exclude
    private HuddleSequence sequence;

    // Denormalized for analytics
    @NotNull(message = "Agency is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id", nullable = false)
    @ToString.Exclude
    private Agency agency;

    @Column(name = "total_huddles", nullable = false)
    private Integer totalHuddles;

    @Column(name = "completed_huddles")
    @Builder.Default
    private Integer completedHuddles = 0;

    @DecimalMin(value = "0.00", message = "Completion percentage must be at least 0")
    @DecimalMax(value = "100.00", message = "Completion percentage must not exceed 100")
    @Column(name = "completion_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal completionPercentage = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "Time spent must be non-negative")
    @Column(name = "total_time_spent_minutes", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalTimeSpentMinutes = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "Average score must be at least 0")
    @DecimalMax(value = "100.00", message = "Average score must not exceed 100")
    @Column(name = "average_score", precision = 5, scale = 2)
    private BigDecimal averageScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "sequence_status", nullable = false, length = 50)
    @Builder.Default
    private ProgressStatus sequenceStatus = ProgressStatus.NOT_STARTED;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;

    // Constructor for basic sequence progress creation
    public SequenceProgress(User user, HuddleSequence sequence, Agency agency) {
        this.user = user;
        this.sequence = sequence;
        this.agency = agency;
        this.totalHuddles = sequence.getTotalHuddles();
        this.completedHuddles = 0;
        this.completionPercentage = BigDecimal.ZERO;
        this.totalTimeSpentMinutes = BigDecimal.ZERO;
        this.sequenceStatus = ProgressStatus.NOT_STARTED;
    }

    // Business methods
    public void startSequence() {
        if (this.sequenceStatus == ProgressStatus.NOT_STARTED) {
            this.sequenceStatus = ProgressStatus.IN_PROGRESS;
            this.startedAt = LocalDateTime.now();
        }
        this.lastAccessed = LocalDateTime.now();
    }

    public void updateProgress(Integer completedCount, BigDecimal totalTimeSpent, BigDecimal avgScore) {
        this.completedHuddles = completedCount;
        this.totalTimeSpentMinutes = totalTimeSpent;
        this.averageScore = avgScore;

        // Calculate completion percentage
        if (this.totalHuddles > 0) {
            this.completionPercentage = new BigDecimal(completedCount)
                    .divide(new BigDecimal(totalHuddles), 2, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }

        // Update status
        if (completedCount > 0 && this.sequenceStatus == ProgressStatus.NOT_STARTED) {
            startSequence();
        }

        if (completedCount.equals(totalHuddles) && this.sequenceStatus != ProgressStatus.COMPLETED) {
            completeSequence();
        }

        this.lastAccessed = LocalDateTime.now();
    }

    public void completeSequence() {
        this.sequenceStatus = ProgressStatus.COMPLETED;
        this.completionPercentage = new BigDecimal("100.00");
        this.completedAt = LocalDateTime.now();
        this.lastAccessed = LocalDateTime.now();
    }

    public boolean isCompleted() {
        return sequenceStatus == ProgressStatus.COMPLETED;
    }

    public boolean isInProgress() {
        return sequenceStatus == ProgressStatus.IN_PROGRESS;
    }
}