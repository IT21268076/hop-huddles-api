package com.hqc.hophuddles.service;

import com.hqc.hophuddles.dto.request.UserCreateRequest;
import com.hqc.hophuddles.dto.response.UserResponse;
import com.hqc.hophuddles.entity.User;
import com.hqc.hophuddles.exception.ResourceNotFoundException;
import com.hqc.hophuddles.exception.ValidationException;
import com.hqc.hophuddles.repository.UserRepository;
import com.hqc.hophuddles.repository.UserAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAssignmentRepository userAssignmentRepository;

    @Autowired
    private UserAssignmentService userAssignmentService;

    public UserResponse createUser(UserCreateRequest request) {
        // Validate unique email and auth0Id
        if (userRepository.existsByEmailAndIsActiveTrue(request.getEmail())) {
            throw new ValidationException("User with email " + request.getEmail() + " already exists");
        }

        if (userRepository.existsByAuth0IdAndIsActiveTrue(request.getAuth0Id())) {
            throw new ValidationException("User with Auth0 ID " + request.getAuth0Id() + " already exists");
        }

        // Create and save user
        User user = new User();
        user.setAuth0Id(request.getAuth0Id());
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setProfilePictureUrl(request.getProfilePictureUrl());

        user = userRepository.save(user);

        return convertToResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .filter(u -> u.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        return convertToResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByAuth0Id(String auth0Id) {
        User user = userRepository.findByAuth0IdAndIsActiveTrue(auth0Id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with Auth0 ID: " + auth0Id));

        return convertToResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return convertToResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllActiveUsers() {
        return userRepository.findByIsActiveTrueOrderByNameAsc()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByAgency(Long agencyId) {
        return userRepository.findActiveUsersByAgency(agencyId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsersByAgency(Long agencyId, String searchTerm, Pageable pageable) {
        return userRepository.findUsersByAgencyWithSearch(agencyId, searchTerm, pageable)
                .map(this::convertToResponse);
    }

    public UserResponse updateUser(Long userId, UserCreateRequest request) {
        User user = userRepository.findById(userId)
                .filter(u -> u.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Validate email uniqueness (excluding current user)
        if (!request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmailAndIsActiveTrue(request.getEmail())) {
                throw new ValidationException("User with email " + request.getEmail() + " already exists");
            }
        }

        // Update fields
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setProfilePictureUrl(request.getProfilePictureUrl());

        user = userRepository.save(user);

        return convertToResponse(user);
    }

    public void updateLastLogin(Long userId) {
        userRepository.updateLastLogin(userId, LocalDateTime.now());
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .filter(u -> u.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Soft delete
        user.setIsActive(false);
        userRepository.save(user);
    }

    // Helper method to convert entity to response DTO
    private UserResponse convertToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setUserId(user.getUserId());
        response.setAuth0Id(user.getAuth0Id());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        response.setPhone(user.getPhone());
        response.setProfilePictureUrl(user.getProfilePictureUrl());
        response.setLastLogin(user.getLastLogin());
        response.setCreatedAt(user.getCreatedAt());

        // Load assignments if needed
        response.setAssignments(
                userAssignmentService.getAssignmentsByUserId(user.getUserId())
        );

        return response;
    }
}