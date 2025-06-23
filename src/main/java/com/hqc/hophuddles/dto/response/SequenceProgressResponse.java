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
public class SequenceProgressResponse {
    private Long sequenceProgressId;
    private Long userId;
    private String userName;
    private Long sequenceId;
    private String sequenceTitle;
    private Long agencyId;
    private String agencyName;
    private Integer totalHuddles;
    private Integer completedHuddles;
    private BigDecimal completionPercentage;
    private BigDecimal totalTimeSpentMinutes;
    private BigDecimal averageScore;
    private ProgressStatus sequenceStatus;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime lastAccessed;
}