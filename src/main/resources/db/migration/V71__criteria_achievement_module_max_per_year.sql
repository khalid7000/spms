ALTER TABLE criteria_achievement_module
    ADD COLUMN max_achievements_per_year INT;

UPDATE criteria_achievement_module SET max_achievements_per_year = 10 WHERE max_achievements_per_year IS NULL;

ALTER TABLE criteria_achievement_module
    ALTER COLUMN max_achievements_per_year SET NOT NULL;
