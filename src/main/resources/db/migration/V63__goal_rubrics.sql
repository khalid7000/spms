ALTER TABLE employee_goal
    ADD COLUMN rubric_unsatisfactory TEXT,
    ADD COLUMN rubric_meets_expectations TEXT,
    ADD COLUMN rubric_exceeds_expectations TEXT;

ALTER TABLE employee_goal_suggestion
    ADD COLUMN rubric_unsatisfactory TEXT,
    ADD COLUMN rubric_meets_expectations TEXT,
    ADD COLUMN rubric_exceeds_expectations TEXT;
