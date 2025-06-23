package com.hqc.hophuddles.controller;

import com.hqc.hophuddles.dto.request.*;
import com.hqc.hophuddles.dto.response.*;
import com.hqc.hophuddles.entity.Huddle;
import com.hqc.hophuddles.service.ContentManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/content")
@RequiredArgsConstructor
public class ContentManagementController {

    private final ContentManagementService contentManagementService;

    /**
     * Create manual content for an existing huddle
     */
    @PostMapping("/huddles/{huddleId}/manual")
    public ResponseEntity<?> createManualContent(
            @PathVariable Long huddleId,
            @Valid @RequestBody ManualContentRequest request) {
        try {
            log.info("Creating manual content for huddle: {}", huddleId);

            Huddle huddle = contentManagementService.createManualContent(huddleId, request);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Manual content created successfully");
            response.put("huddleId", huddle.getHuddleId());
            response.put("title", huddle.getTitle());
            response.put("contentType", huddle.getContentType());
            response.put("huddleStatus", huddle.getHuddleStatus());
            response.put("wordCount", huddle.getWordCount());
            response.put("estimatedMinutes", huddle.getEstimatedMinutes());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to create manual content for huddle {}", huddleId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "status", "error",
                            "message", "Failed to create manual content: " + e.getMessage(),
                            "huddleId", huddleId
                    ));
        }
    }

    /**
     * Bulk create huddles with manual content
     */
    @PostMapping("/sequences/{sequenceId}/bulk-create")
    public ResponseEntity<?> bulkCreateHuddles(
            @PathVariable Long sequenceId,
            @Valid @RequestBody BulkHuddleCreationRequest request) {
        try {
            log.info("Bulk creating huddles for sequence: {}", sequenceId);

            List<Huddle> huddles = contentManagementService.bulkCreateHuddles(sequenceId, request);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Huddles created successfully");
            response.put("sequenceId", sequenceId);
            response.put("huddlesCreated", huddles.size());
            response.put("huddles", huddles.stream().map(h -> Map.of(
                    "huddleId", h.getHuddleId(),
                    "title", h.getTitle(),
                    "orderIndex", h.getOrderIndex(),
                    "status", h.getHuddleStatus(),
                    "wordCount", h.getWordCount() != null ? h.getWordCount() : 0
            )).toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to bulk create huddles for sequence {}", sequenceId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "status", "error",
                            "message", "Failed to create huddles: " + e.getMessage(),
                            "sequenceId", sequenceId
                    ));
        }
    }

    /**
     * Preview huddle content
     */
    @GetMapping("/huddles/{huddleId}/preview")
    public ResponseEntity<?> previewContent(@PathVariable Long huddleId) {
        try {
            ContentPreviewResponse preview = contentManagementService.previewContent(huddleId);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", preview
            ));

        } catch (Exception e) {
            log.error("Failed to preview content for huddle {}", huddleId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "status", "error",
                            "message", "Failed to preview content: " + e.getMessage(),
                            "huddleId", huddleId
                    ));
        }
    }

    /**
     * Validate huddle content
     */
    @PostMapping("/huddles/{huddleId}/validate")
    public ResponseEntity<?> validateContent(@PathVariable Long huddleId) {
        try {
            ContentValidationResult validation = contentManagementService.validateContent(huddleId);

            Map<String, Object> response = new HashMap<>();
            response.put("huddleId", huddleId);
            response.put("validation", validation);
            response.put("isValid", validation.isValid());
            response.put("overallStatus", validation.getOverallStatus());

            if (validation.hasErrors()) {
                response.put("status", "validation_failed");
                response.put("message", "Content validation failed");
                return ResponseEntity.badRequest().body(response);
            } else {
                response.put("status", "valid");
                response.put("message", validation.hasWarnings() ?
                        "Content is valid but has warnings" : "Content is valid");
                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            log.error("Failed to validate content for huddle {}", huddleId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "status", "error",
                            "message", "Failed to validate content: " + e.getMessage(),
                            "huddleId", huddleId
                    ));
        }
    }

    /**
     * Duplicate existing huddle
     */
    @PostMapping("/huddles/{huddleId}/duplicate")
    public ResponseEntity<?> duplicateHuddle(
            @PathVariable Long huddleId,
            @RequestParam Long targetSequenceId,
            @RequestParam(required = false) Integer newOrderIndex) {
        try {
            Huddle duplicatedHuddle = contentManagementService.duplicateHuddle(
                    huddleId, targetSequenceId, newOrderIndex);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Huddle duplicated successfully",
                    "originalHuddleId", huddleId,
                    "newHuddleId", duplicatedHuddle.getHuddleId(),
                    "newTitle", duplicatedHuddle.getTitle(),
                    "targetSequenceId", targetSequenceId,
                    "orderIndex", duplicatedHuddle.getOrderIndex()
            ));

        } catch (Exception e) {
            log.error("Failed to duplicate huddle {}", huddleId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "status", "error",
                            "message", "Failed to duplicate huddle: " + e.getMessage(),
                            "huddleId", huddleId
                    ));
        }
    }

    /**
     * Update existing huddle content
     */
    @PutMapping("/huddles/{huddleId}/manual")
    public ResponseEntity<?> updateManualContent(
            @PathVariable Long huddleId,
            @Valid @RequestBody ManualContentRequest request) {
        try {
            Huddle huddle = contentManagementService.createManualContent(huddleId, request);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Content updated successfully",
                    "huddleId", huddle.getHuddleId(),
                    "title", huddle.getTitle(),
                    "wordCount", huddle.getWordCount(),
                    "lastUpdated", huddle.getUpdatedAt()
            ));

        } catch (Exception e) {
            log.error("Failed to update content for huddle {}", huddleId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "status", "error",
                            "message", "Failed to update content: " + e.getMessage(),
                            "huddleId", huddleId
                    ));
        }
    }

    /**
     * Get content creation statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getContentStatistics(
            @RequestParam(required = false) Long agencyId,
            @RequestParam(required = false) Long sequenceId) {
        try {
            // This would implement content statistics
            Map<String, Object> stats = new HashMap<>();
            stats.put("status", "success");
            stats.put("message", "Content statistics (placeholder)");
            stats.put("totalManualContent", 0);
            stats.put("averageWordCount", 0);
            stats.put("contentByType", Map.of(
                    "MANUAL", 0,
                    "TEMPLATE", 0,
                    "AI_GENERATED", 0
            ));

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Failed to get content statistics", e);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "status", "error",
                            "message", "Failed to get statistics: " + e.getMessage()
                    ));
        }
    }

    /**
     * Get content creation help/guidelines
     */
    @GetMapping("/guidelines")
    public ResponseEntity<?> getContentGuidelines() {
        Map<String, Object> guidelines = new HashMap<>();

        guidelines.put("status", "success");
        guidelines.put("contentGuidelines", Map.of(
                "title", Map.of(
                        "maxLength", 200,
                        "guidelines", "Keep titles clear, specific, and action-oriented"
                ),
                "objective", Map.of(
                        "maxLength", 500,
                        "guidelines", "State what learners will know or be able to do after the huddle"
                ),
                "keyContent", Map.of(
                        "minWords", 25,
                        "maxWords", 750,
                        "guidelines", "Focus on essential information that can be covered in 5-15 minutes"
                ),
                "actionItems", Map.of(
                        "recommended", "2-5 items",
                        "guidelines", "Provide specific, actionable steps staff can take"
                ),
                "estimatedMinutes", Map.of(
                        "recommended", "5-15 minutes",
                        "guidelines", "Consider discussion time in addition to content delivery"
                )
        ));

        guidelines.put("bestPractices", List.of(
                "Use bullet points for easy scanning",
                "Include relevant evidence or guidelines",
                "Make content specific to your organization",
                "Include discussion questions to engage participants",
                "Add resources for follow-up learning"
        ));

        guidelines.put("contentTypes", Map.of(
                "PATIENT_SAFETY", "Focus on error prevention and safety protocols",
                "QUALITY_IMPROVEMENT", "Emphasize measurement and improvement",
                "CLINICAL_SKILLS", "Include hands-on practice opportunities",
                "REGULATORY", "Ensure compliance and understanding"
        ));

        return ResponseEntity.ok(guidelines);
    }
}