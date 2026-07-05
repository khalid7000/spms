package com.rit.spms.dto.request;

import com.rit.spms.domain.enums.SwotReviewActionType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SwotReviewItemRequest {
    @NotNull(message = "Action is required")
    private SwotReviewActionType actionType;

    private String editedTitle;
    private String editedDescription;
}
