package com.hqc.hophuddles.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManualContentRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Learning objective is required")
    @Size(max = 500, message = "Objective must not exceed 500 characters")
    private String objective;

    @NotBlank(message = "Key content is required")
    @Size(max = 5000, message = "Content must not exceed 5000 characters")
    private String keyContent;

    private List<String> actionItems;

    private List<String> discussionPoints;

    private List<String> resources;

    @Size(max = 2000, message = "Voice script must not exceed 2000 characters")
    private String voiceScript;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 60, message = "Duration should not exceed 60 minutes")
    private Integer estimatedMinutes;

    private Long templateId; // Optional: use template

    private String readingLevel; // "BASIC", "INTERMEDIATE", "ADVANCED"

    private List<String> contentTags;

    private Boolean publishImmediately = false;
}