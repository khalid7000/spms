package com.rit.spms.dto.response;

import com.rit.spms.domain.enums.StrategyState;
import com.rit.spms.domain.enums.StrategyType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class StrategyResponse {
    private Long id;
    private Long planningCycleId;
    private String planningCycleName;
    private Long departmentId;
    private String departmentName;
    private StrategyType strategyType;
    private StrategyState state;
    private String title;
    private String description;
    private Integer achievementThreshold;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<VisionAreaResponse> areas;
    private List<GoalResponse> goals;
}
