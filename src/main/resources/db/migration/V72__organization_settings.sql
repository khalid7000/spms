-- Small, fixed set of admin-editable display labels so the app's vocabulary can be adapted to a
-- non-university organization (e.g. "Academic Year" -> "Fiscal Year") without a code change. Rows
-- are seeded here and only ever edited (see OrganizationSettingService) -- the key set is fixed by
-- migration, not admin-creatable, since the frontend only ever reads by these known keys.
CREATE TABLE organization_setting (
    id BIGSERIAL PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    value VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

INSERT INTO organization_setting (setting_key, value, description) VALUES
    ('ACADEMIC_YEAR_LABEL', 'Academic Year', 'Replaces "Academic Year" throughout the app (e.g. Fiscal Year, Review Period)'),
    ('TOP_LEVEL_STRATEGY_LABEL', 'University Strategy', 'Replaces "University Strategy" throughout the app (e.g. Corporate Strategy, Agency Strategy)'),
    ('DEFAULT_HEAD_TITLE_LABEL', 'Head', 'Fallback supervisor title shown when a department/org group has no Head Title set, and in tables spanning multiple departments');
