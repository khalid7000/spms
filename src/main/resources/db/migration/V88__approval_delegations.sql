-- General-purpose approval delegation: any employee who is a headship-derived required approver
-- (Strategy approval chains, VSM author-grant approval, and any future approval type resolved via
-- PermissionService#resolveEffectiveApprover) can hand that authority to another employee for a
-- bounded window. Delegating to an ancestor head or a direct report activates immediately; anyone
-- else needs the delegator's own manager to approve first (requires_manager_approval / status),
-- unless the delegator is at the top of the org pyramid.
CREATE TABLE approval_delegation (
    id BIGSERIAL PRIMARY KEY,
    delegator_id BIGINT NOT NULL REFERENCES app_user(id),
    delegate_id BIGINT NOT NULL REFERENCES app_user(id),
    scope_type VARCHAR(20) NOT NULL,
    department_id BIGINT REFERENCES department(id),
    org_group_id BIGINT REFERENCES org_group(id),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    requires_manager_approval BOOLEAN NOT NULL DEFAULT FALSE,
    manager_approver_id BIGINT REFERENCES app_user(id),
    decided_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_approval_delegation_delegator ON approval_delegation(delegator_id);
CREATE INDEX idx_approval_delegation_delegate ON approval_delegation(delegate_id);
CREATE INDEX idx_approval_delegation_manager_approver ON approval_delegation(manager_approver_id);
