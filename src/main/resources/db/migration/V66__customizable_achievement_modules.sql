-- Customizable Achievement Modules: an extensible, admin-assignable menu of achievement-recording
-- helpers (starting with "Teaching Evaluations"), each wired to exactly one CategoryCriteria per
-- EmployeeTitle. Achievements they produce are ordinary Achievement/PortfolioEntry rows -- not
-- linked to the Strategy Tree -- so `measurement` must become optional.
ALTER TABLE achievement ALTER COLUMN measurement_id DROP NOT NULL;

-- Admin assignment: which module is wired to which criterion. Enforced in the service layer (not
-- here) that a given module_code has at most one criterion assigned within any one title.
CREATE TABLE criteria_achievement_module (
    id BIGSERIAL PRIMARY KEY,
    module_code VARCHAR(100) NOT NULL,
    criteria_id BIGINT NOT NULL REFERENCES category_criteria(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (module_code, criteria_id)
);

-- Teaching Evaluations module: a short-lived working area per (evaluation, criterion) while the
-- employee uploads files and drafts, discarded once finalized into a real Achievement.
CREATE TABLE teaching_evaluation_session (
    id BIGSERIAL PRIMARY KEY,
    evaluation_id BIGINT NOT NULL REFERENCES annual_evaluation(id),
    criteria_id BIGINT NOT NULL REFERENCES category_criteria(id),
    local_folder_note VARCHAR(1000),
    extracted_text TEXT,
    uploaded_file_names VARCHAR(2000),
    generation_requested_at TIMESTAMP,
    generated_at TIMESTAMP,
    generation_failure_reason VARCHAR(1000),
    draft_details TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (evaluation_id, criteria_id)
);
