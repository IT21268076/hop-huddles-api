package com.hqc.hophuddles.controller;

import com.hqc.hophuddles.dto.response.AnalyticsResponse;
import com.hqc.hophuddles.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/analytics")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/agency/{agencyId}")
    public ResponseEntity<AnalyticsResponse> getAgencyAnalytics(@PathVariable Long agencyId) {
        AnalyticsResponse analytics = analyticsService.getAgencyAnalytics(agencyId);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/agency/{agencyId}/since")
    public ResponseEntity<AnalyticsResponse> getAgencyAnalyticsSince(
            @PathVariable Long agencyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        AnalyticsResponse analytics = analyticsService.getAgencyAnalyticsSince(agencyId, since);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/sequence/{sequenceId}")
    public ResponseEntity<AnalyticsResponse> getSequenceAnalytics(@PathVariable Long sequenceId) {
        AnalyticsResponse analytics = analyticsService.getSequenceAnalytics(sequenceId);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<AnalyticsResponse> getUserAnalytics(@PathVariable Long userId) {
        AnalyticsResponse analytics = analyticsService.getUserAnalytics(userId);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/huddle/{huddleId}")
    public ResponseEntity<AnalyticsResponse> getHuddleAnalytics(@PathVariable Long huddleId) {
        AnalyticsResponse analytics = analyticsService.getHuddleAnalytics(huddleId);
        return ResponseEntity.ok(analytics);
    }
}