package com.hqc.hophuddles.service;

import com.hqc.hophuddles.entity.DeliverySchedule;
import com.hqc.hophuddles.entity.HuddleSequence;
import com.hqc.hophuddles.entity.User;
import com.hqc.hophuddles.enums.ScheduleStatus;
import com.hqc.hophuddles.enums.SequenceStatus;
import com.hqc.hophuddles.repository.DeliveryScheduleRepository;
import com.hqc.hophuddles.repository.HuddleSequenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class HuddleSchedulerService {

    private final DeliveryScheduleRepository scheduleRepository;
    private final HuddleSequenceRepository sequenceRepository;
    private final NotificationService notificationService;
    private final UserService userService;
    private final SequenceTargetService sequenceTargetService;

    /**
     * Main scheduler that runs every minute to check for due releases
     */
    @Scheduled(fixedRate = 60000) // Check every minute
    @Transactional
    public void processScheduledReleases() {
        log.debug("Processing scheduled huddle releases...");

        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        List<DeliverySchedule> dueSchedules = scheduleRepository.findSchedulesReadyForExecution(now);

        log.info("Found {} schedules ready for execution", dueSchedules.size());

        for (DeliverySchedule schedule : dueSchedules) {
            try {
                processScheduleExecution(schedule);
            } catch (Exception e) {
                log.error("Error processing schedule {}: {}", schedule.getScheduleId(), e.getMessage(), e);
                handleScheduleFailure(schedule, e.getMessage());
            }
        }
    }

    /**
     * Process individual schedule execution
     */
    @Async("schedulerTaskExecutor")
    public CompletableFuture<Void> processScheduleExecution(DeliverySchedule schedule) {
        try {
            log.info("Executing schedule {} for sequence {}",
                    schedule.getScheduleId(), schedule.getSequence().getSequenceId());

            // Auto-publish the sequence if enabled
            if (schedule.getAutoPublish()) {
                publishSequence(schedule.getSequence());
            }

            // Send notifications to target users
            if (schedule.getSendNotifications()) {
                sendReleaseNotifications(schedule);
            }

            // Mark schedule as executed and calculate next execution
            schedule.markExecuted();
            schedule.calculateNextExecution();

            // Check if schedule should be completed
            if (schedule.isExpired() || schedule.hasReachedMaxExecutions()) {
                schedule.setScheduleStatus(ScheduleStatus.COMPLETED);
            }

            scheduleRepository.save(schedule);

            log.info("Successfully executed schedule {} for sequence {}",
                    schedule.getScheduleId(), schedule.getSequence().getSequenceId());

        } catch (Exception e) {
            log.error("Failed to execute schedule {}: {}", schedule.getScheduleId(), e.getMessage(), e);
            handleScheduleFailure(schedule, e.getMessage());
            throw e;
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Auto-publish a sequence
     */
    private void publishSequence(HuddleSequence sequence) {
        if (sequence.getSequenceStatus() != SequenceStatus.PUBLISHED) {
            sequence.setSequenceStatus(SequenceStatus.PUBLISHED);
            sequence.setPublishedAt(LocalDateTime.now());
            sequenceRepository.save(sequence);

            log.info("Auto-published sequence: {}", sequence.getSequenceId());
        }
    }

    /**
     * Send notifications to target users
     */
    @Async("notificationTaskExecutor")
    public CompletableFuture<Void> sendReleaseNotifications(DeliverySchedule schedule) {
        try {
            HuddleSequence sequence = schedule.getSequence();

            // Get target users based on sequence targeting rules
            List<User> targetUsers = getTargetUsers(sequence);

            // Send release notifications
            notificationService.sendHuddleReleaseNotification(targetUsers, sequence);

            log.info("Sent release notifications for sequence {} to {} users",
                    sequence.getSequenceId(), targetUsers.size());

        } catch (Exception e) {
            log.error("Failed to send release notifications for sequence {}: {}",
                    schedule.getSequence().getSequenceId(), e.getMessage(), e);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Get target users for a sequence based on targeting rules
     */
    private List<User> getTargetUsers(HuddleSequence sequence) {
        // This would use the sequence targeting service to find matching users
        return sequenceTargetService.getTargetUsers(sequence.getSequenceId());
    }

    /**
     * Handle schedule execution failure
     */
    private void handleScheduleFailure(DeliverySchedule schedule, String errorMessage) {
        schedule.markFailed(errorMessage);
        scheduleRepository.save(schedule);

        // Send alert to administrators
        notificationService.sendScheduleFailureAlert(schedule, errorMessage);
    }

    /**
     * Create a new delivery schedule
     */
    public DeliverySchedule createSchedule(Long sequenceId, DeliverySchedule scheduleRequest) {
        HuddleSequence sequence = sequenceRepository.findById(sequenceId)
                .orElseThrow(() -> new ResourceNotFoundException("HuddleSequence", sequenceId));

        DeliverySchedule schedule = DeliverySchedule.builder()
                .sequence(sequence)
                .frequencyType(scheduleRequest.getFrequencyType())
                .startDate(scheduleRequest.getStartDate())
                .endDate(scheduleRequest.getEndDate())
                .releaseTime(scheduleRequest.getReleaseTime())
                .daysOfWeek(scheduleRequest.getDaysOfWeek())
                .timeZone(scheduleRequest.getTimeZone())
                .autoPublish(scheduleRequest.getAutoPublish())
                .sendNotifications(scheduleRequest.getSendNotifications())
                .notificationHoursBefore(scheduleRequest.getNotificationHoursBefore())
                .reminderHoursBefore(scheduleRequest.getReminderHoursBefore())
                .maxExecutions(scheduleRequest.getMaxExecutions())
                .scheduleStatus(ScheduleStatus.ACTIVE)
                .build();

        // Calculate first execution time
        schedule.calculateNextExecution();

        return scheduleRepository.save(schedule);
    }

    /**
     * Update an existing schedule
     */
    public DeliverySchedule updateSchedule(Long scheduleId, DeliverySchedule scheduleUpdate) {
        DeliverySchedule existingSchedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliverySchedule", scheduleId));

        // Update fields
        existingSchedule.setFrequencyType(scheduleUpdate.getFrequencyType());
        existingSchedule.setStartDate(scheduleUpdate.getStartDate());
        existingSchedule.setEndDate(scheduleUpdate.getEndDate());
        existingSchedule.setReleaseTime(scheduleUpdate.getReleaseTime());
        existingSchedule.setDaysOfWeek(scheduleUpdate.getDaysOfWeek());
        existingSchedule.setTimeZone(scheduleUpdate.getTimeZone());
        existingSchedule.setAutoPublish(scheduleUpdate.getAutoPublish());
        existingSchedule.setSendNotifications(scheduleUpdate.getSendNotifications());
        existingSchedule.setNotificationHoursBefore(scheduleUpdate.getNotificationHoursBefore());
        existingSchedule.setReminderHoursBefore(scheduleUpdate.getReminderHoursBefore());
        existingSchedule.setMaxExecutions(scheduleUpdate.getMaxExecutions());

        // Recalculate next execution
        existingSchedule.calculateNextExecution();

        return scheduleRepository.save(existingSchedule);
    }

    /**
     * Pause a schedule
     */
    public void pauseSchedule(Long scheduleId) {
        DeliverySchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliverySchedule", scheduleId));

        schedule.setScheduleStatus(ScheduleStatus.PAUSED);
        scheduleRepository.save(schedule);

        log.info("Paused schedule: {}", scheduleId);
    }

    /**
     * Resume a paused schedule
     */
    public void resumeSchedule(Long scheduleId) {
        DeliverySchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliverySchedule", scheduleId));

        schedule.setScheduleStatus(ScheduleStatus.ACTIVE);
        schedule.calculateNextExecution();
        scheduleRepository.save(schedule);

        log.info("Resumed schedule: {}", scheduleId);
    }

    /**
     * Cancel a schedule
     */
    public void cancelSchedule(Long scheduleId) {
        DeliverySchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliverySchedule", scheduleId));

        schedule.setScheduleStatus(ScheduleStatus.CANCELLED);
        schedule.setIsActive(false);
        scheduleRepository.save(schedule);

        log.info("Cancelled schedule: {}", scheduleId);
    }

    /**
     * Get schedules for a sequence
     */
    public List<DeliverySchedule> getSequenceSchedules(Long sequenceId) {
        return scheduleRepository.findBySequenceId(sequenceId);
    }

    /**
     * Get schedules for an agency
     */
    public List<DeliverySchedule> getAgencySchedules(Long agencyId) {
        return scheduleRepository.findByAgencyId(agencyId);
    }

    /**
     * Send reminder notifications (runs hourly)
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    @Transactional(readOnly = true)
    public void sendReminderNotifications() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextHour = now.plusHours(1);

        List<DeliverySchedule> upcomingSchedules = scheduleRepository.findSchedulesReadyForExecution(nextHour);

        for (DeliverySchedule schedule : upcomingSchedules) {
            if (schedule.getSendNotifications() && schedule.getReminderHoursBefore() != null) {
                LocalDateTime reminderTime = schedule.getNextExecutionTime()
                        .minusHours(schedule.getReminderHoursBefore());

                if (now.isAfter(reminderTime) && now.isBefore(reminderTime.plusMinutes(30))) {
                    sendReminderNotifications(schedule);
                }
            }
        }
    }

    @Async("notificationTaskExecutor")
    public CompletableFuture<Void> sendReminderNotifications(DeliverySchedule schedule) {
        try {
            List<User> targetUsers = getTargetUsers(schedule.getSequence());
            notificationService.sendHuddleReminderNotification(targetUsers, schedule.getSequence());

            log.info("Sent reminder notifications for sequence {} to {} users",
                    schedule.getSequence().getSequenceId(), targetUsers.size());

        } catch (Exception e) {
            log.error("Failed to send reminder notifications for sequence {}: {}",
                    schedule.getSequence().getSequenceId(), e.getMessage(), e);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Cleanup completed schedules (runs daily)
     */
    @Scheduled(cron = "0 0 2 * * ?") // 2 AM daily
    @Transactional
    public void cleanupCompletedSchedules() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);

        List<DeliverySchedule> completedSchedules = scheduleRepository.findByStatus(ScheduleStatus.COMPLETED);

        for (DeliverySchedule schedule : completedSchedules) {
            if (schedule.getLastExecutionTime() != null &&
                    schedule.getLastExecutionTime().isBefore(cutoff)) {
                schedule.setIsActive(false);
                scheduleRepository.save(schedule);
            }
        }

        log.info("Cleaned up old completed schedules");
    }
}