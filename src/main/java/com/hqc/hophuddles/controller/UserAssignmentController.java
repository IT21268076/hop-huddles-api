package com.hqc.hophuddles.controller;

import com.hqc.hophuddles.dto.request.UserAssignmentCreateRequest;
import com.hqc.hophuddles.dto.response.UserAssignmentResponse;
import com.hqc.hophuddles.enums.UserRole;
import com.hqc.hophuddles.enums.Discipline;
import com.hqc.hophuddles.service.UserAssignmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assignments")
@CrossOrigin(origins = "*")
public class UserAssignmentController {

    @Autowired
    private UserAssignmentService userAssignmentService;

    @PostMapping
    public ResponseEntity<UserAssignmentResponse> createAssignment(@Valid @RequestBody UserAssignmentCreateRequest request) {
        UserAssignmentResponse response = userAssignmentService.createAssignment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{assignmentId}")
    public ResponseEntity<UserAssignmentResponse> getAssignment(@PathVariable Long assignmentId) {
        UserAssignmentResponse response = userAssignmentService.getAssignmentById(assignmentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserAssignmentResponse>> getAssignmentsByUser(@PathVariable Long userId) {
        List<UserAssignmentResponse> assignments = userAssignmentService.getAssignmentsByUserId(userId);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/agency/{agencyId}")
    public ResponseEntity<List<UserAssignmentResponse>> getAssignmentsByAgency(@PathVariable Long agencyId) {
        List<UserAssignmentResponse> assignments = userAssignmentService.getAssignmentsByAgency(agencyId);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/agency/{agencyId}/role/{role}")
    public ResponseEntity<List<UserAssignmentResponse>> getAssignmentsByAgencyAndRole(
            @PathVariable Long agencyId,
            @PathVariable UserRole role) {
        List<UserAssignmentResponse> assignments = userAssignmentService.getAssignmentsByAgencyAndRole(agencyId, role);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/agency/{agencyId}/discipline/{discipline}")
    public ResponseEntity<List<UserAssignmentResponse>> getAssignmentsByAgencyAndDiscipline(
            @PathVariable Long agencyId,
            @PathVariable Discipline discipline) {
        List<UserAssignmentResponse> assignments = userAssignmentService.getAssignmentsByAgencyAndDiscipline(agencyId, discipline);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/user/{userId}/agencies")
    public ResponseEntity<List<Long>> getUserAccessibleAgencies(@PathVariable Long userId) {
        List<Long> agencyIds = userAssignmentService.getUserAccessibleAgencyIds(userId);
        return ResponseEntity.ok(agencyIds);
    }

    @GetMapping("/check-access")
    public ResponseEntity<Boolean> checkUserAgencyAccess(
            @RequestParam Long userId,
            @RequestParam Long agencyId) {
        boolean hasAccess = userAssignmentService.hasUserAccessToAgency(userId, agencyId);
        return ResponseEntity.ok(hasAccess);
    }

    @PutMapping("/{assignmentId}")
    public ResponseEntity<UserAssignmentResponse> updateAssignment(
            @PathVariable Long assignmentId,
            @Valid @RequestBody UserAssignmentCreateRequest request) {
        UserAssignmentResponse response = userAssignmentService.updateAssignment(assignmentId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long assignmentId) {
        userAssignmentService.deleteAssignment(assignmentId);
        return ResponseEntity.noContent().build();
    }
}