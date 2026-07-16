ALTER TABLE app_user ADD COLUMN org_group_id BIGINT REFERENCES org_group(id);
