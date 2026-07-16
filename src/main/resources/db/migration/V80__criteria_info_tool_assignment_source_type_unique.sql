-- Uniqueness must be scoped by (tool_code, repository_source_type), not tool_code alone -- there's
-- one CriteriaInfoTool Java implementation (CentralRepositoryViewerTool) but it's parameterized by
-- repository_source_type (Early Alert vs Grade Distribution), which behave as two distinct tools
-- for assignment purposes. Under the old constraint, assigning one repository type anywhere would
-- silently violate uniqueness against a different repository type using the same tool_code.
ALTER TABLE criteria_info_tool_assignment DROP CONSTRAINT criteria_info_tool_assignment_tool_code_criteria_id_key;
ALTER TABLE criteria_info_tool_assignment ADD CONSTRAINT criteria_info_tool_assignment_unique
    UNIQUE (tool_code, repository_source_type, criteria_id);
