package com.hqc.hophuddles.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hqc.hophuddles.dto.request.*;
import com.hqc.hophuddles.dto.response.*;
import com.hqc.hophuddles.entity.*;
import com.hqc.hophuddles.enums.SequenceStatus;
import com.hqc.hophuddles.exception.ResourceNotFoundException;
import com.hqc.hophuddles.exception.ValidationException;
import com.hqc.hophuddles.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentManagementService {

    private final HuddleRepository huddleRepository;
    private final HuddleSequenceRepository sequenceRepository;
    private final ObjectMapper objectMapper;

    /**
     * Create manual content for an existing huddle
     */
    @Transactional
    public Huddle createManualContent(Long huddleId, ManualContentRequest request) {
        log.info("Creating manual content for huddle: {}", huddleId);

        Huddle huddle = huddleRepository.findById(huddleId)
                .orElseThrow(() -> new ResourceNotFoundException("Huddle not found: " + huddleId));

        // Validate request
        ContentValidationResult validation = validateContentRequest(request);
        if (validation.hasErrors()) {
            throw new ValidationException("Content validation failed", validation.getErrors());
        }

        // Apply template if specified
        if (request.getTemplateId() != null) {
            applyContentTemplate(huddle, request.getTemplateId());
        }

        // Set manual content
        updateHuddleWithContent(huddle, request);

        // Set metadata
        huddle.setContentType("MANUAL");
        huddle.setTemplateId(request.getTemplateId());
        huddle.setUpdatedAt(LocalDateTime.now());

        HuddleSequence huddleSequence = new HuddleSequence();
        // Update status based on publish flag
        if (Boolean.TRUE.equals(request.getPublishImmediately()) && huddle.canPublish()) {
            huddleSequence.setSequenceStatus(SequenceStatus.PUBLISHED);
        } else {
            huddleSequence.setSequenceStatus(SequenceStatus.DRAFT);
        }

        Huddle savedHuddle = huddleRepository.save(huddle);
        log.info("Successfully created manual content for huddle: {}", huddleId);

        return savedHuddle;
    }

    /**
     * Bulk create huddles with manual content
     */
    @Transactional
    public List<Huddle> bulkCreateHuddles(Long sequenceId, BulkHuddleCreationRequest request) {
        log.info("Bulk creating {} huddles for sequence: {}", request.getHuddles().size(), sequenceId);

        HuddleSequence sequence = sequenceRepository.findById(sequenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Sequence not found: " + sequenceId));

        List<Huddle> createdHuddles = new ArrayList<>();

        for (BulkHuddleCreationRequest.BulkHuddleItem item : request.getHuddles()) {
            // Check if huddle with this order index already exists
            Optional<Huddle> existingHuddle = huddleRepository
                    .findBySequenceAndOrderIndex(sequenceId, item.getOrderIndex());

            Huddle huddle;
            if (existingHuddle.isPresent() && Boolean.TRUE.equals(request.getOverwriteExisting())) {
                huddle = existingHuddle.get();
                log.info("Overwriting existing huddle at position: {}", item.getOrderIndex());
            } else if (existingHuddle.isPresent()) {
                throw new ValidationException("Huddle already exists at position " + item.getOrderIndex() +
                        ". Set overwriteExisting=true to replace it.");
            } else {
                // Create new huddle
                huddle = new Huddle();
                huddle.setSequence(sequence);
                huddle.setOrderIndex(item.getOrderIndex());
                huddle.setCreatedAt(LocalDateTime.now());
                huddle.setIsActive(true);
                huddle = huddleRepository.save(huddle); // Save to get ID
            }

            // Apply content
            updateHuddleWithContent(huddle, item.getContent());
            huddle.setContentType("MANUAL");
            huddle.setTemplateId(request.getTemplateId());

            // Set status
            if (Boolean.TRUE.equals(request.getPublishImmediately()) && huddle.canPublish()) {
                HuddleSequence huddleSequence = new HuddleSequence();
                huddleSequence.setSequenceStatus(SequenceStatus.PUBLISHED);
            } else {
                HuddleSequence huddleSequence = new HuddleSequence();
                huddleSequence.setSequenceStatus(SequenceStatus.DRAFT);
            }

            Huddle savedHuddle = huddleRepository.save(huddle);
            createdHuddles.add(savedHuddle);
        }

        // Update sequence metadata
        updateSequenceMetadata(sequence);

        log.info("Successfully created {} huddles for sequence: {}", createdHuddles.size(), sequenceId);
        return createdHuddles;
    }

    /**
     * Preview huddle content
     */
    public ContentPreviewResponse previewContent(Long huddleId) {
        Huddle huddle = huddleRepository.findById(huddleId)
                .orElseThrow(() -> new ResourceNotFoundException("Huddle not found: " + huddleId));

        ContentValidationResult validation = huddle.validateContent();

        return ContentPreviewResponse.builder()
                .huddleId(huddle.getHuddleId())
                .title(huddle.getTitle())
                .objective(huddle.getObjective())
                .keyContent(huddle.getKeyContent())
                .actionItems(parseJsonList(huddle.getActionItems()))
                .discussionPoints(parseJsonList(huddle.getDiscussionPoints()))
                .resources(parseJsonList(huddle.getResources()))
                .estimatedMinutes(huddle.getEstimatedMinutes())
                .wordCount(huddle.getWordCount())
                .readingLevel(huddle.getReadingLevel())
                .contentTags(parseJsonList(huddle.getContentTags()))
                .formattedContent(formatContentForDisplay(huddle))
                .plainTextContent(stripHtmlTags(huddle.getKeyContent()))
                .voiceScript(huddle.getVoiceScript())
                .validation(validation)
                .lastUpdated(huddle.getUpdatedAt())
                .contentType(huddle.getContentType())
                .build();
    }

    /**
     * Validate huddle content
     */
    public ContentValidationResult validateContent(Long huddleId) {
        Huddle huddle = huddleRepository.findById(huddleId)
                .orElseThrow(() -> new ResourceNotFoundException("Huddle not found: " + huddleId));

        return huddle.validateContent();
    }

    /**
     * Duplicate existing huddle
     */
    @Transactional
    public Huddle duplicateHuddle(Long huddleId, Long targetSequenceId, Integer newOrderIndex) {
        Huddle sourceHuddle = huddleRepository.findById(huddleId)
                .orElseThrow(() -> new ResourceNotFoundException("Source huddle not found: " + huddleId));

        HuddleSequence targetSequence = sequenceRepository.findById(targetSequenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Target sequence not found: " + targetSequenceId));

        Huddle duplicatedHuddle = new Huddle();

        // Copy all content fields
        duplicatedHuddle.setSequence(targetSequence);
        duplicatedHuddle.setTitle(sourceHuddle.getTitle() + " (Copy)");
        duplicatedHuddle.setObjective(sourceHuddle.getObjective());
        duplicatedHuddle.setKeyContent(sourceHuddle.getKeyContent());
        duplicatedHuddle.setActionItems(sourceHuddle.getActionItems());
        duplicatedHuddle.setDiscussionPoints(sourceHuddle.getDiscussionPoints());
        duplicatedHuddle.setResources(sourceHuddle.getResources());
        duplicatedHuddle.setVoiceScript(sourceHuddle.getVoiceScript());
        duplicatedHuddle.setEstimatedMinutes(sourceHuddle.getEstimatedMinutes());
        duplicatedHuddle.setContentType(sourceHuddle.getContentType());
        duplicatedHuddle.setTemplateId(sourceHuddle.getTemplateId());
        duplicatedHuddle.setWordCount(sourceHuddle.getWordCount());
        duplicatedHuddle.setReadingLevel(sourceHuddle.getReadingLevel());
        duplicatedHuddle.setContentTags(sourceHuddle.getContentTags());

        // Set new metadata
        duplicatedHuddle.setOrderIndex(newOrderIndex != null ? newOrderIndex : getNextOrderIndex(targetSequence));
        duplicatedHuddle.setHuddleStatus(HuddleStatus.DRAFT);
        duplicatedHuddle.setCreatedAt(LocalDateTime.now());
        duplicatedHuddle.setIsActive(true);

        Huddle savedHuddle = huddleRepository.save(duplicatedHuddle);
        updateSequenceMetadata(targetSequence);

        log.info("Duplicated huddle {} to sequence {} at position {}",
                huddleId, targetSequenceId, duplicatedHuddle.getOrderIndex());

        return savedHuddle;
    }

    // PRIVATE HELPER METHODS

    private void updateHuddleWithContent(Huddle huddle, ManualContentRequest request) {
        huddle.setTitle(request.getTitle());
        huddle.setObjective(request.getObjective());
        huddle.setKeyContent(request.getKeyContent());
        huddle.setActionItems(jsonify(request.getActionItems()));
        huddle.setDiscussionPoints(jsonify(request.getDiscussionPoints()));
        huddle.setResources(jsonify(request.getResources()));
        huddle.setVoiceScript(request.getVoiceScript());
        huddle.setEstimatedMinutes(request.getEstimatedMinutes());
        huddle.setReadingLevel(request.getReadingLevel());
        huddle.setContentTags(jsonify(request.getContentTags()));
        huddle.updateWordCount();
    }

    private void applyContentTemplate(Huddle huddle, Long templateId) {
        // Template application logic - placeholder for now
        // TODO: Implement when ContentTemplate entity is created
        log.info("Applying template {} to huddle {}", templateId, huddle.getHuddleId());
    }

    private ContentValidationResult validateContentRequest(ManualContentRequest request) {
        ContentValidationResult result = new ContentValidationResult();

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            result.addError("Title is required");
        }

        if (request.getObjective() == null || request.getObjective().trim().isEmpty()) {
            result.addError("Learning objective is required");
        }

        if (request.getKeyContent() == null || request.getKeyContent().trim().isEmpty()) {
            result.addError("Main content is required");
        } else {
            int wordCount = request.getKeyContent().trim().split("\\s+").length;
            if (wordCount < 25) {
                result.addWarning("Content may be too short (less than 25 words)");
            }
            if (wordCount > 750) {
                result.addWarning("Content may be too long for a huddle (more than 750 words)");
            }
        }

        if (request.getEstimatedMinutes() != null && request.getEstimatedMinutes() > 15) {
            result.addWarning("Huddles should typically be 15 minutes or less");
        }

        return result;
    }

    private String jsonify(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.warn("Failed to convert list to JSON: {}", e.getMessage());
            return "[]";
        }
    }

    private List<String> parseJsonList(String json) {
        if (json == null || json.trim().isEmpty() || "[]".equals(json.trim())) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse JSON list: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private String formatContentForDisplay(Huddle huddle) {
        StringBuilder formatted = new StringBuilder();

        if (huddle.getObjective() != null) {
            formatted.append("<div class='objective'><h3>Learning Objective</h3>")
                    .append("<p>").append(huddle.getObjective()).append("</p></div>");
        }

        if (huddle.getKeyContent() != null) {
            formatted.append("<div class='key-content'><h3>Key Content</h3>")
                    .append(huddle.getKeyContent()).append("</div>");
        }

        List<String> actionItems = parseJsonList(huddle.getActionItems());
        if (!actionItems.isEmpty()) {
            formatted.append("<div class='action-items'><h3>Action Items</h3><ul>");
            actionItems.forEach(item -> formatted.append("<li>").append(item).append("</li>"));
            formatted.append("</ul></div>");
        }

        List<String> discussionPoints = parseJsonList(huddle.getDiscussionPoints());
        if (!discussionPoints.isEmpty()) {
            formatted.append("<div class='discussion-points'><h3>Discussion Questions</h3><ul>");
            discussionPoints.forEach(point -> formatted.append("<li>").append(point).append("</li>"));
            formatted.append("</ul></div>");
        }

        return formatted.toString();
    }

    private String stripHtmlTags(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", "").trim();
    }

    private void updateSequenceMetadata(HuddleSequence sequence) {
        List<Huddle> huddles = huddleRepository.findBySequenceIdAndIsActiveTrueOrderByOrderIndex(sequence.getSequenceId());
        sequence.setTotalHuddles(huddles.size());

        // Calculate total estimated duration
        int totalMinutes = huddles.stream()
                .mapToInt(h -> h.getEstimatedMinutes() != null ? h.getEstimatedMinutes() : 0)
                .sum();
        sequence.setEstimatedDurationMinutes(totalMinutes);

        sequenceRepository.save(sequence);
    }

    private Integer getNextOrderIndex(HuddleSequence sequence) {
        return huddleRepository.findMaxOrderIndexBySequenceId(sequence.getSequenceId())
                .orElse(0) + 1;
    }
}