package com.hqc.hophuddles.controller;

import com.hqc.hophuddles.dto.request.ProgressUpdateRequest;
import com.hqc.hophuddles.dto.response.UserProgressResponse;
import com.hqc.hophuddles.service.UserProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/progress")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserProgressController {

    private final UserProgressService progressService;

    @PostMapping("/start")
    public ResponseEntity<UserProgressResponse> startHuddle(
            @RequestParam Long userId,
            @RequestParam Long huddleId) {
        UserProgressResponse response = progressService.startHuddle(userId, huddleId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update")
    public ResponseEntity<UserProgressResponse> updateProgress(@Valid @RequestBody ProgressUpdateRequest request) {
        UserProgressResponse response = progressService.updateProgress(
                request.getUserId(),
                request.getHuddleId(),
                request.getCompletionPercentage()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/complete")
    public ResponseEntity<UserProgressResponse> completeHuddle(
            @RequestParam Long userId,
            @RequestParam Long huddleId) {
        UserProgressResponse response = progressService.completeHuddle(userId, huddleId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/time")
    public ResponseEntity<UserProgressResponse> addTimeSpent(
            @RequestParam Long userId,
            @RequestParam Long huddleId,
            @RequestParam BigDecimal additionalMinutes) {
        UserProgressResponse response = progressService.addTimeSpent(userId, huddleId, additionalMinutes);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/assessment")
    public ResponseEntity<UserProgressResponse> recordAssessment(
            @RequestParam Long userId,
            @RequestParam Long huddleId,
            @RequestParam BigDecimal score) {
        UserProgressResponse response = progressService.recordAssessmentAttempt(userId, huddleId, score);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/feedback")
    public ResponseEntity<UserProgressResponse> addFeedback(
            @RequestParam Long userId,
            @RequestParam Long huddleId,
            @RequestParam String feedback) {
        UserProgressResponse response = progressService.addFeedback(userId, huddleId, feedback);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserProgressResponse>> getUserProgress(@PathVariable Long userId) {
        List<UserProgressResponse> progress = progressService.getAllUserProgress(userId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/user/{userId}/sequence/{sequenceId}")
    public ResponseEntity<List<UserProgressResponse>> getUserSequenceProgress(
            @PathVariable Long userId,
            @PathVariable Long sequenceId) {
        List<UserProgressResponse> progress = progressService.getUserProgressBySequence(userId, sequenceId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/huddle/{huddleId}")
    public ResponseEntity<List<UserProgressResponse>> getHuddleProgress(@PathVariable Long huddleId) {
        List<UserProgressResponse> progress = progressService.getHuddleProgress(huddleId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/user/{userId}/huddle/{huddleId}")
    public ResponseEntity<UserProgressResponse> getSpecificProgress(
            @PathVariable Long userId,
            @PathVariable Long huddleId) {
        Optional<UserProgressResponse> progress = progressService.getProgressByUserAndHuddle(userId, huddleId);
        return progress.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}