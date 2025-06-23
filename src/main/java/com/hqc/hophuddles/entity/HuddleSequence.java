package com.hqc.hophuddles.entity;

import com.hqc.hophuddles.enums.SequenceStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "huddle_sequences", indexes = {
        @Index(name = "idx_sequence_agency_status", columnList = "agency_id, sequence_status, is_active"),
        @Index(name = "idx_sequence_created_by", columnList = "created_by"),
        @Index(name = "idx_sequence_published", columnList = "published_at"),
        @Index(name = "idx_sequence_status", columnList = "sequence_status")
})
public class HuddleSequence extends BaseEntity {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sequence_id")
    private Long sequenceId;

    // Multi-tenant isolation
    @NotNull(message = "Agency is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id", nullable = false)
    private Agency agency;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Column(name = "description", length = 2000)
    private String description;

    @Size(max = 1000, message = "Topic must not exceed 1000 characters")
    @Column(name = "topic", length = 1000)
    private String topic; // Original prompt/topic for generation

    @Column(name = "total_huddles")
    private Integer totalHuddles = 0;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "sequence_status", nullable = false, length = 50)
    private SequenceStatus sequenceStatus = SequenceStatus.DRAFT;

    @Column(name = "generation_prompt", columnDefinition = "TEXT")
    private String generationPrompt; // Original generation prompt

    @Column(name = "generation_metadata", columnDefinition = "TEXT")
    private String generationMetadata; // JSON metadata from generation

    // Creator and publisher tracking
    @NotNull(message = "Creator is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_by")
    private User publishedBy;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    // Relationships
    @OneToMany(mappedBy = "sequence", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    private List<Huddle> huddles = new ArrayList<>();

    @OneToMany(mappedBy = "sequence", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SequenceTarget> targets = new ArrayList<>();

    // Constructors
    public HuddleSequence() {}

    public HuddleSequence(Agency agency, String title, User createdByUser) {
        this.agency = agency;
        this.title = title;
        this.createdByUser = createdByUser;
    }

    // Helper methods
    public void addHuddle(Huddle huddle) {
        huddles.add(huddle);
        huddle.setSequence(this);
        updateTotalHuddles();
    }

    public void removeHuddle(Huddle huddle) {
        huddles.remove(huddle);
        huddle.setSequence(null);
        updateTotalHuddles();
    }

    public void addTarget(SequenceTarget target) {
        targets.add(target);
        target.setSequence(this);
    }

    public void removeTarget(SequenceTarget target) {
        targets.remove(target);
        target.setSequence(null);
    }

    public void publish(User publishedByUser) {
        this.sequenceStatus = SequenceStatus.PUBLISHED;
        this.publishedBy = publishedByUser;
        this.publishedAt = LocalDateTime.now();
    }

    public void archive() {
        this.sequenceStatus = SequenceStatus.ARCHIVED;
    }

    private void updateTotalHuddles() {
        this.totalHuddles = huddles.size();
    }

    // Business logic methods
    public boolean canEdit() {
        return sequenceStatus.canEdit();
    }

    public boolean canPublish() {
        return sequenceStatus.canPublish() && !huddles.isEmpty();
    }

    public boolean isPublished() {
        return sequenceStatus.isActive();
    }

    @Override
    public String toString() {
        return "HuddleSequence{" +
                "sequenceId=" + sequenceId +
                ", title='" + title + '\'' +
                ", status=" + sequenceStatus +
                ", totalHuddles=" + totalHuddles +
                '}';
    }
}