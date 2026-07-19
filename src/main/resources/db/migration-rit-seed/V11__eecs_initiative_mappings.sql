-- V11: Add initiative mappings for EECS department
--
-- Source: 22-27-Strategic plan mapping-AR Tracking.xlsx (A&R, OCE sheets)
-- EECS is labelled "EE-Comp" in the university tracking sheet's department rows.
--
-- Objective mappings already complete for strategy 4 (EECS) from V6 seed.
-- Only initiative mappings are added here.
--
-- Department covered: EECS (strategy 4, ini 65-73)

DO $$
DECLARE
    v_uni_ini_2  BIGINT;   -- Provide faculty with diverse PD opportunities
    v_uni_ini_4  BIGINT;   -- Establish vibrant Academic Development unit
    v_uni_ini_7  BIGINT;   -- Increase support for faculty research
    v_uni_ini_8  BIGINT;   -- Maintain and expand local and international accreditations
    v_uni_ini_12 BIGINT;   -- Formalize innovative teaching models
    v_uni_ini_14 BIGINT;   -- Introduce new programs and course offerings
    v_uni_ini_22 BIGINT;   -- Each Academic department is engaged in at least one event per term

    v_eecs_ini_65 BIGINT;  v_eecs_ini_66 BIGINT;  v_eecs_ini_67 BIGINT;  v_eecs_ini_68 BIGINT;
    v_eecs_ini_69 BIGINT;  v_eecs_ini_70 BIGINT;  v_eecs_ini_71 BIGINT;  v_eecs_ini_72 BIGINT;
    v_eecs_ini_73 BIGINT;
BEGIN
    SELECT id INTO v_uni_ini_2  FROM initiative WHERE title LIKE 'Provide faculty members with diverse professional%' LIMIT 1;
    SELECT id INTO v_uni_ini_4  FROM initiative WHERE title LIKE 'Establish a vibrant Academic Development%'           LIMIT 1;
    SELECT id INTO v_uni_ini_7  FROM initiative WHERE title LIKE 'Increase support for faculty research%'             LIMIT 1;
    SELECT id INTO v_uni_ini_8  FROM initiative WHERE title LIKE 'Maintain and expand local and international accreditations' LIMIT 1;
    SELECT id INTO v_uni_ini_12 FROM initiative WHERE title LIKE 'Formalize innovative teaching models%'              LIMIT 1;
    SELECT id INTO v_uni_ini_14 FROM initiative WHERE title LIKE 'Introduce new programs and course offerings'        LIMIT 1;
    SELECT id INTO v_uni_ini_22 FROM initiative WHERE title LIKE 'Each Academic department is engaged in at least one event%' LIMIT 1;

    SELECT id INTO v_eecs_ini_65 FROM initiative WHERE title LIKE 'Advocate by example teaching rigor%Maintain alignment%'   LIMIT 1;
    SELECT id INTO v_eecs_ini_66 FROM initiative WHERE title LIKE 'Create research groups with focus on themes%health care%'  LIMIT 1;
    SELECT id INTO v_eecs_ini_67 FROM initiative WHERE title LIKE 'Create a new framework for the capstone%improve faculty and student engagement%' LIMIT 1;
    SELECT id INTO v_eecs_ini_68 FROM initiative WHERE title LIKE 'Promote better research production evidenced by quality publications%' LIMIT 1;
    SELECT id INTO v_eecs_ini_69 FROM initiative WHERE title LIKE 'Engagements in the feasibility studies%developing new minors%concentrations%' LIMIT 1;
    SELECT id INTO v_eecs_ini_70 FROM initiative WHERE title LIKE 'Expand RIT Dubai degree offerings in ICT by adding new degrees%' LIMIT 1;
    SELECT id INTO v_eecs_ini_71 FROM initiative WHERE title LIKE 'Maintain attrition to below 10%%. Promote efficient student advising%' LIMIT 1;
    SELECT id INTO v_eecs_ini_72 FROM initiative WHERE title LIKE 'Secure the BSEE accreditation with ABET%'                 LIMIT 1;
    SELECT id INTO v_eecs_ini_73 FROM initiative WHERE title LIKE 'Add one degree in the ICT domain within the next 2 years%' LIMIT 1;

    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_eecs_ini_65, v_uni_ini_12); -- teaching rigor/alignment → formalize innovative teaching models
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_eecs_ini_66, v_uni_ini_2);  -- research groups/faculty mentors → faculty PD opportunities
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_eecs_ini_67, v_uni_ini_2);  -- capstone framework/committees → faculty PD opportunities
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_eecs_ini_68, v_uni_ini_7);  -- research production/publications → faculty research support
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_eecs_ini_69, v_uni_ini_22); -- feasibility studies/accreditation engagement → dept engaged in event per term
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_eecs_ini_70, v_uni_ini_14); -- expand degree offerings in ICT → new programs/course offerings
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_eecs_ini_71, v_uni_ini_4);  -- attrition/advising → academic development unit
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_eecs_ini_72, v_uni_ini_8);  -- BSEE/MSEE/CIT accreditations → maintain accreditations
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_eecs_ini_73, v_uni_ini_14); -- add ICT degree → new programs/course offerings

    RAISE NOTICE 'V11 EECS: initiative mappings inserted.';
END $$;
