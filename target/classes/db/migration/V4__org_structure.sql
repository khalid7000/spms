-- Org group (self-referential tree: department → group → parent group → ...)
CREATE TABLE org_group (
    id            BIGSERIAL PRIMARY KEY,
    title         VARCHAR(200) NOT NULL,
    head_title    VARCHAR(100) NOT NULL,
    parent_id     BIGINT REFERENCES org_group(id) ON DELETE SET NULL,
    head_user_id  BIGINT REFERENCES app_user(id) ON DELETE SET NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Extend department with head info and group membership
ALTER TABLE department
    ADD COLUMN head_title   VARCHAR(100),
    ADD COLUMN head_user_id BIGINT REFERENCES app_user(id) ON DELETE SET NULL,
    ADD COLUMN org_group_id BIGINT REFERENCES org_group(id) ON DELETE SET NULL;

-- Widen strategy.state to accommodate APPROVAL_PENDING (16 chars)
ALTER TABLE strategy ALTER COLUMN state TYPE VARCHAR(30);

-- Deployment approval records (one row per required approver per strategy)
CREATE TABLE strategy_approval (
    id                    BIGSERIAL PRIMARY KEY,
    strategy_id           BIGINT NOT NULL REFERENCES strategy(id) ON DELETE CASCADE,
    required_approver_id  BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    approver_title        VARCHAR(300) NOT NULL,
    approval_order        INT NOT NULL DEFAULT 0,
    approved              BOOLEAN NOT NULL DEFAULT FALSE,
    approved_at           TIMESTAMP,
    created_at            TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (strategy_id, required_approver_id)
);
