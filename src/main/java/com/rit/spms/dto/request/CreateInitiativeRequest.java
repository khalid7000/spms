package com.rit.spms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateInitiativeRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private Integer sortOrder = 0;
    private Long universityInitiativeId;
    private Long academicYearId;
    private Long assessmentPeriodId;

    /** Optional -- when present, InitiativeService.createInitiative creates this Measurement
     * in the same transaction as the Initiative, so the two are never left partially created. */
    private CreateMeasurementRequest measurement;
}
