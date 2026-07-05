-- V8: Add assessment_period_id to initiative; add private_notes to achievement

ALTER TABLE initiative
    ADD COLUMN assessment_period_id BIGINT REFERENCES assessment_period(id);

ALTER TABLE achievement
    ADD COLUMN private_notes TEXT;
