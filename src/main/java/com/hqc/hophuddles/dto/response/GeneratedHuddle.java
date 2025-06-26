package com.hqc.hophuddles.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneratedHuddle {

    private Integer orderIndex;
    private String title;
    private String huddleType; // INTRO, STANDARD, ASSESSMENT, SUMMARY
    private Integer estimatedDuration;

    // Content sections
    private String introduction;
    private String mainContent;
    private String summary;
    private String keyTakeaways;

    // Voice script (optimized for TTS)
    private String voiceScript;

    // Assessment questions (if included)
    private List<AssessmentQuestion> assessmentQuestions;

    // Interactive elements
    private List<InteractiveElement> interactiveElements;

    // Metadata
    private Map<String, Object> metadata;
}