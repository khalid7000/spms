package com.rit.spms.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CoverageReportResponse {
    private Long strategyId;
    private String strategyTitle;
    private List<ObjectiveCoverage> objectives;

    @Data
    @Builder
    public static class ObjectiveCoverage {
        private Long universityObjectiveId;
        private String universityObjectiveTitle;
        private boolean hasCoverage;
        private List<Long> mappedDeptObjectiveIds;
        private List<InitiativeCoverage> initiatives;
    }

    @Data
    @Builder
    public static class InitiativeCoverage {
        private Long universityInitiativeId;
        private String universityInitiativeTitle;
        private boolean hasCoverage;
        private List<Long> mappedDeptInitiativeIds;
    }
}
