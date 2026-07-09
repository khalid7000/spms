package com.rit.spms.dto.response;

import com.rit.spms.domain.enums.RoleType;
import com.rit.spms.domain.enums.StrategyState;
import com.rit.spms.domain.enums.StrategyType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardResponse {
    private Long strategyId;
    private String strategyTitle;
    private StrategyType strategyType;
    private StrategyState state;
    private RoleType role;
    private String planningCycleName;
    private String departmentName;
    private int involvedUserCount;
    private int unreadNotificationCount;
    private int totalGoals;
    private int goalsOnTrack;
    private int totalObjectives;
    private int objectivesOnTrack;
    private String mostRecentPeriodName;
}
