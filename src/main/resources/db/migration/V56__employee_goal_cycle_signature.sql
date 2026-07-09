-- Captures the typed-name "signature" an employee provides when accepting and deploying their
-- goals for the academic year -- an explicit affirmation step, not just a click-through confirm.
ALTER TABLE employee_goal_cycle ADD COLUMN employee_signature_name VARCHAR(200);
