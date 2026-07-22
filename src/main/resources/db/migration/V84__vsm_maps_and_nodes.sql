-- Value Stream Mapping module, Phase 1: a leader's editable canvas of process/data-box/
-- supplier-customer/kaizen-burst nodes and connecting edges. Standalone from strategy/initiative --
-- see vsm_map having no planning_cycle_id/academic_year_id, unlike strategy/initiative/measurement.
CREATE TABLE vsm_map (
    id BIGSERIAL PRIMARY KEY,
    scope_type VARCHAR(20) NOT NULL,
    department_id BIGINT REFERENCES department(id),
    org_group_id BIGINT REFERENCES org_group(id),
    title VARCHAR(300) NOT NULL,
    description TEXT,
    state VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by BIGINT NOT NULL REFERENCES app_user(id),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE vsm_node (
    id BIGSERIAL PRIMARY KEY,
    vsm_map_id BIGINT NOT NULL REFERENCES vsm_map(id) ON DELETE CASCADE,
    node_type VARCHAR(30) NOT NULL,
    position_x DOUBLE PRECISION NOT NULL,
    position_y DOUBLE PRECISION NOT NULL,
    title VARCHAR(300) NOT NULL,
    description TEXT,
    cycle_time_minutes NUMERIC(10, 2),
    complete_accurate_percent NUMERIC(5, 2),
    fail_rate_percent NUMERIC(5, 2),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE vsm_node_metric (
    id BIGSERIAL PRIMARY KEY,
    node_id BIGINT NOT NULL REFERENCES vsm_node(id) ON DELETE CASCADE,
    label VARCHAR(200) NOT NULL,
    value NUMERIC(14, 4) NOT NULL,
    unit VARCHAR(20),
    display_order INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE vsm_edge (
    id BIGSERIAL PRIMARY KEY,
    vsm_map_id BIGINT NOT NULL REFERENCES vsm_map(id) ON DELETE CASCADE,
    source_node_id BIGINT NOT NULL REFERENCES vsm_node(id) ON DELETE CASCADE,
    target_node_id BIGINT NOT NULL REFERENCES vsm_node(id) ON DELETE CASCADE,
    edge_type VARCHAR(20) NOT NULL DEFAULT 'MATERIAL_FLOW',
    label VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_vsm_node_map ON vsm_node(vsm_map_id);
CREATE INDEX idx_vsm_edge_map ON vsm_edge(vsm_map_id);

-- Admin-sliceable notation packs (see VsmNotationPack) -- reuses the existing organization_setting
-- key/value store (V72) rather than new config plumbing, same mechanism as ENABLED_LANGUAGES (V82).
-- Only GENERIC is on by default; an Admin turns on MANUFACTURING (or future packs) as needed.
INSERT INTO organization_setting (setting_key, value, description) VALUES
    ('VSM_ENABLED_NOTATION_PACKS', 'GENERIC',
     'Comma-separated Value Stream Mapping notation packs available on the canvas palette (GENERIC, MANUFACTURING)');
