package com.hqc.hophuddles.dto.request;

import com.hqc.hophuddles.enums.Discipline;
import com.hqc.hophuddles.enums.UserRole;
import com.hqc.hophuddles.enums.AgencyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIGenerationRequest {

    @NotNull(message = "Sequence ID is required")
    private Long sequenceId;

    @NotBlank(message = "Topic is required")
    private String topic;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @Min(value = 1, message = "Number of huddles must be at least 1")
    @Max(value = 20, message = "Number of huddles cannot exceed 20")
    private Integer numberOfHuddles;

    @Min(value = 5, message = "Duration must be at least 5 minutes")
    @Max(value = 60, message = "Duration cannot exceed 60 minutes")
    private Integer estimatedDurationMinutes;

    // Target audience
    private List<Discipline> targetDisciplines;
    private List<UserRole> targetRoles;
    private AgencyType agencyType;

    // Content customization
    private String difficultyLevel; // BEGINNER, INTERMEDIATE, ADVANCED
    private String contentStyle; // FORMAL, CONVERSATIONAL, TECHNICAL
    private Boolean includeAssessments;
    private Boolean includeInteractivements;

    // Context data
    private Map<String, Object> contextData; // Agency-specific data, compliance requirements, etc.

    // AI Model parameters
    private String modelVersion;
    private Float creativityLevel; // 0.0 to 1.0
    private Integer maxTokens;

    // File generation preferences
    private Boolean generatePDF;
    private Boolean generateAudio;
    private String voiceStyle; // PROFESSIONAL, FRIENDLY, AUTHORITATIVE

    // Additional metadata
    private Map<String, String> additionalInstructions;
}