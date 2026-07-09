ALTER TABLE annual_evaluation_category_result ADD COLUMN head_comments TEXT;
ALTER TABLE annual_evaluation_goal_result ADD COLUMN head_goal_rank INTEGER;
ALTER TABLE annual_evaluation ADD COLUMN head_signature_name VARCHAR(200);
