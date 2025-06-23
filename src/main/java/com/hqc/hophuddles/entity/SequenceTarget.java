package com.hqc.hophuddles.entity;

import com.hqc.hophuddles.enums.TargetType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "sequence_targets", indexes = {
        @Index(name = "idx_target_sequence", columnList = "sequence_id, is_active"),
        @Index(name = "idx_target_type_value", columnList = "target_type, target_value")
})
public class SequenceTarget extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "target_id")
    private Long targetId;

    @NotNull(message = "Sequence is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sequence_id", nullable = false)
    private HuddleSequence sequence;

    @NotNull(message = "Target type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 50)
    private TargetType targetType;

    @NotBlank(message = "Target value is required")
    @Size(max = 255, message = "Target value must not exceed 255 characters")
    @Column(name = "target_value", nullable = false, length = 255)
    private String targetValue; // ID or enum value

    // Constructors
    public SequenceTarget() {}

    public SequenceTarget(HuddleSequence sequence, TargetType targetType, String targetValue) {
        this.sequence = sequence;
        this.targetType = targetType;
        this.targetValue = targetValue;
    }

    // Getters and Setters
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }

    public HuddleSequence getSequence() { return sequence; }
    public void setSequence(HuddleSequence sequence) { this.sequence = sequence; }

    public TargetType getTargetType() { return targetType; }
    public void setTargetType(TargetType targetType) { this.targetType = targetType; }

    public String getTargetValue() { return targetValue; }
    public void setTargetValue(String targetValue) { this.targetValue = targetValue; }

    @Override
    public String toString() {
        return "SequenceTarget{" +
                "targetId=" + targetId +
                ", targetType=" + targetType +
                ", targetValue='" + targetValue + '\'' +
                '}';
    }
}