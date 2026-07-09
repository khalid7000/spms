-- Lets a user describe their own achievement type when none of the presets fit ("Other" +
-- free text), instead of being forced to pick the closest existing AchievementType.
ALTER TABLE achievement ADD COLUMN custom_type_name VARCHAR(200);

INSERT INTO achievement_type (name) VALUES ('Community'), ('Curriculum'), ('Other')
ON CONFLICT (name) DO NOTHING;
