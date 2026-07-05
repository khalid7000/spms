-- Lets an Editor (during REVIEWING) or the Owner (during FINALIZING) propose an entirely new
-- goal under an existing AI-suggested area, alongside the AI's own suggested goals. The owner
-- decides at finalization whether each proposed addition becomes a real Goal (same
-- SwotReviewItem/GOAL_ADDITION mechanism used for everything else in this workflow).
CREATE TABLE swot_suggested_goal_addition (
    id BIGSERIAL PRIMARY KEY,
    swot_suggestion_id BIGINT NOT NULL REFERENCES swot_suggestion(id) ON DELETE CASCADE,
    proposed_by BIGINT NOT NULL REFERENCES app_user(id),
    title VARCHAR(500) NOT NULL,
    description TEXT,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_swot_goal_addition_suggestion ON swot_suggested_goal_addition(swot_suggestion_id);
