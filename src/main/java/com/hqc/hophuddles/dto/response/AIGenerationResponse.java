package com.hqc.hophuddles.dto.response;

import com.hqc.hophuddles.enums.GenerationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIGenerationResponse {

    private String jobId;
    private Long sequenceId;
    private GenerationStatus status;
    private String message;

    // Generated content
    private List<GeneratedHuddle> generatedHuddles;

    // Generated files
    private List<GeneratedFile> generatedFiles;

    // Metadata
    private AIGenerationMetadata metadata;

    // Error information
    private String errorMessage;
    private String errorCode;
    private Long timestamp;
}