package com.rit.spms.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepartmentAchievementSummary {
    private String assessmentPeriodName;
    private String departmentName;
    private long achievementCount;
}
