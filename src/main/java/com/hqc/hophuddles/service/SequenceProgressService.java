package com.hqc.hophuddles.service;

import com.hqc.hophuddles.dto.response.SequenceProgressResponse;
import com.hqc.hophuddles.entity.*;
import com.hqc.hophuddles.enums.ProgressStatus;
import com.hqc.hophuddles.exception.ResourceNotFoundException;
import com.hqc.hophuddles.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SequenceProgressService {

    private final SequenceProgressRepository sequenceProgressRepository;
    private final UserProgressRepository userProgressRepository;
    private final UserRepository userRepository;
    private final HuddleSequenceRepository sequenceRepository;

    public void updateSequenceProgress(Long userId, Long sequenceId) {
        User user = findUserById(userId);
        HuddleSequence sequence = findSequenceById(sequenceId);

        SequenceProgress sequenceProgress = findOrCreateSequenceProgress(user, sequence);

        // Calculate progress from individual huddle progress
        List<UserProgress> huddleProgressList = userProgressRepository
                .findByUserUserIdAndSequenceSequenceIdAndIsActiveTrueOrderByHuddleOrderIndexAsc(userId, sequenceId);

        // Calculate metrics
        long completedCount = huddleProgressList.stream()
                .mapToLong(up -> up.isCompleted() ? 1 : 0)
                .sum();

        BigDecimal totalTimeSpent = huddleProgressList.stream()
                .map(UserProgress::getTimeSpentMinutes)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageScore = huddleProgressList.stream()
                .filter(up -> up.getAssessmentScore() != null)
                .map(UserProgress::getAssessmentScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(Math.max(1, huddleProgressList.size())), 2, RoundingMode.HALF_UP);

        // Update sequence progress
        sequenceProgress.updateProgress((int) completedCount, totalTimeSpent, averageScore);
        sequenceProgressRepository.save(sequenceProgress);

        log.debug("Updated sequence progress for user {} on sequence {}: {}/{} completed",
                userId, sequenceId, completedCount, sequence.getTotalHuddles());
    }

    @Transactional(readOnly = true)
    public List<SequenceProgressResponse> getUserSequenceProgress(Long userId) {
        return sequenceProgressRepository.findByUserUserIdAndIsActiveTrueOrderByLastAccessedDesc(userId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SequenceProgressResponse> getSequenceProgressBySequence(Long sequenceId) {
        return sequenceProgressRepository.findBySequenceSequenceIdAndIsActiveTrueOrderByCompletionPercentageDesc(sequenceId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SequenceProgressResponse> getAgencySequenceProgress(Long agencyId) {
        return sequenceProgressRepository.findByAgencyAgencyIdAndIsActiveTrueOrderByLastAccessedDesc(agencyId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SequenceProgressResponse getSequenceProgressByUserAndSequence(Long userId, Long sequenceId) {
        return sequenceProgressRepository.findByUserUserIdAndSequenceSequenceIdAndIsActiveTrue(userId, sequenceId)
                .map(this::convertToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Sequence progress not found for user " + userId + " and sequence " + sequenceId));
    }

    // Helper methods
    private SequenceProgress findOrCreateSequenceProgress(User user, HuddleSequence sequence) {
        return sequenceProgressRepository.findByUserUserIdAndSequenceSequenceIdAndIsActiveTrue(
                        user.getUserId(), sequence.getSequenceId())
                .orElseGet(() -> {
                    SequenceProgress newProgress = new SequenceProgress(user, sequence, sequence.getAgency());
                    return sequenceProgressRepository.save(newProgress);
                });
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .filter(User::getIsActive)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private HuddleSequence findSequenceById(Long sequenceId) {
        return sequenceRepository.findById(sequenceId)
                .filter(HuddleSequence::getIsActive)
                .orElseThrow(() -> new ResourceNotFoundException("HuddleSequence", sequenceId));
    }

    private SequenceProgressResponse convertToResponse(SequenceProgress progress) {
        return SequenceProgressResponse.builder()
                .sequenceProgressId(progress.getSequenceProgressId())
                .userId(progress.getUser().getUserId())
                .userName(progress.getUser().getName())
                .sequenceId(progress.getSequence().getSequenceId())
                .sequenceTitle(progress.getSequence().getTitle())
                .agencyId(progress.getAgency().getAgencyId())
                .agencyName(progress.getAgency().getName())
                .totalHuddles(progress.getTotalHuddles())
                .completedHuddles(progress.getCompletedHuddles())
                .completionPercentage(progress.getCompletionPercentage())
                .totalTimeSpentMinutes(progress.getTotalTimeSpentMinutes())
                .averageScore(progress.getAverageScore())
                .sequenceStatus(progress.getSequenceStatus())
                .startedAt(progress.getStartedAt())
                .completedAt(progress.getCompletedAt())
                .lastAccessed(progress.getLastAccessed())
                .build();
    }
}