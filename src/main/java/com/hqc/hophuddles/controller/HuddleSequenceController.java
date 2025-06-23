package com.hqc.hophuddles.controller;

import com.hqc.hophuddles.dto.request.HuddleSequenceCreateRequest;
import com.hqc.hophuddles.dto.response.HuddleSequenceResponse;
import com.hqc.hophuddles.enums.SequenceStatus;
import com.hqc.hophuddles.service.HuddleSequenceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sequences")
@CrossOrigin(origins = "*")
public class HuddleSequenceController {

    @Autowired
    private HuddleSequenceService sequenceService;

    @PostMapping
    public ResponseEntity<HuddleSequenceResponse> createSequence(
            @Valid @RequestBody HuddleSequenceCreateRequest request,
            @RequestParam Long createdByUserId) {
        HuddleSequenceResponse response = sequenceService.createSequence(request, createdByUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{sequenceId}")
    public ResponseEntity<HuddleSequenceResponse> getSequence(@PathVariable Long sequenceId) {
        HuddleSequenceResponse response = sequenceService.getSequenceById(sequenceId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/agency/{agencyId}")
    public ResponseEntity<List<HuddleSequenceResponse>> getSequencesByAgency(@PathVariable Long agencyId) {
        List<HuddleSequenceResponse> sequences = sequenceService.getSequencesByAgency(agencyId);
        return ResponseEntity.ok(sequences);
    }

    @GetMapping("/agency/{agencyId}/status/{status}")
    public ResponseEntity<List<HuddleSequenceResponse>> getSequencesByAgencyAndStatus(
            @PathVariable Long agencyId,
            @PathVariable SequenceStatus status) {
        List<HuddleSequenceResponse> sequences = sequenceService.getSequencesByAgencyAndStatus(agencyId, status);
        return ResponseEntity.ok(sequences);
    }

    @GetMapping("/creator/{userId}")
    public ResponseEntity<List<HuddleSequenceResponse>> getSequencesByCreator(@PathVariable Long userId) {
        List<HuddleSequenceResponse> sequences = sequenceService.getSequencesByCreator(userId);
        return ResponseEntity.ok(sequences);
    }

    @GetMapping("/agency/{agencyId}/search")
    public ResponseEntity<Page<HuddleSequenceResponse>> searchSequences(
            @PathVariable Long agencyId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) SequenceStatus status,
            Pageable pageable) {
        Page<HuddleSequenceResponse> sequences = sequenceService.searchSequences(agencyId, title, status, pageable);
        return ResponseEntity.ok(sequences);
    }

    @PutMapping("/{sequenceId}")
    public ResponseEntity<HuddleSequenceResponse> updateSequence(
            @PathVariable Long sequenceId,
            @Valid @RequestBody HuddleSequenceCreateRequest request) {
        HuddleSequenceResponse response = sequenceService.updateSequence(sequenceId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{sequenceId}/status")
    public ResponseEntity<HuddleSequenceResponse> updateSequenceStatus(
            @PathVariable Long sequenceId,
            @RequestParam SequenceStatus status,
            @RequestParam Long updatedByUserId) {
        HuddleSequenceResponse response = sequenceService.updateSequenceStatus(sequenceId, status, updatedByUserId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sequenceId}/publish")
    public ResponseEntity<HuddleSequenceResponse> publishSequence(
            @PathVariable Long sequenceId,
            @RequestParam Long publishedByUserId) {
        HuddleSequenceResponse response = sequenceService.updateSequenceStatus(sequenceId, SequenceStatus.PUBLISHED, publishedByUserId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sequenceId}/archive")
    public ResponseEntity<HuddleSequenceResponse> archiveSequence(
            @PathVariable Long sequenceId,
            @RequestParam Long updatedByUserId) {
        HuddleSequenceResponse response = sequenceService.updateSequenceStatus(sequenceId, SequenceStatus.ARCHIVED, updatedByUserId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{sequenceId}")
    public ResponseEntity<Void> deleteSequence(@PathVariable Long sequenceId) {
        sequenceService.deleteSequence(sequenceId);
        return ResponseEntity.noContent().build();
    }
}