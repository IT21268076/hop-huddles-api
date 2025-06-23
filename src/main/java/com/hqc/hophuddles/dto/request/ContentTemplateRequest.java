// ContentTemplateRequest.java
package com.hqc.hophuddles.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentTemplateRequest {

    @NotBlank(message = "Template name is required")
    @Size(max = 100, message = "Template name must not exceed 100 characters")
    private String templateName;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotBlank(message = "Template structure is required")
    private String structure; // JSON defining template structure

    @NotBlank(message = "Template type is required")
    private String templateType; // "STANDARD", "ASSESSMENT", "DISCUSSION", "CLINICAL_SKILLS"

    private String targetDisciplines; // JSON array of target disciplines

    private Integer defaultDuration;

    private String category; // "PATIENT_SAFETY", "QUALITY_IMPROVEMENT", "CLINICAL_SKILLS", etc.
}