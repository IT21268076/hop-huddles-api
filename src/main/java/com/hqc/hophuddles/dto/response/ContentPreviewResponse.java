// ContentPreviewResponse.java
package com.hqc.hophuddles.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentPreviewResponse {

    private Long huddleId;
    private String title;
    private String objective;
    private String keyContent;
    private List<String> actionItems;
    private List<String> discussionPoints;
    private List<String> resources;
    private Integer estimatedMinutes;
    private Integer wordCount;
    private String readingLevel;
    private List<String> contentTags;

    // Preview metadata
    private String formattedContent; // HTML formatted for display
    private String plainTextContent; // Plain text version
    private String voiceScript;
    private ContentValidationResult validation;
    private LocalDateTime lastUpdated;
    private String contentType;

    // SEO and accessibility info
    private String metaDescription;
    private List<String> keywords;
    private String accessibilityScore;
}