package com.rit.spms.domain.enums;

public enum SwotReviewTargetType {
    AREA,
    GOAL,
    /** A new goal an Editor or the Owner proposed under an existing AI-suggested area (see SwotSuggestedGoalAddition). */
    GOAL_ADDITION,
    ALTERNATIVE_AREA,
    ALTERNATIVE_GOAL
}
