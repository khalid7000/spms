-- Portfolio Entries: thin 1:1 evaluation extension of an existing achievement record.
-- The achievement itself (title/details/measurement) lives entirely in the `achievement` table;
-- this only carries portfolio-specific evaluation metadata so that logging an achievement against
-- a strategy initiative and logging it for the annual evaluation are the same action.
CREATE TABLE portfolio_entry (
    id BIGSERIAL PRIMARY KEY,
    achievement_id BIGINT NOT NULL UNIQUE REFERENCES achievement(id) ON DELETE CASCADE,
    employee_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES portfolio_category(id) ON DELETE CASCADE,
    goal_id BIGINT REFERENCES employee_goal(id) ON DELETE SET NULL,
    category_rating INT CHECK (category_rating >= 1 AND category_rating <= 5),
    evidence_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_portfolio_entry_employee ON portfolio_entry(employee_id);
CREATE INDEX idx_portfolio_entry_category ON portfolio_entry(category_id);
CREATE INDEX idx_portfolio_entry_goal ON portfolio_entry(goal_id);
CREATE INDEX idx_portfolio_entry_employee_category ON portfolio_entry(employee_id, category_id);
