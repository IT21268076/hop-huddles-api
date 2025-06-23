package com.hqc.hophuddles.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressUpdateRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Huddle ID is required")
    private Long huddleId;

    @DecimalMin(value = "0.00", message = "Completion percentage must be at least 0")
    @DecimalMax(value = "100.00", message = "Completion percentage must not exceed 100")
    private BigDecimal completionPercentage;

    @DecimalMin(value = "0.00", message = "Time spent must be non-negative")
    private BigDecimal timeSpentMinutes;

    @DecimalMin(value = "0.00", message = "Assessment score must be at least 0")
    @DecimalMax(value = "100.00", message = "Assessment score must not exceed 100")
    private BigDecimal assessmentScore;

    private String feedback;

    private String sessionId;
}