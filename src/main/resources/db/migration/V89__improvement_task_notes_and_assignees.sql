-- Notes stay attached to the task AND to whichever employee wrote them, permanently -- reassigning
-- or stripping a task never touches this table, only the assignee links below.
CREATE TABLE improvement_task_note (
    id BIGSERIAL PRIMARY KEY,
    improvement_task_id BIGINT NOT NULL REFERENCES improvement_task(id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES app_user(id),
    body TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_improvement_task_note_task ON improvement_task_note(improvement_task_id);

-- Collaborators an owner (or the map's author/admin) adds to a PULLED/IN_PROGRESS task -- they can
-- see it and add notes, but only the recorded owner (improvement_task.pulled_by) or an admin may
-- change its state. Wiped whenever the map's author returns the task to the board (see
-- ImprovementTaskService#returnToBoard) -- notes above are untouched.
CREATE TABLE improvement_task_assignee (
    id BIGSERIAL PRIMARY KEY,
    improvement_task_id BIGINT NOT NULL REFERENCES improvement_task(id) ON DELETE CASCADE,
    employee_id BIGINT NOT NULL REFERENCES app_user(id),
    added_by_id BIGINT NOT NULL REFERENCES app_user(id),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (improvement_task_id, employee_id)
);

CREATE INDEX idx_improvement_task_assignee_task ON improvement_task_assignee(improvement_task_id);
CREATE INDEX idx_improvement_task_assignee_employee ON improvement_task_assignee(employee_id);
