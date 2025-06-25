package com.hqc.hophuddles.service;

import com.hqc.hophuddles.dto.response.AnalyticsResponse;
import com.hqc.hophuddles.entity.EngagementEvent;
import com.hqc.hophuddles.enums.EventType;
import com.hqc.hophuddles.enums.ProgressStatus;
import com.hqc.hophuddles.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final UserProgressRepository userProgressRepository;
    private final SequenceProgressRepository sequenceProgressRepository;
    private final EngagementEventRepository engagementEventRepository;
    private final HuddleSequenceRepository sequenceRepository;
    private final UserRepository userRepository;

    public AnalyticsResponse getAgencyAnalytics(Long agencyId) {
        return getAgencyAnalyticsSince(agencyId, LocalDateTime.now().minusDays(30));
    }

    public AnalyticsResponse getAgencyAnalyticsSince(Long agencyId, LocalDateTime since) {
        Map<String, Object> metrics = new HashMap<>();

        // Basic counts - using correct method names
        long totalSequences = sequenceRepository.countByAgencyAndStatus(agencyId, com.hqc.hophuddles.enums.SequenceStatus.PUBLISHED);
        long totalUsers = userRepository.countActiveUsersByAgency(agencyId);
        long activeUsers = engagementEventRepository.countActiveUsersByAgencySince(agencyId, since);

        // Progress metrics
        long completedSequences = sequenceProgressRepository.countCompletedByAgencySince(agencyId, since);
        long inProgressSequences = sequenceProgressRepository.countByAgencyAndStatus(agencyId, ProgressStatus.IN_PROGRESS);

        // Engagement metrics
        long totalViews = engagementEventRepository.countEventsByAgencyAndTypeSince(agencyId, EventType.VIEW, since);
        long totalDownloads = engagementEventRepository.countEventsByAgencyAndTypeSince(agencyId, EventType.DOWNLOAD, since);
        long totalAssessments = engagementEventRepository.countEventsByAgencyAndTypeSince(agencyId, EventType.ASSESSMENT_SUBMIT, since);

        // Daily engagement data
        List<Object[]> dailyEngagement = engagementEventRepository.getDailyEngagementByAgency(agencyId, since);

        metrics.put("totalSequences", totalSequences);
        metrics.put("totalUsers", totalUsers);
        metrics.put("activeUsers", activeUsers);
        metrics.put("completedSequences", completedSequences);
        metrics.put("inProgressSequences", inProgressSequences);
        metrics.put("totalViews", totalViews);
        metrics.put("totalDownloads", totalDownloads);
        metrics.put("totalAssessments", totalAssessments);
        metrics.put("dailyEngagement", dailyEngagement);

        // Calculate rates
        if (totalUsers > 0) {
            metrics.put("activeUserRate", (double) activeUsers / totalUsers * 100);
        }

        if (totalSequences > 0) {
            metrics.put("completionRate", (double) completedSequences / totalSequences * 100);
        }

        return AnalyticsResponse.builder()
                .agencyId(agencyId)
                .period("Last 30 days")
                .generatedAt(LocalDateTime.now())
                .metrics(metrics)
                .build();
    }

    public AnalyticsResponse getSequenceAnalytics(Long sequenceId) {
        Map<String, Object> metrics = new HashMap<>();

        // Progress metrics
        long totalUsers = userProgressRepository.countBySequenceAndStatus(sequenceId, ProgressStatus.NOT_STARTED) +
                userProgressRepository.countBySequenceAndStatus(sequenceId, ProgressStatus.IN_PROGRESS) +
                userProgressRepository.countBySequenceAndStatus(sequenceId, ProgressStatus.COMPLETED);

        long completedUsers = userProgressRepository.countBySequenceAndStatus(sequenceId, ProgressStatus.COMPLETED);
        long inProgressUsers = userProgressRepository.countBySequenceAndStatus(sequenceId, ProgressStatus.IN_PROGRESS);

        // Average metrics
        BigDecimal avgCompletion = userProgressRepository.getAverageCompletionBySequence(sequenceId);
        BigDecimal avgTimeSpent = userProgressRepository.getAverageTimeSpentBySequence(sequenceId);
        BigDecimal avgAssessmentScore = userProgressRepository.getAverageAssessmentScoreBySequence(sequenceId);

        // Engagement metrics
        List<Object[]> eventCounts = engagementEventRepository.countEventsByTypeForSequence(sequenceId);
        Map<String, Long> eventMetrics = new HashMap<>();
        for (Object[] row : eventCounts) {
            eventMetrics.put(row[0].toString(), (Long) row[1]);
        }

        // Sequence progress distribution
        List<Object[]> progressDistribution = sequenceProgressRepository.countByStatusInSequence(sequenceId);
        Map<String, Long> progressMetrics = new HashMap<>();
        for (Object[] row : progressDistribution) {
            progressMetrics.put(row[0].toString(), (Long) row[1]);
        }

        metrics.put("totalUsers", totalUsers);
        metrics.put("completedUsers", completedUsers);
        metrics.put("inProgressUsers", inProgressUsers);
        metrics.put("averageCompletion", avgCompletion);
        metrics.put("averageTimeSpent", avgTimeSpent);
        metrics.put("averageAssessmentScore", avgAssessmentScore);
        metrics.put("eventCounts", eventMetrics);
        metrics.put("progressDistribution", progressMetrics);

        if (totalUsers > 0) {
            metrics.put("completionRate", (double) completedUsers / totalUsers * 100);
        }

        return AnalyticsResponse.builder()
                .sequenceId(sequenceId)
                .period("All time")
                .generatedAt(LocalDateTime.now())
                .metrics(metrics)
                .build();
    }

    public AnalyticsResponse getUserAnalytics(Long userId) {
        Map<String, Object> metrics = new HashMap<>();

        // Basic counts
        long completedHuddles = userProgressRepository.countCompletedByUser(userId);
        BigDecimal totalTimeSpent = userProgressRepository.getTotalTimeSpentByUser(userId);

        // Recent activity
        List<EngagementEvent> recentEvents = engagementEventRepository
                .findByUserUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId)
                .stream()
                .limit(10)
                .toList();

        metrics.put("completedHuddles", completedHuddles);
        metrics.put("totalTimeSpent", totalTimeSpent);
        metrics.put("recentActivityCount", recentEvents.size());

        return AnalyticsResponse.builder()
                .userId(userId)
                .period("All time")
                .generatedAt(LocalDateTime.now())
                .metrics(metrics)
                .build();
    }

    public AnalyticsResponse getHuddleAnalytics(Long huddleId) {
        Map<String, Object> metrics = new HashMap<>();

        // Progress metrics
        long totalUsers = userProgressRepository.countBySequenceAndStatus(huddleId, ProgressStatus.NOT_STARTED) +
                userProgressRepository.countBySequenceAndStatus(huddleId, ProgressStatus.IN_PROGRESS) +
                userProgressRepository.countBySequenceAndStatus(huddleId, ProgressStatus.COMPLETED);

        // Event metrics
        List<Object[]> eventCounts = engagementEventRepository.countEventsByTypeForHuddle(huddleId);
        Map<String, Long> eventMetrics = new HashMap<>();
        for (Object[] row : eventCounts) {
            eventMetrics.put(row[0].toString(), (Long) row[1]);
        }

        metrics.put("totalUsers", totalUsers);
        metrics.put("eventCounts", eventMetrics);

        return AnalyticsResponse.builder()
                .huddleId(huddleId)
                .period("All time")
                .generatedAt(LocalDateTime.now())
                .metrics(metrics)
                .build();
    }
}