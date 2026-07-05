-- SWOT collaborative workflow: word collection -> ranked voting -> AI-suggested
-- areas/goals -> per-user review -> owner finalization -> draft VisionArea/Goal rows.
-- Runs entirely within Strategy.state = CREATION; does not add new strategy states.

CREATE TABLE swot_session (
    id BIGSERIAL PRIMARY KEY,
    strategy_id BIGINT NOT NULL UNIQUE REFERENCES strategy(id) ON DELETE CASCADE,
    phase VARCHAR(30) NOT NULL DEFAULT 'COLLECTING',
    started_by BIGINT REFERENCES app_user(id),
    voting_closed_at TIMESTAMP,
    suggestions_generated_at TIMESTAMP,
    review_locked_at TIMESTAMP,
    finalized_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE swot_participant (
    id BIGSERIAL PRIMARY KEY,
    swot_session_id BIGINT NOT NULL REFERENCES swot_session(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES app_user(id),
    role_at_invite VARCHAR(20) NOT NULL,
    invited_at TIMESTAMP NOT NULL DEFAULT NOW(),
    swot_submitted_at TIMESTAMP,
    vote_submitted_at TIMESTAMP,
    review_submitted_at TIMESTAMP,
    UNIQUE(swot_session_id, user_id)
);

CREATE TABLE swot_entry (
    id BIGSERIAL PRIMARY KEY,
    swot_session_id BIGINT NOT NULL REFERENCES swot_session(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES app_user(id),
    quadrant VARCHAR(20) NOT NULL,
    word VARCHAR(100) NOT NULL,
    normalized_word VARCHAR(100) NOT NULL,
    justification VARCHAR(500) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(swot_session_id, user_id, quadrant, normalized_word)
);

CREATE TABLE swot_vote_entry (
    id BIGSERIAL PRIMARY KEY,
    swot_session_id BIGINT NOT NULL REFERENCES swot_session(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES app_user(id),
    quadrant VARCHAR(20) NOT NULL,
    rank INT NOT NULL,
    normalized_word VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(swot_session_id, user_id, quadrant, rank),
    UNIQUE(swot_session_id, user_id, quadrant, normalized_word)
);

CREATE TABLE swot_quadrant_result (
    id BIGSERIAL PRIMARY KEY,
    swot_session_id BIGINT NOT NULL REFERENCES swot_session(id) ON DELETE CASCADE,
    quadrant VARCHAR(20) NOT NULL,
    normalized_word VARCHAR(100) NOT NULL,
    display_word VARCHAR(100) NOT NULL,
    total_score INT NOT NULL,
    rank_position INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(swot_session_id, quadrant, normalized_word)
);

CREATE TABLE swot_suggestion (
    id BIGSERIAL PRIMARY KEY,
    swot_session_id BIGINT NOT NULL REFERENCES swot_session(id) ON DELETE CASCADE,
    name VARCHAR(300) NOT NULL,
    rationale TEXT,
    sort_order INT NOT NULL DEFAULT 0,
    generated_by_model VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE swot_suggested_goal (
    id BIGSERIAL PRIMARY KEY,
    swot_suggestion_id BIGINT NOT NULL REFERENCES swot_suggestion(id) ON DELETE CASCADE,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE swot_alternative_proposal (
    id BIGSERIAL PRIMARY KEY,
    swot_session_id BIGINT NOT NULL REFERENCES swot_session(id) ON DELETE CASCADE,
    proposed_by BIGINT NOT NULL REFERENCES app_user(id),
    name VARCHAR(300) NOT NULL,
    rationale TEXT,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE swot_alternative_proposed_goal (
    id BIGSERIAL PRIMARY KEY,
    alternative_proposal_id BIGINT NOT NULL REFERENCES swot_alternative_proposal(id) ON DELETE CASCADE,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE swot_review_item (
    id BIGSERIAL PRIMARY KEY,
    swot_session_id BIGINT NOT NULL REFERENCES swot_session(id) ON DELETE CASCADE,
    reviewer_id BIGINT NOT NULL REFERENCES app_user(id),
    target_type VARCHAR(20) NOT NULL,
    target_id BIGINT NOT NULL,
    action_type VARCHAR(30) NOT NULL,
    edited_title VARCHAR(500),
    edited_description TEXT,
    is_owner_final BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(swot_session_id, reviewer_id, target_type, target_id, is_owner_final)
);

CREATE INDEX idx_swot_entry_session_quadrant ON swot_entry(swot_session_id, quadrant);
CREATE INDEX idx_swot_review_item_target ON swot_review_item(swot_session_id, target_type, target_id);
CREATE INDEX idx_swot_participant_user ON swot_participant(user_id);
