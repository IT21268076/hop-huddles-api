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

    // NEW FIELDS FOR MANUAL CONTENT CREATION

    @Column(name = "objective", columnDefinition = "TEXT")
    private String objective; // Learning objective for the huddle

    @Column(name = "key_content", columnDefinition = "TEXT")
    private String keyContent; // Main content (supports HTML formatting)

    @Column(name = "action_items", columnDefinition = "TEXT")
    private String actionItems; // JSON array of action items

    @Column(name = "discussion_points", columnDefinition = "TEXT")
    private String discussionPoints; // JSON array of discussion questions

    @Column(name = "resources", columnDefinition = "TEXT")
    private String resources; // JSON array of additional resources

    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes; // Duration estimate

    @Column(name = "content_type", length = 50)
    private String contentType = "MANUAL"; // "MANUAL", "TEMPLATE", "AI_GENERATED"

    @Column(name = "template_id")
    private Long templateId; // Reference to template used (if any)

    @Column(name = "word_count")
    private Integer wordCount; // Auto-calculated word count

    @Column(name = "reading_level")
    private String readingLevel; // "BASIC", "INTERMEDIATE", "ADVANCED"

    @Column(name = "content_tags", columnDefinition = "TEXT")
    private String contentTags; // JSON array of content tags

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_user_id")
    private User reviewedBy;

    // HELPER METHODS FOR CONTENT MANAGEMENT

    public void updateWordCount() {
        if (keyContent != null) {
            String plainText = keyContent.replaceAll("<[^>]*>", ""); // Remove HTML tags
            this.wordCount = plainText.trim().split("\\s+").length;
        } else {
            this.wordCount = 0;
        }
    }

    public boolean isContentComplete() {
        return title != null && !title.trim().isEmpty() &&
                objective != null && !objective.trim().isEmpty() &&
                keyContent != null && !keyContent.trim().isEmpty();
    }

    public boolean canPublish() {
        return isContentComplete() &&
                huddleStatus != SequenceStatus.ARCHIVED &&
                isActive;
    }

    public void markAsReviewed(User reviewer) {
        this.reviewedBy = reviewer;
        this.lastReviewedAt = LocalDateTime.now();
    }

    // Content validation
    public ContentValidationResult validateContent() {
        ContentValidationResult result = new ContentValidationResult();

        if (title == null || title.trim().isEmpty()) {
            result.addError("Title is required");
        }

        if (objective == null || objective.trim().isEmpty()) {
            result.addError("Learning objective is required");
        }

        if (keyContent == null || keyContent.trim().isEmpty()) {
            result.addError("Main content is required");
        } else {
            updateWordCount();
            if (wordCount < 25) {
                result.addWarning("Content may be too short (less than 25 words)");
            }
            if (wordCount > 750) {
                result.addWarning("Content may be too long for a huddle (more than 750 words)");
            }
        }

        if (estimatedMinutes == null || estimatedMinutes < 1) {
            result.addWarning("Please set estimated duration");
        } else if (estimatedMinutes > 15) {
            result.addWarning("Huddles should typically be 15 minutes or less");
        }

        return result;
    }

    @PrePersist
    @PreUpdate
    private void updateContentMetadata() {
        updateWordCount();
        setUpdatedAt(LocalDateTime.now());
    }
}

