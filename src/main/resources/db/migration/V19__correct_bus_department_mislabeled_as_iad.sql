-- V19: Correct department mislabeling discovered while importing the Business
-- Management (BUS) department sheet.
--
-- Investigation: BUS's own source sheet ("Bus - Copy.xlsx") contains 10
-- department vision/goal rows, each carrying an explicit "Map to: <sheet> Q<n>"
-- annotation written by the department itself. Comparing this sheet's content
-- against the already-deployed strategy id=7 (seeded in V6, mapped in V14)
-- shows the two are verbatim identical:
--   - Strategy 7's 10 objective titles ("Curriculum Enhancement and Teaching
--     Quality Improvement", "Course Material Development and Research
--     Initiatives", "Start-up Competition Organization and Mentorship", etc.)
--     are abbreviations of BUS's own initiative-column headers, in the same
--     order as BUS's R2-R11 rows.
--   - Strategy 7's achievement text references "MGMT489", "QFEmirates",
--     AACSB/CAA accreditation, and faculty names (Kokkalis, Klincar, Tahir,
--     Strate) that are BUS-specific (Kokkalis = BUS department head, user 18;
--     Klincar/Tahir/Strate are BUS faculty, department_id=9).
--   - V14's header comment claims "IAD is labelled 'BusMng' in the university
--     tracking sheet" and maps strategy 7's objectives/initiatives against
--     the tracking sheet's "BusMng:" rows -- which is correct AS LABELLED,
--     but the underlying strategy was incorrectly attached to department_id
--     11 (IAD, "Interactive Arts and Design") instead of department_id 9
--     (BUS, "Business Management"). "BusMng" abbreviates Business Management,
--     not Interactive Arts and Design.
--
-- Conclusion: strategy id=7 (10 objectives, 10 initiatives, 30 achievements,
-- 10 objective_mappings, 10 initiative_mappings, all already deployed via
-- V6/V14) is genuinely the Business Management department's 2022-2027 plan.
-- No new seed or mapping data is needed for BUS -- it already exists, just
-- mislabeled. This migration relabels strategy 7 to BUS and reassigns its
-- ownership from the IAD faculty member it was mistakenly assigned to, to
-- BUS's actual department head.
--
-- Note: this leaves IAD (department_id=11) with zero strategy data of its
-- own -- a real, separate gap (like LA's was before V17/V18) that should be
-- addressed with a full from-scratch IAD seed in a future migration.

DO $$
DECLARE
    v_bus_dept_id BIGINT;
    v_bus_head_id BIGINT;
BEGIN
    SELECT id INTO v_bus_dept_id FROM department WHERE code = 'BUS';
    SELECT head_user_id INTO v_bus_head_id FROM department WHERE code = 'BUS';

    UPDATE strategy
    SET department_id = v_bus_dept_id,
        title = 'Business Management 2022-2027 Departmental Strategic Plan'
    WHERE id = 7;

    UPDATE goal
    SET title = 'Business Management Strategic Objectives 2022-2027'
    WHERE strategy_id = 7;

    UPDATE role_assignment
    SET user_id = v_bus_head_id
    WHERE strategy_id = 7 AND user_id = 111;

    RAISE NOTICE 'V19: strategy 7 relabeled from IAD to BUS (department_id=%, owner=%).', v_bus_dept_id, v_bus_head_id;
END $$;
