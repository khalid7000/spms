-- VSM author delegation (Phase 4 of the Value Stream Mapping module): an Admin can grant "VSM
-- author" rights over a unit to an employee who isn't its head, but it needs sign-off from the
-- top-of-hierarchy head above that employee before it's active. Shaped like strategy_approval
-- (required_approver_id/approver_title) so a future cross-type "my approvals" query is a simple
-- merge rather than a schema change.
CREATE TABLE vsm_author_grant (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES app_user(id),
    granted_by_admin_id BIGINT NOT NULL REFERENCES app_user(id),
    scope_type VARCHAR(20) NOT NULL,
    department_id BIGINT REFERENCES department(id),
    org_group_id BIGINT REFERENCES org_group(id),
    required_approver_id BIGINT NOT NULL REFERENCES app_user(id),
    approver_title VARCHAR(300) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_APPROVAL',
    decided_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_vsm_author_grant_employee ON vsm_author_grant(employee_id);
CREATE INDEX idx_vsm_author_grant_approver ON vsm_author_grant(required_approver_id);
