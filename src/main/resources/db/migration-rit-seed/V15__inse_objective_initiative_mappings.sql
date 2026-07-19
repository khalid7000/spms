-- V15: Add objective and initiative mappings for INSE department
--
-- Source: 22-27-Strategic plan mapping-AR Tracking.xlsx (A&R, IE sheets)
-- INSE is labelled "IE" (Institutional Effectiveness) in the university
-- tracking sheet's department rows.
--
-- A&R/IE sheet analysis for "IE:" rows:
--   Block (institutional ranking/risk)  -> IE: "Collect, analyse, interpret, disseminate institutional data...benchmarking..."
--   Block (Accreditations)              -> IE: "Ensure compliance and alignment with accreditation requirements..."
--   Block (IT automate)                 -> IE: "Automate repetitive processes such as course folder review..."
--   Block (Innovative teaching/eval)    -> IE: "Enhance evaluation framework...academic programs...data insights..."
--   Block (New programs)                -> IE: "Prepare, submit, obtain accreditation documents for new programs..."
--
-- Department covered: INSE (strategy 8, obj 82-86, ini 93-97)
-- V6 already mapped obj_83, obj_84, obj_85, obj_86; obj_82 missing.
-- Zero initiative_mapping rows exist yet for strategy 8.

DO $$
DECLARE
    v_uni_obj_4  BIGINT;   -- Increase RIT Dubai rank score in UAE MOE risk assessment system

    v_uni_ini_8  BIGINT;   -- Maintain and expand local and international accreditations
    v_uni_ini_9  BIGINT;   -- Establish taskforce for the centers with KPIs
    v_uni_ini_11 BIGINT;   -- Increase use of IT to streamline tasks
    v_uni_ini_12 BIGINT;   -- Formalize innovative teaching models
    v_uni_ini_14 BIGINT;   -- Introduce new programs and course offerings

    v_inse_obj_82 BIGINT;

    v_inse_ini_93 BIGINT;  v_inse_ini_94 BIGINT;  v_inse_ini_95 BIGINT;
    v_inse_ini_96 BIGINT;  v_inse_ini_97 BIGINT;
BEGIN
    SELECT id INTO v_uni_obj_4  FROM objective WHERE title LIKE 'Increase RIT Dubai rank score%' LIMIT 1;

    SELECT id INTO v_uni_ini_8  FROM initiative WHERE title LIKE 'Maintain and expand local and international accreditations' LIMIT 1;
    SELECT id INTO v_uni_ini_9  FROM initiative WHERE title LIKE 'Establish taskforce for the centers with KPIs%'              LIMIT 1;
    SELECT id INTO v_uni_ini_11 FROM initiative WHERE title LIKE 'Increase use of IT to streamline%'                          LIMIT 1;
    SELECT id INTO v_uni_ini_12 FROM initiative WHERE title LIKE 'Formalize innovative teaching models%'                      LIMIT 1;
    SELECT id INTO v_uni_ini_14 FROM initiative WHERE title LIKE 'Introduce new programs and course offerings'                LIMIT 1;

    SELECT id INTO v_inse_obj_82 FROM objective WHERE title LIKE 'Collect, analyse, interpret, and disseminate institutional data for better decision-making%' LIMIT 1;

    SELECT id INTO v_inse_ini_93 FROM initiative WHERE title LIKE 'Collect, analyse, interpret, and disseminate institutional data in order to facilitate%' LIMIT 1;
    SELECT id INTO v_inse_ini_94 FROM initiative WHERE title LIKE 'Ensure compliance and alignment with accreditation requirements in a timely manner%'      LIMIT 1;
    SELECT id INTO v_inse_ini_95 FROM initiative WHERE title LIKE 'Automate repetitive processes such as the course folder review process%'                 LIMIT 1;
    SELECT id INTO v_inse_ini_96 FROM initiative WHERE title LIKE 'Enhance the evaluation framework for ongoing assessment and improvement, focusing%'        LIMIT 1;
    SELECT id INTO v_inse_ini_97 FROM initiative WHERE title LIKE 'Prepare, submit, and obtain accreditation documents for new programs%'                     LIMIT 1;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_inse_obj_82, v_uni_obj_4); -- institutional data/benchmarking → RIT Dubai rank score

    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_inse_ini_93, v_uni_ini_9);  -- institutional data/benchmarking → KPI taskforce
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_inse_ini_94, v_uni_ini_8);  -- accreditation compliance → maintain accreditations
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_inse_ini_95, v_uni_ini_11); -- automate course folder review → IT streamline
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_inse_ini_96, v_uni_ini_12); -- evaluation framework/programs → formalize innovative teaching models
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_inse_ini_97, v_uni_ini_14); -- accreditation docs for new programs → new programs/course offerings

    RAISE NOTICE 'V15 INSE: objective mappings (1 new) and initiative mappings (5) inserted.';
END $$;
