package com.hqc.hophuddles.service;

import com.hqc.hophuddles.entity.*;
import com.hqc.hophuddles.enums.EventType;
import com.hqc.hophuddles.exception.ResourceNotFoundException;
import com.hqc.hophuddles.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class EngagementService {

    private final EngagementEventRepository eventRepository;
    private final UserRepository userRepository;
    private final HuddleRepository huddleRepository;
    private final HuddleSequenceRepository sequenceRepository;

    public void recordHuddleEvent(Long userId, Long huddleId, EventType eventType, String sessionId, String eventData) {
        User user = findUserById(userId);
        Huddle huddle = findHuddleById(huddleId);

        EngagementEvent event = EngagementEvent.builder()
                .user(user)
                .huddle(huddle)
                .sequence(huddle.getSequence())
                .agency(huddle.getSequence().getAgency())
                .eventType(eventType)
                .sessionId(sessionId)
                .eventData(eventData)
                .build();

        eventRepository.save(event);

        log.debug("Recorded {} event for user {} on huddle {}", eventType, userId, huddleId);
    }

    public void recordSequenceEvent(Long userId, Long sequenceId, EventType eventType, String sessionId, String eventData) {
        User user = findUserById(userId);
        HuddleSequence sequence = findSequenceById(sequenceId);

        EngagementEvent event = EngagementEvent.builder()
                .user(user)
                .sequence(sequence)
                .agency(sequence.getAgency())
                .eventType(eventType)
                .sessionId(sessionId)
                .eventData(eventData)
                .build();

        eventRepository.save(event);

        log.debug("Recorded {} event for user {} on sequence {}", eventType, userId, sequenceId);
    }

    @Transactional(readOnly = true)
    public List<EngagementEvent> getUserEngagementHistory(Long userId) {
        return eventRepository.findByUserUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<EngagementEvent> getHuddleEngagementHistory(Long huddleId) {
        return eventRepository.findByHuddleHuddleIdAndIsActiveTrueOrderByCreatedAtDesc(huddleId);
    }

    @Transactional(readOnly = true)
    public List<EngagementEvent> getSequenceEngagementHistory(Long sequenceId) {
        return eventRepository.findBySequenceSequenceIdAndIsActiveTrueOrderByCreatedAtDesc(sequenceId);
    }

    @Transactional(readOnly = true)
    public List<EngagementEvent> getSessionEvents(String sessionId) {
        return eventRepository.findBySessionIdAndIsActiveTrueOrderByCreatedAtAsc(sessionId);
    }

    // Helper methods
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

    private HuddleSequence findSequenceById(Long sequenceId) {
        return sequenceRepository.findById(sequenceId)
                .filter(HuddleSequence::getIsActive)
                .orElseThrow(() -> new ResourceNotFoundException("HuddleSequence", sequenceId));
    }
}