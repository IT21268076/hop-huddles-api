package com.hqc.hophuddles.service;

import com.hqc.hophuddles.dto.request.HuddleSequenceCreateRequest;
import com.hqc.hophuddles.dto.response.HuddleSequenceResponse;
import com.hqc.hophuddles.entity.Agency;
import com.hqc.hophuddles.entity.HuddleSequence;
import com.hqc.hophuddles.entity.User;
import com.hqc.hophuddles.entity.SequenceTarget;
import com.hqc.hophuddles.enums.SequenceStatus;
import com.hqc.hophuddles.exception.ResourceNotFoundException;
import com.hqc.hophuddles.exception.ValidationException;
import com.hqc.hophuddles.repository.HuddleSequenceRepository;
import com.hqc.hophuddles.repository.AgencyRepository;
import com.hqc.hophuddles.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class HuddleSequenceService {

    @Autowired
    private HuddleSequenceRepository sequenceRepository;

    @Autowired
    private AgencyRepository agencyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SequenceTargetService sequenceTargetService;

    @Autowired
    private HuddleService huddleService;

    public HuddleSequenceResponse createSequence(HuddleSequenceCreateRequest request, Long createdByUserId) {
        // Validate agency exists
        Agency agency = agencyRepository.findById(request.getAgencyId())
                .filter(a -> a.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("Agency", request.getAgencyId()));

        // Validate creator exists
        User creator = userRepository.findById(createdByUserId)
                .filter(u -> u.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("User", createdByUserId));

        // Create sequence
        HuddleSequence sequence = new HuddleSequence();
        sequence.setAgency(agency);
        sequence.setTitle(request.getTitle());
        sequence.setDescription(request.getDescription());
        sequence.setTopic(request.getTopic());
        sequence.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
        sequence.setGenerationPrompt(request.getGenerationPrompt());
        sequence.setCreatedByUser(creator);
        sequence.setSequenceStatus(SequenceStatus.DRAFT);

        sequence = sequenceRepository.save(sequence);

        // Add targets if provided
        if (request.getTargets() != null && !request.getTargets().isEmpty()) {
            for (HuddleSequenceCreateRequest.TargetRequest targetRequest : request.getTargets()) {
                sequenceTargetService.addTarget(sequence.getSequenceId(),
                        targetRequest.getTargetType(), targetRequest.getTargetValue());
            }
        }

        return convertToResponse(sequence);
    }

    @Transactional(readOnly = true)
    public HuddleSequenceResponse getSequenceById(Long sequenceId) {
        HuddleSequence sequence = sequenceRepository.findById(sequenceId)
                .filter(s -> s.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("HuddleSequence", sequenceId));

        return convertToResponse(sequence);
    }

    @Transactional(readOnly = true)
    public List<HuddleSequenceResponse> getSequencesByAgency(Long agencyId) {
        return sequenceRepository.findByAgencyAgencyIdAndIsActiveTrueOrderByCreatedAtDesc(agencyId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HuddleSequenceResponse> getSequencesByAgencyAndStatus(Long agencyId, SequenceStatus status) {
        return sequenceRepository.findByAgencyAgencyIdAndSequenceStatusAndIsActiveTrueOrderByCreatedAtDesc(agencyId, status)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HuddleSequenceResponse> getSequencesByCreator(Long userId) {
        return sequenceRepository.findByCreatedByUserUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<HuddleSequenceResponse> searchSequences(Long agencyId, String title, SequenceStatus status, Pageable pageable) {
        return sequenceRepository.findSequencesWithFilters(agencyId, title, status, pageable)
                .map(this::convertToResponse);
    }

    public HuddleSequenceResponse updateSequence(Long sequenceId, HuddleSequenceCreateRequest request) {
        HuddleSequence sequence = sequenceRepository.findById(sequenceId)
                .filter(s -> s.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("HuddleSequence", sequenceId));

        // Check if sequence can be edited
        if (!sequence.canEdit()) {
            throw new ValidationException("Sequence cannot be edited in " + sequence.getSequenceStatus() + " status");
        }

        // Update fields
        sequence.setTitle(request.getTitle());
        sequence.setDescription(request.getDescription());
        sequence.setTopic(request.getTopic());
        sequence.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
        sequence.setGenerationPrompt(request.getGenerationPrompt());

        sequence = sequenceRepository.save(sequence);

        return convertToResponse(sequence);
    }

    public HuddleSequenceResponse updateSequenceStatus(Long sequenceId, SequenceStatus newStatus, Long updatedByUserId) {
        HuddleSequence sequence = sequenceRepository.findById(sequenceId)
                .filter(s -> s.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("HuddleSequence", sequenceId));

        User updatedByUser = userRepository.findById(updatedByUserId)
                .filter(u -> u.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("User", updatedByUserId));

        // Validate status transition
        if (newStatus == SequenceStatus.PUBLISHED && !sequence.canPublish()) {
            throw new ValidationException("Sequence cannot be published. It must be in REVIEW status and have at least one huddle.");
        }

        // Update status
        if (newStatus == SequenceStatus.PUBLISHED) {
            sequence.publish(updatedByUser);
        } else {
            sequence.setSequenceStatus(newStatus);
        }

        sequence = sequenceRepository.save(sequence);

        return convertToResponse(sequence);
    }

    public void deleteSequence(Long sequenceId) {
        HuddleSequence sequence = sequenceRepository.findById(sequenceId)
                .filter(s -> s.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("HuddleSequence", sequenceId));

        // Check if sequence can be deleted
        if (sequence.getSequenceStatus() == SequenceStatus.PUBLISHED) {
            throw new ValidationException("Published sequences cannot be deleted. Archive them instead.");
        }

        // Soft delete
        sequence.setIsActive(false);
        sequenceRepository.save(sequence);
    }

    @Transactional(readOnly = true)
    public boolean hasUserAccessToSequence(Long userId, Long sequenceId) {
        // This will be enhanced with proper permission checking
        return sequenceRepository.existsById(sequenceId);
    }

    // Helper method to convert entity to response DTO
    private HuddleSequenceResponse convertToResponse(HuddleSequence sequence) {
        HuddleSequenceResponse response = new HuddleSequenceResponse();
        response.setSequenceId(sequence.getSequenceId());
        response.setAgencyId(sequence.getAgency().getAgencyId());
        response.setAgencyName(sequence.getAgency().getName());
        response.setTitle(sequence.getTitle());
        response.setDescription(sequence.getDescription());
        response.setTopic(sequence.getTopic());
        response.setTotalHuddles(sequence.getTotalHuddles());
        response.setEstimatedDurationMinutes(sequence.getEstimatedDurationMinutes());
        response.setSequenceStatus(sequence.getSequenceStatus());
        response.setGenerationPrompt(sequence.getGenerationPrompt());
        response.setCreatedByUserId(sequence.getCreatedByUser().getUserId());
        response.setCreatedByUserName(sequence.getCreatedByUser().getName());

        if (sequence.getPublishedBy() != null) {
            response.setPublishedByUserId(sequence.getPublishedBy().getUserId());
            response.setPublishedByUserName(sequence.getPublishedBy().getName());
        }

        response.setPublishedAt(sequence.getPublishedAt());
        response.setCreatedAt(sequence.getCreatedAt());

        // Load huddles and targets
        response.setHuddles(huddleService.getHuddlesBySequence(sequence.getSequenceId()));
        response.setTargets(sequenceTargetService.getTargetsBySequence(sequence.getSequenceId()));

        return response;
    }
}