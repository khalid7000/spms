-- V22: Restructure BUS strategy 7 so each objective has its own goal.
--
-- Before: 1 goal ("Business Management Strategic Objectives 2022-2027")
--         containing all 10 objectives.
-- After:  10 goals, each titled identically to its single child objective,
--         one objective per goal (matching the MAS/MnS reference pattern).
--
-- All objectives, initiatives, measurements, achievements, and mappings
-- keep their existing IDs — only goal_id on each objective changes.

DO $$
DECLARE
    v_old_goal_id BIGINT;
    v_obj         RECORD;
    v_new_goal_id BIGINT;
    v_count       INT := 0;
BEGIN
    SELECT id INTO v_old_goal_id FROM goal WHERE strategy_id = 7;

    FOR v_obj IN
        SELECT id, title, sort_order
        FROM objective
        WHERE goal_id = v_old_goal_id
        ORDER BY sort_order
    LOOP
        INSERT INTO goal (strategy_id, title, sort_order, created_by)
        VALUES (7, v_obj.title, v_obj.sort_order, 1)
        RETURNING id INTO v_new_goal_id;

        UPDATE objective SET goal_id = v_new_goal_id WHERE id = v_obj.id;

        v_count := v_count + 1;
    END LOOP;

    -- Original goal now has no objectives; safe to delete.
    DELETE FROM goal WHERE id = v_old_goal_id;

    RAISE NOTICE 'V22: BUS strategy 7 split into % goals, one per objective.', v_count;
END $$;
