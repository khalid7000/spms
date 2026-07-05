package com.rit.spms.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SwotFinalizationResultResponse {
    private List<Long> createdAreaIds;
    private List<Long> createdGoalIds;
}
