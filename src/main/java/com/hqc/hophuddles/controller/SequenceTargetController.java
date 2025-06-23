package com.hqc.hophuddles.controller;

import com.hqc.hophuddles.dto.response.SequenceTargetResponse;
import com.hqc.hophuddles.enums.TargetType;
import com.hqc.hophuddles.service.SequenceTargetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sequence-targets")
@CrossOrigin(origins = "*")
public class SequenceTargetController {

    @Autowired
    private SequenceTargetService targetService;

    @PostMapping
    public ResponseEntity<SequenceTargetResponse> addTarget(
            @RequestParam Long sequenceId,
            @RequestParam TargetType targetType,
            @RequestParam String targetValue) {
        SequenceTargetResponse response = targetService.addTarget(sequenceId, targetType, targetValue);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/sequence/{sequenceId}")
    public ResponseEntity<List<SequenceTargetResponse>> getTargetsBySequence(@PathVariable Long sequenceId) {
        List<SequenceTargetResponse> targets = targetService.getTargetsBySequence(sequenceId);
        return ResponseEntity.ok(targets);
    }

    @GetMapping("/sequence/{sequenceId}/type/{targetType}")
    public ResponseEntity<List<SequenceTargetResponse>> getTargetsBySequenceAndType(
            @PathVariable Long sequenceId,
            @PathVariable TargetType targetType) {
        List<SequenceTargetResponse> targets = targetService.getTargetsBySequenceAndType(sequenceId, targetType);
        return ResponseEntity.ok(targets);
    }

    @GetMapping("/find-sequences")
    public ResponseEntity<List<Long>> findSequencesForTarget(
            @RequestParam TargetType targetType,
            @RequestParam String targetValue) {
        List<Long> sequenceIds = targetService.findSequencesForTarget(targetType, targetValue);
        return ResponseEntity.ok(sequenceIds);
    }

    @DeleteMapping("/{targetId}")
    public ResponseEntity<Void> removeTarget(@PathVariable Long targetId) {
        targetService.removeTarget(targetId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/sequence/{sequenceId}")
    public ResponseEntity<Void> removeTargetByValues(
            @PathVariable Long sequenceId,
            @RequestParam TargetType targetType,
            @RequestParam String targetValue) {
        targetService.removeTargetByValues(sequenceId, targetType, targetValue);
        return ResponseEntity.noContent().build();
    }
}