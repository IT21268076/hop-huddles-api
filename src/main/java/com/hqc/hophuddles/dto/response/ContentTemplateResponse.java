// ContentTemplateResponse.java
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
public class ContentTemplateResponse {

    private Long templateId;
    private String templateName;
    private String description;
    private String structure; // JSON structure
    private String templateType;
    private List<String> targetDisciplines;
    private Integer defaultDuration;
    private String category;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Usage statistics
    private Integer usageCount;
    private LocalDateTime lastUsed;
    private Double averageRating;

    // Template preview
    private String previewHtml;
    private List<String> sectionNames;
}