package com.rit.spms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Same field shape as PortfolioEntryController's LogAchievementRequest -- this is the "achievement
 *  fields required to close an IMPROVEMENT task" step (see VsmTaskAchievementService), reusing the
 *  existing achievement/portfolio-entry infrastructure verbatim rather than a parallel one. */
@Data
public class LogTaskAchievementRequest {
    @NotNull(message = "Measurement is required")
    private Long measurementId;

    @NotBlank(message = "Title is required")
    private String achievementTitle;

    @NotNull(message = "Achievement type is required")
    private Long achievementTypeId;

    private String customTypeName;
    private String details;
    private String privateNotes;

    @NotNull(message = "Assessment period is required")
    private Long assessmentPeriodId;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private Long criteriaId;
    private Integer categoryRating;
    private Long goalId;

    @NotBlank(message = "Evidence/Link is required")
    private String evidenceUrl;
}
