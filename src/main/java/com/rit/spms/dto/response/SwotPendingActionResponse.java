package com.rit.spms.dto.response;

import com.rit.spms.domain.enums.SwotPhase;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SwotPendingActionResponse {
    private Long strategyId;
    private String strategyTitle;
    private SwotPhase phase;
    /** One of SUBMIT_SWOT, VOTE, REVIEW, FINALIZE. */
    private String actionNeeded;
}
