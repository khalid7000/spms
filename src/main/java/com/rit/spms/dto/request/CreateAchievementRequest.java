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

    // Required only when achievementTypeId refers to the "Other" preset -- validated in
    // AchievementService, not here, since that requires looking up the type's name.
    private String customTypeName;

    private String details;
    private String privateNotes;
    private Long assessmentPeriodId;
}
