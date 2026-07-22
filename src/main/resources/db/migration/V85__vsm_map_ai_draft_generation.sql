-- Async AI draft-generation tracking for VsmMap, same shape as employee_goal_cycle's
-- generation_requested_at/suggestions_generated_at/generation_failure_reason and
-- teaching_evaluation_session's equivalent columns -- the background job (VsmDraftGenerationService)
-- reads draft_process_description fresh from the row rather than being passed it directly, so a
-- retry after failure or a page reload mid-generation needs no re-entry from the user.
ALTER TABLE vsm_map
    ADD COLUMN generation_requested_at TIMESTAMP,
    ADD COLUMN generated_at TIMESTAMP,
    ADD COLUMN generation_failure_reason VARCHAR(1000),
    ADD COLUMN draft_process_description TEXT;
