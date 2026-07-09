-- Replaces the single is_admin boolean with a proper multi-role system: every user is implicitly
-- an "Employee" (not stored -- it's the base capability everyone already has), and can additionally
-- hold ADMIN and/or HR roles, multiple at once.

CREATE TABLE app_user_system_role (
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, role)
);

INSERT INTO app_user_system_role (user_id, role)
SELECT id, 'ADMIN' FROM app_user WHERE is_admin = true;

ALTER TABLE app_user DROP COLUMN is_admin;
