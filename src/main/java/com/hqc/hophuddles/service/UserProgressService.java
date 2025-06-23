package com.hqc.hophuddles.service;

import com.hqc.hophuddles.dto.response.UserProgressResponse;
import com.hqc.hophuddles.entity.*;
import com.hqc.hophuddles.enums.ProgressStatus;
import com.hqc.hophuddles.exception.ResourceNotFoundException;
import com.hqc.hophuddles.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserProgressService {

    private final UserProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final HuddleRepository huddleRepository;
    private final HuddleSequenceRepository sequenceRepository;
    private final SequenceProgressService sequenceProgressService;

    public UserProgressResponse startHuddle(Long userId, Long huddleId) {
        User user = findUserById(userId);
        Huddle huddle = findHuddleById(huddleId);

        UserProgress progress = findOrCreateProgress(user, huddle);
        progress.startProgress();

        progress = progressRepository.save(progress);

        // Update sequence progress
        sequenceProgressService.updateSequenceProgress(userId, huddle.getSequence().getSequenceId());

        log.info("User {} started huddle {}", userId, huddleId);
        return convertToResponse(progress);
    }

    public UserProgressResponse updateProgress(Long userId, Long huddleId, BigDecimal completionPercentage) {
        UserProgress progress = findProgressByUserAndHuddle(userId, huddleId);

        progress.updateProgress(completionPercentage);
        progress = progressRepository.save(progress);

        // Update sequence progress
        sequenceProgressService.updateSequenceProgress(userId, progress.getSequence().getSequenceId());

        log.info("Updated progress for user {} on huddle {} to {}%", userId, huddleId, completionPercentage);
        return convertToResponse(progress);
    }

    public UserProgressResponse completeHuddle(Long userId, Long huddleId) {
        UserProgress progress = findProgressByUserAndHuddle(userId, huddleId);

        progress.completeProgress();
        progress = progressRepository.save(progress);

        // Update sequence progress
        sequenceProgressService.updateSequenceProgress(userId, progress.getSequence().getSequenceId());

        log.info("User {} completed huddle {}", userId, huddleId);
        return convertToResponse(progress);
    }

    public UserProgressResponse addTimeSpent(Long userId, Long huddleId, BigDecimal additionalMinutes) {
        UserProgress progress = findProgressByUserAndHuddle(userId, huddleId);

        progress.addTimeSpent(additionalMinutes);
        progress = progressRepository.save(progress);

        log.debug("Added {} minutes to user {} for huddle {}", additionalMinutes, userId, huddleId);
        return convertToResponse(progress);
    }

    public UserProgressResponse recordAssessmentAttempt(Long userId, Long huddleId, BigDecimal score) {
        UserProgress progress = findProgressByUserAndHuddle(userId, huddleId);

        progress.recordAssessmentAttempt(score);
        progress = progressRepository.save(progress);

        // Update sequence progress
        sequenceProgressService.updateSequenceProgress(userId, progress.getSequence().getSequenceId());

        log.info("User {} completed assessment for huddle {} with score {}", userId, huddleId, score);
        return convertToResponse(progress);
    }

    public UserProgressResponse addFeedback(Long userId, Long huddleId, String feedback) {
        UserProgress progress = findProgressByUserAndHuddle(userId, huddleId);

        progress.setFeedback(feedback);
        progress = progressRepository.save(progress);

        log.info("User {} added feedback for huddle {}", userId, huddleId);
        return convertToResponse(progress);
    }

    @Transactional(readOnly = true)
    public List<UserProgressResponse> getUserProgressBySequence(Long userId, Long sequenceId) {
        return progressRepository.findByUserUserIdAndSequenceSequenceIdAndIsActiveTrueOrderByHuddleOrderIndexAsc(userId, sequenceId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserProgressResponse> getAllUserProgress(Long userId) {
        return progressRepository.findByUserUserIdAndIsActiveTrueOrderByLastAccessedDesc(userId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserProgressResponse> getHuddleProgress(Long huddleId) {
        return progressRepository.findByHuddleHuddleIdAndIsActiveTrueOrderByCompletedAtDesc(huddleId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<UserProgressResponse> getProgressByUserAndHuddle(Long userId, Long huddleId) {
        return progressRepository.findByUserUserIdAndHuddleHuddleIdAndIsActiveTrue(userId, huddleId)
                .map(this::convertToResponse);
    }

    // Helper methods
    private UserProgress findOrCreateProgress(User user, Huddle huddle) {
        return progressRepository.findByUserUserIdAndHuddleHuddleIdAndIsActiveTrue(
                        user.getUserId(), huddle.getHuddleId())
                .orElseGet(() -> {
                    UserProgress newProgress = new UserProgress(user, huddle, huddle.getSequence());
                    return progressRepository.save(newProgress);
                });
    }

    private UserProgress findProgressByUserAndHuddle(Long userId, Long huddleId) {
        return progressRepository.findByUserUserIdAndHuddleHuddleIdAndIsActiveTrue(userId, huddleId)
                .orElseThrow(() -> new ResourceNotFoundException("Progress not found for user " + userId + " and huddle " + huddleId));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .filter(User::getIsActive)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private Huddle findHuddleById(Long huddleId) {
        return huddleRepository.findById(huddleId)
                .filter(Huddle::getIsActive)
                .orElseThrow(() -> new ResourceNotFoundException("Huddle", huddleId));
    }

    private UserProgressResponse convertToResponse(UserProgress progress) {
        return UserProgressResponse.builder()
                .progressId(progress.getProgressId())
                .userId(progress.getUser().getUserId())
                .userName(progress.getUser().getName())
                .huddleId(progress.getHuddle().getHuddleId())
                .huddleTitle(progress.getHuddle().getTitle())
                .sequenceId(progress.getSequence().getSequenceId())
                .sequenceTitle(progress.getSequence().getTitle())
                .progressStatus(progress.getProgressStatus())
                .completionPercentage(progress.getCompletionPercentage())
                .timeSpentMinutes(progress.getTimeSpentMinutes())
                .assessmentScore(progress.getAssessmentScore())
                .assessmentAttempts(progress.getAssessmentAttempts())
                .startedAt(progress.getStartedAt())
                .completedAt(progress.getCompletedAt())
                .lastAccessed(progress.getLastAccessed())
                .feedback(progress.getFeedback())
                .build();
    }
}