package com.rit.spms.dto.response;

import com.rit.spms.domain.enums.SwotReviewActionType;
import com.rit.spms.domain.enums.SwotReviewTargetType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SwotReviewItemResponse {
    private Long id;
    private Long reviewerId;
    private String reviewerName;
    private SwotReviewTargetType targetType;
    private Long targetId;
    private SwotReviewActionType actionType;
    private String editedTitle;
    private String editedDescription;
    private boolean ownerFinal;
}
