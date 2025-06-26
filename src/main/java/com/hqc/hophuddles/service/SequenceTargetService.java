package com.hqc.hophuddles.service;

import com.hqc.hophuddles.dto.response.SequenceTargetResponse;
import com.hqc.hophuddles.entity.HuddleSequence;
import com.hqc.hophuddles.entity.SequenceTarget;
import com.hqc.hophuddles.entity.User;
import com.hqc.hophuddles.entity.UserAssignment;
import com.hqc.hophuddles.enums.TargetType;
import com.hqc.hophuddles.enums.UserRole;
import com.hqc.hophuddles.enums.Discipline;
import com.hqc.hophuddles.exception.ResourceNotFoundException;
import com.hqc.hophuddles.exception.ValidationException;
import com.hqc.hophuddles.repository.SequenceTargetRepository;
import com.hqc.hophuddles.repository.HuddleSequenceRepository;
import com.hqc.hophuddles.repository.UserAssignmentRepository;
import com.hqc.hophuddles.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class SequenceTargetService {

    @Autowired
    private SequenceTargetRepository targetRepository;

    @Autowired
    private HuddleSequenceRepository sequenceRepository;

    @Autowired
    private UserAssignmentRepository userAssignmentRepository;

    @Autowired
    private UserRepository userRepository;

    public SequenceTargetResponse addTarget(Long sequenceId, TargetType targetType, String targetValue) {
        // Validate sequence exists
        HuddleSequence sequence = sequenceRepository.findById(sequenceId)
                .filter(s -> s.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("HuddleSequence", sequenceId));

        // Check if target already exists
        if (targetRepository.existsBySequenceSequenceIdAndTargetTypeAndTargetValueAndIsActiveTrue(
                sequenceId, targetType, targetValue)) {
            throw new ValidationException("Target already exists for this sequence");
        }

        // Validate target value based on type
        validateTargetValue(targetType, targetValue);

        // Create target
        SequenceTarget target = new SequenceTarget();
        target.setSequence(sequence);
        target.setTargetType(targetType);
        target.setTargetValue(targetValue);

        target = targetRepository.save(target);

        return convertToResponse(target);
    }

    public List<User> getTargetUsers(Long sequenceId) {
        List<SequenceTargetResponse> targets = getTargetsBySequence(sequenceId);
        Set<User> targetUsers = new HashSet<>();

        for (SequenceTargetResponse target : targets) {
            switch (target.getTargetType()) {
                case DISCIPLINE:
                    Discipline discipline = Discipline.valueOf(target.getTargetValue());
                    List<UserAssignment> disciplineAssignments = userAssignmentRepository
                            .findByDisciplineAndIsActiveTrueOrderByUserNameAsc(discipline);
                    disciplineAssignments.forEach(assignment ->
                            targetUsers.add(assignment.getUser()));
                    break;

                case ROLE:
                    UserRole role = UserRole.valueOf(target.getTargetValue());
                    List<UserAssignment> roleAssignments = userAssignmentRepository
                            .findByRoleAndIsActiveTrueOrderByUserNameAsc(role);
                    roleAssignments.forEach(assignment ->
                            targetUsers.add(assignment.getUser()));
                    break;

                case AGENCY:
                    Long agencyId = Long.parseLong(target.getTargetValue());
                    List<User> agencyUsers = userRepository.findActiveUsersByAgency(agencyId);
                    targetUsers.addAll(agencyUsers);
                    break;

                case BRANCH:
                    Long branchId = Long.parseLong(target.getTargetValue());
                    List<UserAssignment> branchAssignments = userAssignmentRepository
                            .findByBranchBranchIdAndIsActiveTrueOrderByUserNameAsc(branchId);
                    branchAssignments.forEach(assignment ->
                            targetUsers.add(assignment.getUser()));
                    break;

                case TEAM:
                    Long teamId = Long.parseLong(target.getTargetValue());
                    List<UserAssignment> teamAssignments = userAssignmentRepository
                            .findByTeamTeamIdAndIsActiveTrueOrderByUserNameAsc(teamId);
                    teamAssignments.forEach(assignment ->
                            targetUsers.add(assignment.getUser()));
                    break;

                case USER:
                    Long userId = Long.parseLong(target.getTargetValue());
                    userRepository.findById(userId)
                            .filter(User::getIsActive)
                            .ifPresent(targetUsers::add);
                    break;
            }
        }

        return new ArrayList<>(targetUsers);
    }

    @Transactional(readOnly = true)
    public List<SequenceTargetResponse> getTargetsBySequence(Long sequenceId) {
        return targetRepository.findBySequenceSequenceIdAndIsActiveTrueOrderByTargetTypeAsc(sequenceId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SequenceTargetResponse> getTargetsBySequenceAndType(Long sequenceId, TargetType targetType) {
        return targetRepository.findBySequenceSequenceIdAndTargetTypeAndIsActiveTrueOrderByTargetValueAsc(sequenceId, targetType)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Long> findSequencesForTarget(TargetType targetType, String targetValue) {
        return targetRepository.findPublishedSequenceIdsByTarget(targetType, targetValue);
    }

    public void removeTarget(Long targetId) {
        SequenceTarget target = targetRepository.findById(targetId)
                .filter(t -> t.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("SequenceTarget", targetId));

        // Soft delete
        target.setIsActive(false);
        targetRepository.save(target);
    }

    public void removeTargetByValues(Long sequenceId, TargetType targetType, String targetValue) {
        List<SequenceTarget> targets = targetRepository.findBySequenceSequenceIdAndTargetTypeAndIsActiveTrueOrderByTargetValueAsc(
                sequenceId, targetType);

        targets.stream()
                .filter(t -> t.getTargetValue().equals(targetValue))
                .forEach(t -> {
                    t.setIsActive(false);
                    targetRepository.save(t);
                });
    }

    private void validateTargetValue(TargetType targetType, String targetValue) {
        switch (targetType) {
            case ROLE:
                try {
                    UserRole.valueOf(targetValue);
                } catch (IllegalArgumentException e) {
                    throw new ValidationException("Invalid role: " + targetValue);
                }
                break;
            case DISCIPLINE:
                try {
                    Discipline.valueOf(targetValue);
                } catch (IllegalArgumentException e) {
                    throw new ValidationException("Invalid discipline: " + targetValue);
                }
                break;
            case AGENCY:
            case BRANCH:
            case TEAM:
            case USER:
                // For these, we expect numeric IDs
                try {
                    Long.parseLong(targetValue);
                } catch (NumberFormatException e) {
                    throw new ValidationException("Target value must be a valid ID for " + targetType);
                }
                break;
        }
    }

    // Helper method to convert entity to response DTO
    private SequenceTargetResponse convertToResponse(SequenceTarget target) {
        SequenceTargetResponse response = new SequenceTargetResponse();
        response.setTargetId(target.getTargetId());
        response.setTargetType(target.getTargetType());
        response.setTargetValue(target.getTargetValue());
        response.setTargetDisplayName(getTargetDisplayName(target.getTargetType(), target.getTargetValue()));

        return response;
    }

    private String getTargetDisplayName(TargetType targetType, String targetValue) {
        switch (targetType) {
            case ROLE:
                try {
                    return UserRole.valueOf(targetValue).getDisplayName();
                } catch (IllegalArgumentException e) {
                    return targetValue;
                }
            case DISCIPLINE:
                try {
                    return Discipline.valueOf(targetValue).getDisplayName();
                } catch (IllegalArgumentException e) {
                    return targetValue;
                }
            case AGENCY:
                return "Agency " + targetValue;
            case BRANCH:
                return "Branch " + targetValue;
            case TEAM:
                return "Team " + targetValue;
            case USER:
                return "User " + targetValue;
            default:
                return targetValue;
        }
    }
}