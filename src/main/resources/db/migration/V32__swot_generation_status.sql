-- Tracks when AI suggestion generation was requested and why it last failed, so the owner's
-- "Retry Generation" button (and the waiting message shown to everyone) can distinguish a
-- genuine failure from generation that is simply still running.
ALTER TABLE swot_session ADD COLUMN generation_requested_at TIMESTAMP;
ALTER TABLE swot_session ADD COLUMN generation_failure_reason VARCHAR(1000);
