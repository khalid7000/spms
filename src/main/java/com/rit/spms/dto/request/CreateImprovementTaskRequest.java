package com.rit.spms.dto.request;

import com.rit.spms.domain.enums.VsmTaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateImprovementTaskRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Task type is required")
    private VsmTaskType taskType;

    /** Optional early reporting link (decision #1 in the round-1 VSM plan) -- if unset here, the
     *  completer resolves one at achievement-logging time instead (see LogTaskAchievementRequest). */
    private Long linkedInitiativeId;
}
