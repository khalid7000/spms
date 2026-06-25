package com.rit.spms.dto.request;

import lombok.Data;

@Data
public class AssignGoalAreaRequest {
    // null means remove from area (ungrouped)
    private Long areaId;
}
