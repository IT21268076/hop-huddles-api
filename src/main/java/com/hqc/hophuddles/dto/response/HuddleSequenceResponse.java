// HuddleSequenceResponse.java
package com.hqc.hophuddles.dto.response;

import com.hqc.hophuddles.enums.SequenceStatus;

import java.time.LocalDateTime;
import java.util.List;

public class HuddleSequenceResponse {
    private Long sequenceId;
    private Long agencyId;
    private String agencyName;
    private String title;
    private String description;
    private String topic;
    private Integer totalHuddles;
    private Integer estimatedDurationMinutes;
    private SequenceStatus sequenceStatus;
    private String generationPrompt;
    private Long createdByUserId;
    private String createdByUserName;
    private Long publishedByUserId;
    private String publishedByUserName;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private List<HuddleResponse> huddles;
    private List<SequenceTargetResponse> targets;

    // Constructors
    public HuddleSequenceResponse() {}

    // Getters and Setters
    public Long getSequenceId() { return sequenceId; }
    public void setSequenceId(Long sequenceId) { this.sequenceId = sequenceId; }

    public Long getAgencyId() { return agencyId; }
    public void setAgencyId(Long agencyId) { this.agencyId = agencyId; }

    public String getAgencyName() { return agencyName; }
    public void setAgencyName(String agencyName) { this.agencyName = agencyName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public Integer getTotalHuddles() { return totalHuddles; }
    public void setTotalHuddles(Integer totalHuddles) { this.totalHuddles = totalHuddles; }

    public Integer getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }

    public SequenceStatus getSequenceStatus() { return sequenceStatus; }
    public void setSequenceStatus(SequenceStatus sequenceStatus) { this.sequenceStatus = sequenceStatus; }

    public String getGenerationPrompt() { return generationPrompt; }
    public void setGenerationPrompt(String generationPrompt) { this.generationPrompt = generationPrompt; }

    public Long getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Long createdByUserId) { this.createdByUserId = createdByUserId; }

    public String getCreatedByUserName() { return createdByUserName; }
    public void setCreatedByUserName(String createdByUserName) { this.createdByUserName = createdByUserName; }

    public Long getPublishedByUserId() { return publishedByUserId; }
    public void setPublishedByUserId(Long publishedByUserId) { this.publishedByUserId = publishedByUserId; }

    public String getPublishedByUserName() { return publishedByUserName; }
    public void setPublishedByUserName(String publishedByUserName) { this.publishedByUserName = publishedByUserName; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<HuddleResponse> getHuddles() { return huddles; }
    public void setHuddles(List<HuddleResponse> huddles) { this.huddles = huddles; }

    public List<SequenceTargetResponse> getTargets() { return targets; }
    public void setTargets(List<SequenceTargetResponse> targets) { this.targets = targets; }
}