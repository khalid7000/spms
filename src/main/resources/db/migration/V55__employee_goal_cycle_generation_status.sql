-- Same async-generation status tracking as V32 gave swot_session: lets the leader's "Retry
-- Generation" button and the in-progress message distinguish a genuine failure from AI goal
-- suggestion generation that is simply still running in the background.
ALTER TABLE employee_goal_cycle ADD COLUMN generation_requested_at TIMESTAMP;
ALTER TABLE employee_goal_cycle ADD COLUMN suggestions_generated_at TIMESTAMP;
ALTER TABLE employee_goal_cycle ADD COLUMN generation_failure_reason VARCHAR(1000);
