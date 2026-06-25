package com.rit.spms.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class GoalResponse {
    private Long id;
    private Long strategyId;
    private Long themeId;
    private String themeName;
    private Long areaId;
    private String areaName;
    private String title;
    private String description;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ObjectiveResponse> objectives;
}
