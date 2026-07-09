-- Achievements can be designated against a goal (in addition to category/criteria), and an
-- employee can explicitly say "nothing to report" for a criteria or goal this cycle, instead of
-- submission only silently allowing zero achievements against it.

ALTER TABLE annual_evaluation_criteria_result ADD COLUMN employee_nothing_to_report BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE annual_evaluation_goal_result (
    id BIGSERIAL PRIMARY KEY,
    evaluation_id BIGINT NOT NULL REFERENCES annual_evaluation(id) ON DELETE CASCADE,
    goal_id BIGINT NOT NULL REFERENCES employee_goal(id),
    employee_nothing_to_report BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (evaluation_id, goal_id)
);
