package com.hqc.hophuddles.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsResponse {
    private Long agencyId;
    private Long sequenceId;
    private Long huddleId;
    private Long userId;
    private String period;
    private LocalDateTime generatedAt;
    private Map<String, Object> metrics;
}