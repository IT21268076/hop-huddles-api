package com.hqc.hophuddles.entity;

import com.hqc.hophuddles.enums.FrequencyType;
import com.hqc.hophuddles.enums.ScheduleStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Set;

@Entity
@Table(name = "delivery_schedules", indexes = {
        @Index(name = "idx_schedule_next_execution", columnList = "next_execution_time, is_active"),
        @Index(name = "idx_schedule_sequence", columnList = "sequence_id, is_active"),
        @Index(name = "idx_schedule_status", columnList = "schedule_status, next_execution_time")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class DeliverySchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;

    @NotNull(message = "Sequence is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sequence_id", nullable = false)
    @ToString.Exclude
    private HuddleSequence sequence;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency_type", nullable = false, length = 20)
    private FrequencyType frequencyType;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "release_time")
    private LocalTime releaseTime;

    // For custom scheduling (cron expression)
    @Column(name = "cron_expression", length = 100)
    private String cronExpression;

    // Days of week for weekly scheduling
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "schedule_days",
            joinColumns = @JoinColumn(name = "schedule_id")
    )
    @Column(name = "day_of_week")
    private Set<DayOfWeek> daysOfWeek;

    // Target timezone
    @Column(name = "time_zone", length = 50)
    @Builder.Default
    private String timeZone = "UTC";

    @Column(name = "auto_publish")
    @Builder.Default
    private Boolean autoPublish = true;

    // Notification settings
    @Column(name = "send_notifications")
    @Builder.Default
    private Boolean sendNotifications = true;

    @Column(name = "notification_hours_before")
    @Builder.Default
    private Integer notificationHoursBefore = 24;

    @Column(name = "reminder_hours_before")
    @Builder.Default
    private Integer reminderHoursBefore = 1;

    // Execution tracking
    @Column(name = "next_execution_time")
    private LocalDateTime nextExecutionTime;

    @Column(name = "last_execution_time")
    private LocalDateTime lastExecutionTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_status", length = 20)
    @Builder.Default
    private ScheduleStatus scheduleStatus = ScheduleStatus.ACTIVE;

    @Column(name = "execution_count")
    @Builder.Default
    private Integer executionCount = 0;

    @Column(name = "max_executions")
    private Integer maxExecutions;

    // Error tracking
    @Column(name = "last_error_message", length = 1000)
    private String lastErrorMessage;

    @Column(name = "consecutive_failures")
    @Builder.Default
    private Integer consecutiveFailures = 0;

    // Business methods
    public ZoneId getTimeZoneId() {
        return ZoneId.of(timeZone);
    }

    public boolean isActive() {
        return scheduleStatus == ScheduleStatus.ACTIVE && getIsActive();
    }

    public boolean isExpired() {
        return endDate != null && LocalDateTime.now(getTimeZoneId()).isAfter(endDate);
    }

    public boolean hasReachedMaxExecutions() {
        return maxExecutions != null && executionCount >= maxExecutions;
    }

    public void markExecuted() {
        this.lastExecutionTime = LocalDateTime.now(getTimeZoneId());
        this.executionCount++;
        this.consecutiveFailures = 0;
        this.lastErrorMessage = null;
    }

    public void markFailed(String errorMessage) {
        this.lastErrorMessage = errorMessage;
        this.consecutiveFailures++;

        // Disable schedule after 3 consecutive failures
        if (consecutiveFailures >= 3) {
            this.scheduleStatus = ScheduleStatus.FAILED;
        }
    }

    public void calculateNextExecution() {
        if (frequencyType == FrequencyType.IMMEDIATE) {
            this.nextExecutionTime = null; // Execute once
            return;
        }

        LocalDateTime now = LocalDateTime.now(getTimeZoneId());
        LocalDateTime next = switch (frequencyType) {
            case DAILY -> calculateNextDaily(now);
            case WEEKLY -> calculateNextWeekly(now);
            case MONTHLY -> calculateNextMonthly(now);
            case CUSTOM -> calculateNextCustom(now);
            default -> null;
        };

        this.nextExecutionTime = next;
    }

    private LocalDateTime calculateNextDaily(LocalDateTime from) {
        LocalDateTime next = from.toLocalDate().atTime(releaseTime);
        if (next.isBefore(from) || next.isEqual(from)) {
            next = next.plusDays(1);
        }
        return next;
    }

    private LocalDateTime calculateNextWeekly(LocalDateTime from) {
        if (daysOfWeek == null || daysOfWeek.isEmpty()) {
            return from.plusWeeks(1);
        }

        LocalDateTime next = from.toLocalDate().atTime(releaseTime);
        DayOfWeek currentDay = from.getDayOfWeek();

        // Find next day in the week that matches our schedule
        for (int i = 0; i < 7; i++) {
            DayOfWeek checkDay = currentDay.plus(i);
            if (daysOfWeek.contains(checkDay)) {
                next = next.plusDays(i);
                if (next.isAfter(from)) {
                    return next;
                }
            }
        }

        // If no day found this week, go to next week
        return next.plusWeeks(1);
    }

    private LocalDateTime calculateNextMonthly(LocalDateTime from) {
        LocalDateTime next = from.toLocalDate().atTime(releaseTime);
        if (next.isBefore(from) || next.isEqual(from)) {
            next = next.plusMonths(1);
        }
        return next;
    }

    private LocalDateTime calculateNextCustom(LocalDateTime from) {
        // This would use a cron expression parser
        // For now, return null (would need spring-cron or similar library)
        return null;
    }
}