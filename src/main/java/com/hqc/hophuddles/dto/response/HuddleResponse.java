// HuddleResponse.java
package com.hqc.hophuddles.dto.response;

import com.hqc.hophuddles.enums.HuddleType;

import java.time.LocalDateTime;

public class HuddleResponse {
    private Long huddleId;
    private Long sequenceId;
    private String sequenceTitle;
    private String title;
    private Integer orderIndex;
    private String contentJson;
    private String voiceScript;
    private String pdfUrl;
    private String audioUrl;
    private Integer durationMinutes;
    private HuddleType huddleType;
    private boolean isComplete;
    private LocalDateTime createdAt;

    // Constructors
    public HuddleResponse() {}

    // Getters and Setters
    public Long getHuddleId() { return huddleId; }
    public void setHuddleId(Long huddleId) { this.huddleId = huddleId; }

    public Long getSequenceId() { return sequenceId; }
    public void setSequenceId(Long sequenceId) { this.sequenceId = sequenceId; }

    public String getSequenceTitle() { return sequenceTitle; }
    public void setSequenceTitle(String sequenceTitle) { this.sequenceTitle = sequenceTitle; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

    public String getContentJson() { return contentJson; }
    public void setContentJson(String contentJson) { this.contentJson = contentJson; }

    public String getVoiceScript() { return voiceScript; }
    public void setVoiceScript(String voiceScript) { this.voiceScript = voiceScript; }

    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }

    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public HuddleType getHuddleType() { return huddleType; }
    public void setHuddleType(HuddleType huddleType) { this.huddleType = huddleType; }

    public boolean isComplete() { return isComplete; }
    public void setComplete(boolean complete) { isComplete = complete; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}