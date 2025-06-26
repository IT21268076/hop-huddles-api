package com.hqc.hophuddles.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIGenerationMetadata {

    private String modelUsed;
    private Float processingTimeSeconds;
    private Integer tokensUsed;
    private String qualityScore;
    private List<String> tags;
    private Map<String, Object> generationParams;
}
