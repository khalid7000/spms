-- V21: Merge three duplicate department pairs.
--
-- Three departments were created by the seed migrations under slightly
-- different names than the canonical departments that were created
-- via the admin UI. Each duplicate has the strategy data but no users;
-- the canonical department has the users and department head but no strategy.
--
-- Pairs (duplicate → canonical):
--   "Math and Sciences"               (id=21, MAS)  → "Math and Science"       (id=8,  MnS)
--   "Mechanical and Industrial Eng."  (id=17, MEIE) → "Mechanical Engineering"  (id=2,  ME)
--   "EE and Computing"                (id=15, EECS) → "Electrical Engineering"  (id=3,  EE)
--
-- For each pair: move the strategy to the canonical dept, retitle the
-- strategy and its abbreviated goal, then delete the duplicate dept row.
-- No app_user rows reference the duplicate departments (all users are
-- already under the canonical dept ids), so no user migration is needed.
-- role_assignment owners already live in the canonical depts.

DO $$
BEGIN
    -- =========================================================================
    -- 1. Math and Sciences (id=21) → Math and Science (id=8)
    -- =========================================================================
    UPDATE strategy
    SET department_id = 8,
        title = 'Math and Science 2022-2027 Departmental Strategic Plan'
    WHERE id = 12;

    -- Goal titles for MAS are verbatim source content — leave them unchanged.

    DELETE FROM department WHERE id = 21;

    -- =========================================================================
    -- 2. Mechanical and Industrial Engineering (id=17) → Mechanical Engineering (id=2)
    -- =========================================================================
    UPDATE strategy
    SET department_id = 2,
        title = 'Mechanical Engineering 2022-2027 Departmental Strategic Plan'
    WHERE id = 6;

    UPDATE goal
    SET title = 'Mechanical Engineering Strategic Objectives 2022-2027'
    WHERE id = 15;

    DELETE FROM department WHERE id = 17;

    -- =========================================================================
    -- 3. EE and Computing (id=15) → Electrical Engineering (id=3)
    -- =========================================================================
    UPDATE strategy
    SET department_id = 3,
        title = 'Electrical Engineering 2022-2027 Departmental Strategic Plan'
    WHERE id = 4;

    UPDATE goal
    SET title = 'Electrical Engineering Strategic Objectives 2022-2027'
    WHERE id = 13;

    DELETE FROM department WHERE id = 15;

    RAISE NOTICE 'V21: 3 duplicate departments merged into their canonical counterparts.';
END $$;
