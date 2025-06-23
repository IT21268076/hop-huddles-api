// SequenceTargetResponse.java
package com.hqc.hophuddles.dto.response;

import com.hqc.hophuddles.enums.TargetType;

public class SequenceTargetResponse {
    private Long targetId;
    private TargetType targetType;
    private String targetValue;
    private String targetDisplayName; // Human-readable name

    // Constructors
    public SequenceTargetResponse() {}

    public SequenceTargetResponse(TargetType targetType, String targetValue) {
        this.targetType = targetType;
        this.targetValue = targetValue;
    }

    // Getters and Setters
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }

    public TargetType getTargetType() { return targetType; }
    public void setTargetType(TargetType targetType) { this.targetType = targetType; }

    public String getTargetValue() { return targetValue; }
    public void setTargetValue(String targetValue) { this.targetValue = targetValue; }

    public String getTargetDisplayName() { return targetDisplayName; }
    public void setTargetDisplayName(String targetDisplayName) { this.targetDisplayName = targetDisplayName; }
}