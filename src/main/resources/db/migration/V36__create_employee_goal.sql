-- Employee Goal Cycle: one per (employee, academic year), owns the mutual-approval workflow state
CREATE TABLE employee_goal_cycle (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    leader_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    academic_year_id BIGINT NOT NULL REFERENCES academic_year(id) ON DELETE CASCADE,
    state VARCHAR(30) NOT NULL DEFAULT 'DRAFT' CHECK (state IN ('DRAFT', 'LEADER_SUBMITTED', 'EMPLOYEE_REVIEW', 'EMPLOYEE_SUBMITTED', 'DEPLOYED', 'ARCHIVED')),
    leader_strengths TEXT,
    leader_weaknesses TEXT,
    leader_submitted_at TIMESTAMP,
    employee_accepted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(employee_id, academic_year_id)
);

CREATE INDEX idx_employee_goal_cycle_employee ON employee_goal_cycle(employee_id);
CREATE INDEX idx_employee_goal_cycle_leader ON employee_goal_cycle(leader_id);
CREATE INDEX idx_employee_goal_cycle_state ON employee_goal_cycle(state);

-- AI-generated (or leader-authored) goal candidates awaiting the leader's review decision
CREATE TABLE employee_goal_suggestion (
    id BIGSERIAL PRIMARY KEY,
    cycle_id BIGINT NOT NULL REFERENCES employee_goal_cycle(id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES portfolio_category(id) ON DELETE CASCADE,
    suggested_title VARCHAR(500) NOT NULL,
    suggested_description TEXT,
    rationale TEXT,
    generated_by_model VARCHAR(100),
    sort_order INT NOT NULL DEFAULT 0,
    leader_action_type VARCHAR(30) CHECK (leader_action_type IN ('REJECT', 'APPROVE_AS_IS', 'APPROVE_WITH_EDITS', 'PROPOSE_ALTERNATIVE')),
    edited_title VARCHAR(500),
    edited_description TEXT,
    leader_reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_employee_goal_suggestion_cycle ON employee_goal_suggestion(cycle_id);

-- Materialized goals for a cycle, produced once the leader submits for employee review
CREATE TABLE employee_goal (
    id BIGSERIAL PRIMARY KEY,
    cycle_id BIGINT NOT NULL REFERENCES employee_goal_cycle(id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES portfolio_category(id) ON DELETE CASCADE,
    measurement_id BIGINT REFERENCES measurement(id) ON DELETE SET NULL,
    goal_title VARCHAR(500) NOT NULL,
    description TEXT,
    sort_order INT NOT NULL DEFAULT 0,
    employee_action_type VARCHAR(30) CHECK (employee_action_type IN ('APPROVE_AS_IS', 'APPROVE_WITH_EDITS', 'PROPOSE_ALTERNATIVE')),
    employee_edited_title VARCHAR(500),
    employee_edited_description TEXT,
    employee_reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(cycle_id, goal_title)
);

CREATE INDEX idx_employee_goal_cycle_fk ON employee_goal(cycle_id);
CREATE INDEX idx_employee_goal_category ON employee_goal(category_id);
