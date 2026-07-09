-- Generalizes notifications beyond Annual Evaluation: entity_id replaces the AnnualEvaluation-only
-- evaluation_id, and a type column tells the frontend what entity_id actually points at (and lets
-- new notification-producing flows -- strategy membership, approvals, SWOT invites -- reuse the
-- same table instead of each inventing their own).
ALTER TABLE notification RENAME COLUMN evaluation_id TO entity_id;
ALTER TABLE notification ADD COLUMN type VARCHAR(30);
UPDATE notification SET type = 'ANNUAL_EVALUATION' WHERE type IS NULL;
ALTER TABLE notification ALTER COLUMN type SET NOT NULL;
