package com.hqc.hophuddles.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "assessments",
        indexes = {
                @Index(name = "idx_assessments_huddle", columnList = "huddle_id, is_active")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Assessment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assessment_id")
    private Long assessmentId;

    @NotNull(message = "Huddle is required")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "huddle_id", nullable = false)
    @ToString.Exclude
    private Huddle huddle;

    @Column(name = "questions_json", columnDefinition = "TEXT", nullable = false)
    private String questionsJson; // JSON array of questions

    @DecimalMin(value = "0.00", message = "Passing score must be at least 0")
    @DecimalMax(value = "100.00", message = "Passing score must not exceed 100")
    @Column(name = "passing_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal passingScore = new BigDecimal("70.00");

    @Column(name = "max_attempts")
    @Builder.Default
    private Integer maxAttempts = 3;

    @Column(name = "time_limit_minutes")
    private Integer timeLimitMinutes;

    // Constructor for basic assessment creation
    public Assessment(Huddle huddle, String questionsJson) {
        this.huddle = huddle;
        this.questionsJson = questionsJson;
        this.passingScore = new BigDecimal("70.00");
        this.maxAttempts = 3;
    }

    public boolean hasTimeLimit() {
        return timeLimitMinutes != null && timeLimitMinutes > 0;
    }

    public boolean isPassingScore(BigDecimal score) {
        return score.compareTo(passingScore) >= 0;
    }
}