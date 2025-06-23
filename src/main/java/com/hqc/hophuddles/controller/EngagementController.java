package com.hqc.hophuddles.controller;

import com.hqc.hophuddles.entity.EngagementEvent;
import com.hqc.hophuddles.enums.EventType;
import com.hqc.hophuddles.service.EngagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/engagement")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class EngagementController {

    private final EngagementService engagementService;

    @PostMapping("/huddle")
    public ResponseEntity<Void> recordHuddleEvent(
            @RequestParam Long userId,
            @RequestParam Long huddleId,
            @RequestParam EventType eventType,
            @RequestParam String sessionId,
            @RequestBody(required = false) String eventData) {
        engagementService.recordHuddleEvent(userId, huddleId, eventType, sessionId, eventData);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sequence")
    public ResponseEntity<Void> recordSequenceEvent(
            @RequestParam Long userId,
            @RequestParam Long sequenceId,
            @RequestParam EventType eventType,
            @RequestParam String sessionId,
            @RequestBody(required = false) String eventData) {
        engagementService.recordSequenceEvent(userId, sequenceId, eventType, sessionId, eventData);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EngagementEvent>> getUserEngagement(@PathVariable Long userId) {
        List<EngagementEvent> events = engagementService.getUserEngagementHistory(userId);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/huddle/{huddleId}")
    public ResponseEntity<List<EngagementEvent>> getHuddleEngagement(@PathVariable Long huddleId) {
        List<EngagementEvent> events = engagementService.getHuddleEngagementHistory(huddleId);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/sequence/{sequenceId}")
    public ResponseEntity<List<EngagementEvent>> getSequenceEngagement(@PathVariable Long sequenceId) {
        List<EngagementEvent> events = engagementService.getSequenceEngagementHistory(sequenceId);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<EngagementEvent>> getSessionEvents(@PathVariable String sessionId) {
        List<EngagementEvent> events = engagementService.getSessionEvents(sessionId);
        return ResponseEntity.ok(events);
    }
}