-- A stable identifier for the two achievement types other code branches on (the "Other" custom-type
-- flow, and the Teaching Evaluations module's gating) -- previously matched by display NAME, which
-- silently breaks if an admin renames or deactivates the wrong row. Admins can still freely rename
-- either row (renaming is the whole point of making the list configurable); this column is never
-- writable through the achievement-type admin API, only ever set here by migration.
ALTER TABLE achievement_type ADD COLUMN system_code VARCHAR(50) UNIQUE;

UPDATE achievement_type SET system_code = 'OTHER' WHERE name = 'Other';
UPDATE achievement_type SET system_code = 'COURSE_EVALUATION' WHERE name = 'Course Evaluation';
