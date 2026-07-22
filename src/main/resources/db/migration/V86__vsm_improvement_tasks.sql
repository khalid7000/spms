-- Kaizen-burst -> Kanban improvement tasks (Phase 3 of the Value Stream Mapping module). No
-- separate "board" table -- per-map and department-rollup boards are both queries over this table
-- (see VsmBoardService). achievement_id stays nullable/unused until Phase 4 wires up the
-- achievement-linkage gate for IMPROVEMENT tasks.
CREATE TABLE improvement_task (
    id BIGSERIAL PRIMARY KEY,
    kaizen_node_id BIGINT NOT NULL REFERENCES vsm_node(id) ON DELETE CASCADE,
    title VARCHAR(300) NOT NULL,
    description TEXT,
    task_type VARCHAR(20) NOT NULL,
    state VARCHAR(20) NOT NULL DEFAULT 'BACKLOG',
    created_by BIGINT NOT NULL REFERENCES app_user(id),
    pulled_by BIGINT REFERENCES app_user(id),
    pulled_at TIMESTAMP,
    linked_initiative_id BIGINT REFERENCES initiative(id),
    achievement_id BIGINT UNIQUE REFERENCES achievement(id),
    done_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_improvement_task_kaizen_node ON improvement_task(kaizen_node_id);
CREATE INDEX idx_improvement_task_state ON improvement_task(state);
