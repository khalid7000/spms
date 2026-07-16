-- Persists the head's Rating Assistant word selections (see RatingAssistantModal) so they survive
-- a page refresh, browser close, or later login -- not just an in-memory session. Strictly private
-- to the specific head who made them: scoped by head_id, never exposed to the employee or anyone
-- else viewing the same evaluation.
CREATE TABLE rating_assistant_selection (
    id BIGSERIAL PRIMARY KEY,
    evaluation_id BIGINT NOT NULL REFERENCES annual_evaluation(id),
    head_id BIGINT NOT NULL REFERENCES app_user(id),
    target_type VARCHAR(20) NOT NULL,
    target_id BIGINT NOT NULL,
    selection_history TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (evaluation_id, head_id, target_type, target_id)
);
