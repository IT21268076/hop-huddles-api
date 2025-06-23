package com.hqc.hophuddles.controller;

import com.hqc.hophuddles.dto.response.SequenceProgressResponse;
import com.hqc.hophuddles.service.SequenceProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sequence-progress")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SequenceProgressController {

    private final SequenceProgressService sequenceProgressService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SequenceProgressResponse>> getUserSequenceProgress(@PathVariable Long userId) {
        List<SequenceProgressResponse> progress = sequenceProgressService.getUserSequenceProgress(userId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/sequence/{sequenceId}")
    public ResponseEntity<List<SequenceProgressResponse>> getSequenceProgress(@PathVariable Long sequenceId) {
        List<SequenceProgressResponse> progress = sequenceProgressService.getSequenceProgressBySequence(sequenceId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/agency/{agencyId}")
    public ResponseEntity<List<SequenceProgressResponse>> getAgencyProgress(@PathVariable Long agencyId) {
        List<SequenceProgressResponse> progress = sequenceProgressService.getAgencySequenceProgress(agencyId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/user/{userId}/sequence/{sequenceId}")
    public ResponseEntity<SequenceProgressResponse> getSpecificSequenceProgress(
            @PathVariable Long userId,
            @PathVariable Long sequenceId) {
        SequenceProgressResponse progress = sequenceProgressService.getSequenceProgressByUserAndSequence(userId, sequenceId);
        return ResponseEntity.ok(progress);
    }
}