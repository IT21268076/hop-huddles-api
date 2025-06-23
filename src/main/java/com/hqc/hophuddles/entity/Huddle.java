package com.hqc.hophuddles.entity;

import com.hqc.hophuddles.enums.HuddleType;
import com.hqc.hophuddles.enums.SequenceStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Getter
@Setter
@Entity
@Table(name = "huddles", indexes = {
        @Index(name = "idx_huddle_sequence_order", columnList = "sequence_id, order_index, is_active"),
        @Index(name = "idx_huddle_type", columnList = "huddle_type"),
        @Index(name = "idx_huddle_sequence", columnList = "sequence_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_huddle_sequence_order", columnNames = {"sequence_id", "order_index"})
})
public class Huddle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "huddle_id")
    private Long huddleId;

    @NotNull(message = "Sequence is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sequence_id", nullable = false)
    private HuddleSequence sequence;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @NotNull(message = "Order index is required")
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Setter
    @Column(name = "content_json", columnDefinition = "TEXT")
    private String contentJson; // Structured content as JSON

    @Column(name = "voice_script", columnDefinition = "TEXT")
    private String voiceScript; // Script optimized for TTS

    @Size(max = 500, message = "PDF URL must not exceed 500 characters")
    @Column(name = "pdf_url", length = 500)
    private String pdfUrl; // Generated PDF location

    @Size(max = 500, message = "Audio URL must not exceed 500 characters")
    @Column(name = "audio_url", length = 500)
    private String audioUrl; // Generated audio location

    @Column(name = "duration_minutes")
    private Integer durationMinutes; // Individual huddle duration

    @Enumerated(EnumType.STRING)
    @Column(name = "huddle_type", length = 50)
    private HuddleType huddleType = HuddleType.STANDARD;

    @Column(name = "generation_metadata", columnDefinition = "TEXT")
    private String generationMetadata; // JSON metadata from AI generation

    // Constructors
    public Huddle() {}

    public Huddle(HuddleSequence sequence, String title, Integer orderIndex) {
        this.sequence = sequence;
        this.title = title;
        this.orderIndex = orderIndex;
    }

    // Business logic methods
    public boolean hasContent() {
        return contentJson != null && !contentJson.trim().isEmpty();
    }

    public boolean hasVoiceScript() {
        return voiceScript != null && !voiceScript.trim().isEmpty();
    }

    public boolean hasFiles() {
        return pdfUrl != null || audioUrl != null;
    }

    public boolean isComplete() {
        return hasContent() && hasVoiceScript();
    }

    @Override
    public String toString() {
        return "Huddle{" +
                "huddleId=" + huddleId +
                ", title='" + title + '\'' +
                ", orderIndex=" + orderIndex +
                ", type=" + huddleType +
                '}';
    }
}

