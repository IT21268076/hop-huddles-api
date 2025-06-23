package com.hqc.hophuddles.entity;

import com.hqc.hophuddles.enums.EventType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "engagement_events",
        indexes = {
                @Index(name = "idx_events_user_date", columnList = "user_id, created_at"),
                @Index(name = "idx_events_huddle_type", columnList = "huddle_id, event_type"),
                @Index(name = "idx_events_sequence_type", columnList = "sequence_id, event_type"),
                @Index(name = "idx_events_session", columnList = "session_id"),
                @Index(name = "idx_events_type_date", columnList = "event_type, created_at"),
                @Index(name = "idx_events_agency_date", columnList = "agency_id, created_at")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class EngagementEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "huddle_id")
    @ToString.Exclude
    private Huddle huddle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sequence_id")
    @ToString.Exclude
    private HuddleSequence sequence;

    // Denormalized for analytics
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id")
    @ToString.Exclude
    private Agency agency;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private EventType eventType;

    @Column(name = "event_data", columnDefinition = "TEXT")
    private String eventData; // JSON data specific to event type

    @Size(max = 255, message = "Session ID must not exceed 255 characters")
    @Column(name = "session_id", length = 255)
    private String sessionId; // For grouping related events

    @Size(max = 45, message = "IP address must not exceed 45 characters")
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Size(max = 1000, message = "User agent must not exceed 1000 characters")
    @Column(name = "user_agent", length = 1000)
    private String userAgent;

    // Constructor for huddle events
    public EngagementEvent(User user, Huddle huddle, EventType eventType, String sessionId) {
        this.user = user;
        this.huddle = huddle;
        this.sequence = huddle.getSequence();
        this.agency = huddle.getSequence().getAgency();
        this.eventType = eventType;
        this.sessionId = sessionId;
    }

    // Constructor for sequence events
    public EngagementEvent(User user, HuddleSequence sequence, EventType eventType, String sessionId) {
        this.user = user;
        this.sequence = sequence;
        this.agency = sequence.getAgency();
        this.eventType = eventType;
        this.sessionId = sessionId;
    }
}