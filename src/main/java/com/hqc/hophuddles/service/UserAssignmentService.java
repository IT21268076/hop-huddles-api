package com.hqc.hophuddles.service;

import com.hqc.hophuddles.dto.request.UserAssignmentCreateRequest;
import com.hqc.hophuddles.dto.response.UserAssignmentResponse;
import com.hqc.hophuddles.entity.User;
import com.hqc.hophuddles.entity.Agency;
import com.hqc.hophuddles.entity.UserAssignment;
import com.hqc.hophuddles.enums.UserRole;
import com.hqc.hophuddles.enums.Discipline;
import com.hqc.hophuddles.exception.ResourceNotFoundException;
import com.hqc.hophuddles.exception.ValidationException;
import com.hqc.hophuddles.repository.UserAssignmentRepository;
import com.hqc.hophuddles.repository.UserRepository;
import com.hqc.hophuddles.repository.AgencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserAssignmentService {

    @Autowired
    private UserAssignmentRepository userAssignmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AgencyRepository agencyRepository;

    public UserAssignmentResponse createAssignment(UserAssignmentCreateRequest request) {
        // Validate user exists
        User user = userRepository.findById(request.getUserId())
                .filter(u -> u.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        // Validate agency exists
        Agency agency = agencyRepository.findById(request.getAgencyId())
                .filter(a -> a.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("Agency", request.getAgencyId()));

        // If this is primary assignment, unset other primary assignments for the user
        if (Boolean.TRUE.equals(request.getIsPrimary())) {
            userAssignmentRepository.findByUserUserIdAndIsPrimaryTrueAndIsActiveTrue(request.getUserId())
                    .ifPresent(existingPrimary -> {
                        existingPrimary.setIsPrimary(false);
                        userAssignmentRepository.save(existingPrimary);
                    });
        }

        // Create assignment
        UserAssignment assignment = new UserAssignment();
        assignment.setUser(user);
        assignment.setAgency(agency);
        assignment.setRole(request.getRole());
        assignment.setDiscipline(request.getDiscipline());
        assignment.setIsPrimary(request.getIsPrimary());
        // TODO: Set branch and team when those services are implemented

        assignment = userAssignmentRepository.save(assignment);

        return convertToResponse(assignment);
    }

    @Transactional(readOnly = true)
    public UserAssignmentResponse getAssignmentById(Long assignmentId) {
        UserAssignment assignment = userAssignmentRepository.findById(assignmentId)
                .filter(a -> a.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("UserAssignment", assignmentId));

        return convertToResponse(assignment);
    }

    @Transactional(readOnly = true)
    public List<UserAssignmentResponse> getAssignmentsByUserId(Long userId) {
        return userAssignmentRepository.findByUserUserIdAndIsActiveTrueOrderByIsPrimaryDescAssignedAtDesc(userId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserAssignmentResponse> getAssignmentsByAgency(Long agencyId) {
        return userAssignmentRepository.findByAgencyAgencyIdAndIsActiveTrueOrderByUserNameAsc(agencyId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserAssignmentResponse> getAssignmentsByAgencyAndRole(Long agencyId, UserRole role) {
        return userAssignmentRepository.findByAgencyAgencyIdAndRoleAndIsActiveTrueOrderByUserNameAsc(agencyId, role)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserAssignmentResponse> getAssignmentsByAgencyAndDiscipline(Long agencyId, Discipline discipline) {
        return userAssignmentRepository.findByAgencyAgencyIdAndDisciplineAndIsActiveTrueOrderByUserNameAsc(agencyId, discipline)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean hasUserAccessToAgency(Long userId, Long agencyId) {
        return userAssignmentRepository.hasUserAccessToAgency(userId, agencyId);
    }

    @Transactional(readOnly = true)
    public boolean hasUserRoleInAgency(Long userId, Long agencyId, UserRole role) {
        return userAssignmentRepository.hasUserRoleInAgency(userId, agencyId, role);
    }

    @Transactional(readOnly = true)
    public List<Long> getUserAccessibleAgencyIds(Long userId) {
        return userAssignmentRepository.findAgencyIdsByUserId(userId);
    }

    public UserAssignmentResponse updateAssignment(Long assignmentId, UserAssignmentCreateRequest request) {
        UserAssignment assignment = userAssignmentRepository.findById(assignmentId)
                .filter(a -> a.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("UserAssignment", assignmentId));

        // If setting as primary, unset other primary assignments for the user
        if (Boolean.TRUE.equals(request.getIsPrimary()) && !assignment.getIsPrimary()) {
            userAssignmentRepository.findByUserUserIdAndIsPrimaryTrueAndIsActiveTrue(assignment.getUser().getUserId())
                    .ifPresent(existingPrimary -> {
                        existingPrimary.setIsPrimary(false);
                        userAssignmentRepository.save(existingPrimary);
                    });
        }

        // Update fields
        assignment.setRole(request.getRole());
        assignment.setDiscipline(request.getDiscipline());
        assignment.setIsPrimary(request.getIsPrimary());

        assignment = userAssignmentRepository.save(assignment);

        return convertToResponse(assignment);
    }

    public void deleteAssignment(Long assignmentId) {
        UserAssignment assignment = userAssignmentRepository.findById(assignmentId)
                .filter(a -> a.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("UserAssignment", assignmentId));

        // Soft delete
        assignment.setIsActive(false);
        userAssignmentRepository.save(assignment);
    }

    // Helper method to convert entity to response DTO
    private UserAssignmentResponse convertToResponse(UserAssignment assignment) {
        UserAssignmentResponse response = new UserAssignmentResponse();
        response.setAssignmentId(assignment.getAssignmentId());
        response.setUserId(assignment.getUser().getUserId());
        response.setUserName(assignment.getUser().getName());
        response.setAgencyId(assignment.getAgency().getAgencyId());
        response.setAgencyName(assignment.getAgency().getName());

        if (assignment.getBranch() != null) {
            response.setBranchId(assignment.getBranch().getBranchId());
            response.setBranchName(assignment.getBranch().getName());
        }

        if (assignment.getTeam() != null) {
            response.setTeamId(assignment.getTeam().getTeamId());
            response.setTeamName(assignment.getTeam().getName());
        }

        response.setDiscipline(assignment.getDiscipline());
        response.setRole(assignment.getRole());
        response.setIsPrimary(assignment.getIsPrimary());
        response.setAccessScope(assignment.getAccessScope());
        response.setAssignedAt(assignment.getAssignedAt());

        return response;
    }
}