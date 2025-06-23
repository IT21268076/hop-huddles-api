// HuddleSequenceCreateRequest.java
package com.hqc.hophuddles.dto.request;

import com.hqc.hophuddles.enums.TargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class HuddleSequenceCreateRequest {

    @NotNull(message = "Agency ID is required")
    private Long agencyId;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Size(max = 1000, message = "Topic must not exceed 1000 characters")
    private String topic;

    private Integer estimatedDurationMinutes;

    private String generationPrompt;

    // Targeting information
    private List<TargetRequest> targets;

    // Constructors
    public HuddleSequenceCreateRequest() {}

    // Getters and Setters
    public Long getAgencyId() { return agencyId; }
    public void setAgencyId(Long agencyId) { this.agencyId = agencyId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public Integer getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }

    public String getGenerationPrompt() { return generationPrompt; }
    public void setGenerationPrompt(String generationPrompt) { this.generationPrompt = generationPrompt; }

    public List<TargetRequest> getTargets() { return targets; }
    public void setTargets(List<TargetRequest> targets) { this.targets = targets; }

    // Nested class for targets
    public static class TargetRequest {
        private TargetType targetType;
        private String targetValue;

        public TargetRequest() {}

        public TargetRequest(TargetType targetType, String targetValue) {
            this.targetType = targetType;
            this.targetValue = targetValue;
        }

        public TargetType getTargetType() { return targetType; }
        public void setTargetType(TargetType targetType) { this.targetType = targetType; }

        public String getTargetValue() { return targetValue; }
        public void setTargetValue(String targetValue) { this.targetValue = targetValue; }
    }
}