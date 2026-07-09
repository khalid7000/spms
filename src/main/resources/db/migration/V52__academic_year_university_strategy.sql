-- An academic year must belong to exactly one university-level strategy (its cycle) -- previously
-- AcademicYearService.create() copied initiatives/measurements from *every* DEPLOYED strategy
-- system-wide, so a new academic year would get contaminated with initiatives from unrelated
-- university strategies (e.g. both the 2022-2027 and 2028-2033 university plans at once) instead
-- of just the one cycle it actually belongs to.

ALTER TABLE academic_year ADD COLUMN university_strategy_id BIGINT REFERENCES strategy(id);

-- Backfill: all existing academic years (2022-2024, 2024-2025, 2025-2026) predate every cycle
-- except the earliest deployed university strategy, so that's unambiguously the one they belong to.
UPDATE academic_year SET university_strategy_id = (
    SELECT s.id FROM strategy s JOIN planning_cycle pc ON pc.id = s.planning_cycle_id
    WHERE s.strategy_type = 'UNIVERSITY' AND s.state = 'DEPLOYED'
    ORDER BY pc.start_year ASC LIMIT 1
)
WHERE university_strategy_id IS NULL;

ALTER TABLE academic_year ALTER COLUMN university_strategy_id SET NOT NULL;
