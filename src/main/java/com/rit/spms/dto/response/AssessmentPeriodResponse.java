package com.rit.spms.dto.response;

import com.rit.spms.domain.AssessmentPeriod;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssessmentPeriodResponse {
    private Long id;
    private String name;

    public static AssessmentPeriodResponse from(AssessmentPeriod p) {
        return AssessmentPeriodResponse.builder().id(p.getId()).name(p.getName()).build();
    }
}
