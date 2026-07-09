-- Admin-set three-level evaluation rubric (Unsatisfactory / Meets Expectations / Exceeds
-- Expectations) per criteria, per the Faculty Evaluation Rubrics Sheet -- used by the head as
-- reference when giving the 1-5 rank during an Annual Evaluation. Nullable: criteria for titles
-- other than Faculty/Research Faculty have no rubric sheet yet.
ALTER TABLE category_criteria ADD COLUMN rubric_unsatisfactory TEXT;
ALTER TABLE category_criteria ADD COLUMN rubric_meets_expectations TEXT;
ALTER TABLE category_criteria ADD COLUMN rubric_exceeds_expectations TEXT;
