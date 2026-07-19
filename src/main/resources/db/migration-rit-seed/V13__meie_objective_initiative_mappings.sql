-- V13: Add objective and initiative mappings for MEIE department
--
-- Source: 22-27-Strategic plan mapping-AR Tracking.xlsx (A&R, OCE sheets)
-- MEIE is labelled "MEIE" in the university tracking sheet's department rows.
--
-- A&R sheet analysis for "MEIE:" rows:
--   Block (Faculty research)  -> MEIE: "Develop research groups and labs..."
--   Block (IT automate)       -> MEIE: "Streamline admin processes...Automate PLO assessment..."
--   OCE Block (each dept in event) -> MEIE: "Enhance Student for a Day workshops..."
--
-- Department covered: MEIE (strategy 6, obj 67-71, ini 78-82)
-- V6 already mapped obj_67, obj_70, obj_71; obj_68 and obj_69 missing.

DO $$
DECLARE
    v_uni_obj_2  BIGINT;   -- Achieve 90% employability
    v_uni_obj_16 BIGINT;   -- Active annual participation in admission initiatives

    v_uni_ini_2  BIGINT;   -- Provide faculty with diverse PD opportunities
    v_uni_ini_7  BIGINT;   -- Increase support for faculty research
    v_uni_ini_11 BIGINT;   -- Increase use of IT to streamline tasks
    v_uni_ini_12 BIGINT;   -- Formalize innovative teaching models
    v_uni_ini_22 BIGINT;   -- Each Academic department is engaged in at least one event per term

    v_meie_obj_68 BIGINT;  v_meie_obj_69 BIGINT;
    v_meie_ini_78 BIGINT;  v_meie_ini_79 BIGINT;  v_meie_ini_80 BIGINT;
    v_meie_ini_81 BIGINT;  v_meie_ini_82 BIGINT;
BEGIN
    SELECT id INTO v_uni_obj_2  FROM objective WHERE title LIKE 'Achieve 90% employability%'        LIMIT 1;
    SELECT id INTO v_uni_obj_16 FROM objective WHERE title LIKE 'Active annual participation in admission%' LIMIT 1;

    SELECT id INTO v_uni_ini_2  FROM initiative WHERE title LIKE 'Provide faculty members with diverse professional%' LIMIT 1;
    SELECT id INTO v_uni_ini_7  FROM initiative WHERE title LIKE 'Increase support for faculty research%'             LIMIT 1;
    SELECT id INTO v_uni_ini_11 FROM initiative WHERE title LIKE 'Increase use of IT to streamline%'                  LIMIT 1;
    SELECT id INTO v_uni_ini_12 FROM initiative WHERE title LIKE 'Formalize innovative teaching models%'              LIMIT 1;
    SELECT id INTO v_uni_ini_22 FROM initiative WHERE title LIKE 'Each Academic department is engaged in at least one event%' LIMIT 1;

    SELECT id INTO v_meie_obj_68 FROM objective WHERE title LIKE 'Enhance outreach workshops%increase faculty involvement in open days%'             LIMIT 1;
    SELECT id INTO v_meie_obj_69 FROM objective WHERE title LIKE 'Develop faculty mentorship program%explore funding sources%increase support staff' LIMIT 1;

    SELECT id INTO v_meie_ini_78 FROM initiative WHERE title LIKE 'Enhance the curriculum to incorporate new industry-relevant%'                      LIMIT 1;
    SELECT id INTO v_meie_ini_79 FROM initiative WHERE title LIKE 'Enhance the current "Student for a Day" workshops%'                                LIMIT 1;
    SELECT id INTO v_meie_ini_80 FROM initiative WHERE title LIKE 'Develop faculty mentorship program. Explore various funding sources%'              LIMIT 1;
    SELECT id INTO v_meie_ini_81 FROM initiative WHERE title LIKE 'Streamline admin processes to reduce workload%Assign and recognize faculty admin%' LIMIT 1;
    SELECT id INTO v_meie_ini_82 FROM initiative WHERE title LIKE 'Develop research groups and labs with state-of-the-art equipment%'                 LIMIT 1;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_meie_obj_68, v_uni_obj_16); -- outreach/open days → active admission participation
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_meie_obj_69, v_uni_obj_2);  -- faculty mentorship/funding → employability

    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_meie_ini_78, v_uni_ini_12); -- curriculum/innovative teaching → formalize innovative teaching models
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_meie_ini_79, v_uni_ini_22); -- Student for a Day/open days → dept engaged in event per term
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_meie_ini_80, v_uni_ini_2);  -- faculty mentorship → faculty PD opportunities
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_meie_ini_81, v_uni_ini_11); -- streamline admin/automate PLO → increase use of IT
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_meie_ini_82, v_uni_ini_7);  -- research groups/labs → faculty research support

    RAISE NOTICE 'V13 MEIE: objective mappings (2 new) and initiative mappings (5) inserted.';
END $$;
