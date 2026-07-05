-- V12: Add objective and initiative mappings for Academic Advising department
--
-- Source: 22-27-Strategic plan mapping-AR Tracking.xlsx (A&R sheet)
-- Academic Advising is labelled "Advising" in the university tracking sheet's
-- department rows.
--
-- A&R sheet analysis for "Advising:" rows:
--   Block (Academic Dev, target attrition)  -> Advising: "Increase availability and responsiveness of advisors..."
--   Block (Early Alert)                     -> Advising: "Develop a program of support for students on Probation..."
--   Block (Target freshman students)        -> Advising: "Offer greater support for graduate students..."
--   Block (IT automate)                     -> Advising: "Develop online repository of advising resources..."
--
-- Department covered: Academic Advising (strategy 5, obj 63-66, ini 74-77)
-- Currently 0 of either mapping exist for this department.

DO $$
DECLARE
    v_uni_obj_3  BIGINT;   -- Maintain attrition below 10%
    v_uni_obj_6  BIGINT;   -- Automate repetitive internal processes

    v_uni_ini_4  BIGINT;   -- Establish vibrant Academic Development unit
    v_uni_ini_5  BIGINT;   -- More effective use of Early Alert
    v_uni_ini_6  BIGINT;   -- Target freshman students with program specific events
    v_uni_ini_11 BIGINT;   -- Increase use of IT to streamline tasks

    v_adv_obj_63 BIGINT;  v_adv_obj_64 BIGINT;  v_adv_obj_65 BIGINT;  v_adv_obj_66 BIGINT;
    v_adv_ini_74 BIGINT;  v_adv_ini_75 BIGINT;  v_adv_ini_76 BIGINT;  v_adv_ini_77 BIGINT;
BEGIN
    SELECT id INTO v_uni_obj_3  FROM objective WHERE title LIKE 'Maintain attrition%'           LIMIT 1;
    SELECT id INTO v_uni_obj_6  FROM objective WHERE title LIKE 'Automate repetitive internal%'  LIMIT 1;

    SELECT id INTO v_uni_ini_4  FROM initiative WHERE title LIKE 'Establish a vibrant Academic Development%' LIMIT 1;
    SELECT id INTO v_uni_ini_5  FROM initiative WHERE title LIKE 'More effective use of Early Alert%'        LIMIT 1;
    SELECT id INTO v_uni_ini_6  FROM initiative WHERE title LIKE 'Target freshman students with program specific%' LIMIT 1;
    SELECT id INTO v_uni_ini_11 FROM initiative WHERE title LIKE 'Increase use of IT to streamline%'         LIMIT 1;

    SELECT id INTO v_adv_obj_63 FROM objective WHERE title LIKE 'Offer greater support for graduate students%improve access%'     LIMIT 1;
    SELECT id INTO v_adv_obj_64 FROM objective WHERE title LIKE 'Develop%program of support for students on Probation%Suspension%' LIMIT 1;
    SELECT id INTO v_adv_obj_65 FROM objective WHERE title LIKE 'Develop online repository of student advising resources%'        LIMIT 1;
    SELECT id INTO v_adv_obj_66 FROM objective WHERE title LIKE 'Increase availability and responsiveness of advisors%hiring plan%' LIMIT 1;

    SELECT id INTO v_adv_ini_74 FROM initiative WHERE title LIKE 'Offer greater support for graduate students to understand%'      LIMIT 1;
    SELECT id INTO v_adv_ini_75 FROM initiative WHERE title LIKE 'Develop a program of support for students on Probation%'         LIMIT 1;
    SELECT id INTO v_adv_ini_76 FROM initiative WHERE title LIKE 'Develop an online repository of student advising resources%'     LIMIT 1;
    SELECT id INTO v_adv_ini_77 FROM initiative WHERE title LIKE 'Increase availability and responsiveness of advisors%faculty%staff%' LIMIT 1;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_adv_obj_63, v_uni_obj_3);  -- support for grad students → attrition
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_adv_obj_64, v_uni_obj_3);  -- probation support → attrition
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_adv_obj_65, v_uni_obj_6);  -- online repository/paperless → automate repetitive processes
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_adv_obj_66, v_uni_obj_3);  -- advisor availability → attrition

    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_adv_ini_74, v_uni_ini_6);  -- support grad students → target freshman students
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_adv_ini_75, v_uni_ini_5);  -- probation program → early alert
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_adv_ini_76, v_uni_ini_11); -- online repo/paperless → IT streamline
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_adv_ini_77, v_uni_ini_4);  -- advisor availability → academic development unit

    RAISE NOTICE 'V12 Academic Advising: objective mappings (4) and initiative mappings (4) inserted.';
END $$;
