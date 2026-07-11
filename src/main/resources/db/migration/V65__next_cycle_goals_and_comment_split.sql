-- Part A: split single "head comments" fields into Strengths / Potential Improvements.
ALTER TABLE annual_evaluation_category_result
    ADD COLUMN head_comments_strengths TEXT,
    ADD COLUMN head_comments_improvements TEXT;

UPDATE annual_evaluation_category_result SET head_comments_strengths = head_comments WHERE head_comments IS NOT NULL;

ALTER TABLE annual_evaluation_category_result DROP COLUMN head_comments;

ALTER TABLE annual_evaluation
    ADD COLUMN goals_head_comments_strengths TEXT,
    ADD COLUMN goals_head_comments_improvements TEXT;

UPDATE annual_evaluation SET goals_head_comments_strengths = goals_head_comments WHERE goals_head_comments IS NOT NULL;

ALTER TABLE annual_evaluation DROP COLUMN goals_head_comments;

-- Part B: Next Cycle Goals -- drafted/reviewed during this evaluation, reused later in Team Goal Setting.
ALTER TABLE annual_evaluation
    ADD COLUMN next_cycle_notes_strengths TEXT,
    ADD COLUMN next_cycle_notes_weaknesses TEXT,
    ADD COLUMN next_cycle_generation_requested_at TIMESTAMP,
    ADD COLUMN next_cycle_generated_at TIMESTAMP,
    ADD COLUMN next_cycle_generation_failure_reason VARCHAR(1000);

CREATE TABLE annual_evaluation_next_cycle_goal (
    id BIGSERIAL PRIMARY KEY,
    evaluation_id BIGINT NOT NULL REFERENCES annual_evaluation(id),
    category_id BIGINT NOT NULL REFERENCES portfolio_category(id),
    suggested_title VARCHAR(500) NOT NULL,
    suggested_description TEXT,
    rationale TEXT,
    generated_by_model VARCHAR(100),
    sort_order INTEGER NOT NULL DEFAULT 0,
    rubric_unsatisfactory TEXT,
    rubric_meets_expectations TEXT,
    rubric_exceeds_expectations TEXT,
    leader_action_type VARCHAR(30),
    leader_edited_title VARCHAR(500),
    leader_edited_description TEXT,
    leader_reviewed_at TIMESTAMP,
    employee_action_type VARCHAR(30),
    employee_edited_title VARCHAR(500),
    employee_edited_description TEXT,
    employee_reviewed_at TIMESTAMP,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    used_in_cycle_id BIGINT REFERENCES employee_goal_cycle(id),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
