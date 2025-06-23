// HuddleCreateRequest.java
package com.hqc.hophuddles.dto.request;

import com.hqc.hophuddles.enums.HuddleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class HuddleCreateRequest {

    @NotNull(message = "Sequence ID is required")
    private Long sequenceId;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private Integer orderIndex; // If null, will be auto-generated

    private String contentJson;

    private String voiceScript;

    private Integer durationMinutes;

    private HuddleType huddleType = HuddleType.STANDARD;

    // Constructors
    public HuddleCreateRequest() {}

    // Getters and Setters
    public Long getSequenceId() { return sequenceId; }
    public void setSequenceId(Long sequenceId) { this.sequenceId = sequenceId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

    public String getContentJson() { return contentJson; }
    public void setContentJson(String contentJson) { this.contentJson = contentJson; }

    public String getVoiceScript() { return voiceScript; }
    public void setVoiceScript(String voiceScript) { this.voiceScript = voiceScript; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public HuddleType getHuddleType() { return huddleType; }
    public void setHuddleType(HuddleType huddleType) { this.huddleType = huddleType; }
}