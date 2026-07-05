package com.rit.spms.dto.request;

import com.rit.spms.domain.enums.SwotReviewActionType;
import com.rit.spms.domain.enums.SwotReviewTargetType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SwotFinalDecisionRequest {
    @NotNull(message = "Target type is required")
    private SwotReviewTargetType targetType;

    @NotNull(message = "Target id is required")
    private Long targetId;

    @NotNull(message = "Action is required")
    private SwotReviewActionType actionType;

    private String editedTitle;
    private String editedDescription;
}
