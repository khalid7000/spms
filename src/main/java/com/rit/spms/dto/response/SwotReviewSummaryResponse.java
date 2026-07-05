package com.rit.spms.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SwotReviewSummaryResponse {
    private List<SwotSuggestionResponse> suggestions;
    private List<SwotAlternativeProposalResponse> alternatives;
    /** All non-owner-final review verdicts across all reviewers, for every target. */
    private List<SwotReviewItemResponse> reviewItems;
    /** The current owner's own draft-final decisions, if any have been saved yet. */
    private List<SwotReviewItemResponse> ownerFinalDecisions;
    /** Every Editor- (or Owner-) proposed new goal under an existing suggested area, across the whole session. */
    private List<SwotSuggestedGoalAdditionResponse> goalAdditions;
}
