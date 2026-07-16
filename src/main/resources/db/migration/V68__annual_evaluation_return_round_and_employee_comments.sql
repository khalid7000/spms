ALTER TABLE annual_evaluation
    ADD COLUMN returned_to_employee_at TIMESTAMP,
    ADD COLUMN goals_employee_comments TEXT;

ALTER TABLE annual_evaluation_category_result
    ADD COLUMN employee_comments TEXT;
