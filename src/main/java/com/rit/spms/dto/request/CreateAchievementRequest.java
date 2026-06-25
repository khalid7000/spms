package com.rit.spms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAchievementRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Achievement type is required")
    private Long achievementTypeId;

    private String details;
    private Long assessmentPeriodId;
}
