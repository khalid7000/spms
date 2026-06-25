-- Adds forced-password-change flag; default FALSE for all existing users.
ALTER TABLE app_user
    ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT FALSE;
