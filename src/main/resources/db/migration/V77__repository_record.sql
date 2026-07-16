-- Generic central data repository fed by admin-run "readers" (first: EarlyAlert). Each row is one
-- record, keyed by (source_type, secondary_key, employee_email) -- multiple rows can share a key.
CREATE TABLE repository_record (
    id BIGSERIAL PRIMARY KEY,
    source_type VARCHAR(100) NOT NULL,
    secondary_key VARCHAR(100) NOT NULL,
    secondary_key_label VARCHAR(200),
    employee_email VARCHAR(200) NOT NULL,
    value TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_repository_record_lookup ON repository_record(source_type, secondary_key, employee_email);
CREATE INDEX idx_repository_record_employee ON repository_record(source_type, employee_email);
