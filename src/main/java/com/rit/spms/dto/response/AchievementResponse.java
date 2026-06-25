package com.rit.spms.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AchievementResponse {
    private Long id;
    private Long measurementId;
    private String title;
    private Long achievementTypeId;
    private String achievementTypeName;
    private String details;
    private Long authorId;
    private String authorName;
    private Long assessmentPeriodId;
    private String assessmentPeriodName;
    private LocalDateTime recordedAt;
    private LocalDateTime updatedAt;
}
