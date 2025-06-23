package com.hqc.hophuddles.dto.response;

import com.hqc.hophuddles.enums.ProgressStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProgressResponse {
    private Long progressId;
    private Long userId;
    private String userName;
    private Long huddleId;
    private String huddleTitle;
    private Long sequenceId;
    private String sequenceTitle;
    private ProgressStatus progressStatus;
    private BigDecimal completionPercentage;
    private BigDecimal timeSpentMinutes;
    private BigDecimal assessmentScore;
    private Integer assessmentAttempts;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime lastAccessed;
    private String feedback;
}