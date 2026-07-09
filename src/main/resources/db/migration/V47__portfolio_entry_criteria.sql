-- Achievements now tag to a specific criteria within their category (not just the category),
-- which the Annual Evaluation workflow rates individually. Nullable: existing entries predate
-- this and the achievement-logging form will require it going forward.
ALTER TABLE portfolio_entry ADD COLUMN criteria_id BIGINT REFERENCES category_criteria(id);
