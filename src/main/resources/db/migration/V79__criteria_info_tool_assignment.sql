-- Admin assignment of a CriteriaInfoTool (head-only viewer, parallel to criteria_achievement_module
-- but for viewing rather than recording) to one CategoryCriteria.
CREATE TABLE criteria_info_tool_assignment (
    id BIGSERIAL PRIMARY KEY,
    tool_code VARCHAR(100) NOT NULL,
    criteria_id BIGINT NOT NULL REFERENCES category_criteria(id) ON DELETE CASCADE,
    display_name VARCHAR(200) NOT NULL,
    repository_source_type VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (tool_code, criteria_id)
);
