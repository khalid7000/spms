-- V18: Add objective and initiative mappings for LA (Liberal Arts) department
--
-- Source: 22-27-Strategic plan mapping-AR Tracking.xlsx (Academic & Research,
-- Innovation & Entrepreneurship, Outreach & Community Engagement sheets).
-- LA is labelled "LA:" in the university tracking sheet's department rows.
--
-- Tracking sheet block analysis (matched by row block; LA's designated row
-- offset within several blocks was blank or duplicated, so semantic content
-- match was used where the sheet position was ambiguous):
--   A&R block (Introduce new programs and course offerings)              -> LA obj_115 / ini_162 (new BS in Advertising and PR)
--   A&R block (Each program helps co-op office w/ industry connection)   -> LA obj_118 / ini_165 (industry relationships)
--   A&R block (Establish a vibrant Academic Development unit)            -> LA obj_119 / ini_166 (ASC retention workshops)
--   A&R block (Increase support for faculty research, CAA rank)          -> LA obj_120 / ini_167 (faculty research)
--   OCE block (Each department holds 1 k-12 event/yr in Innovation Ctr)  -> LA obj_121 / ini_168 (annual Psych/Advertising promo event)
--   OCE block (Each Academic dept engaged in >=1 event per term)         -> LA obj_122 / ini_169 (K-12/university bridge talks)
--   I&E block (Collaborate with admission on sponsoring k-12 event)      -> LA obj_127 / ini_174 (K-12 admission event)
--   I&E block (Each department holds 1 k-12 event/yr in Innovation Ctr)  -> LA obj_127 / ini_175 (Innovation Center K-12 event)
--   I&E block (Funds identified by center / Innovation Journey deploy)   -> LA obj_128 / ini_176 (Innovation/Entrepreneurship Journey review)
--
-- Department covered: LA (strategy 13, obj 115-128, ini 162-176).
-- Objectives 116, 117, 123, 124, 125, 126 (GenEd courses, minors, internal
-- PD, internal/cross-campus PREP, Creative Writing) have no corresponding
-- row in the university tracking sheet and are left unmapped.

DO $$
DECLARE
    v_uni_obj_2  BIGINT;   -- Achieve 90% employability for graduated students
    v_uni_obj_3  BIGINT;   -- Maintain attrition to below 10%
    v_uni_obj_4  BIGINT;   -- Increase RIT Dubai rank score in the UAE MOE risk assessment system
    v_uni_obj_7  BIGINT;   -- Enhance curriculum with innovative and new material and teaching models
    v_uni_obj_8  BIGINT;   -- Increase number of new RIT Dubai student/faculty launched businesses via Innovation Center
    v_uni_obj_9  BIGINT;   -- Funding of start-up should grow by 10% year on year
    v_uni_obj_11 BIGINT;   -- Engage the Innovation Center with community event and activities
    v_uni_obj_16 BIGINT;   -- Active annual participation in admission initiatives

    v_uni_ini_3  BIGINT;   -- Each program helps co-op office with at least one industry connection every year
    v_uni_ini_4  BIGINT;   -- Establish a vibrant Academic Development unit
    v_uni_ini_7  BIGINT;   -- Increase support for faculty research giving priority to CAA rank parameters
    v_uni_ini_14 BIGINT;   -- Introduce new programs and course offerings
    v_uni_ini_15 BIGINT;   -- Funds identified by center / Improve deployment of Innovation Journey
    v_uni_ini_16 BIGINT;   -- Collaborate with admission on sponsoring an event with a k-12 school
    v_uni_ini_17 BIGINT;   -- Each department holds at least one k-12 event per year in the Innovation Center (I&E sheet, obj_9)
    v_uni_ini_21 BIGINT;   -- Each department holds at least one k-12 event per year in the Innovation Center (OCE sheet, obj_11)
    v_uni_ini_26 BIGINT;   -- Each Academic department is engaged in at least one event per term
BEGIN
    SELECT id INTO v_uni_obj_2  FROM objective WHERE title LIKE 'Achieve 90% employability%'                       LIMIT 1;
    SELECT id INTO v_uni_obj_3  FROM objective WHERE title LIKE 'Maintain attrition to below 10%'                  LIMIT 1;
    SELECT id INTO v_uni_obj_4  FROM objective WHERE title LIKE 'Increase RIT Dubai rank score%'                    LIMIT 1;
    SELECT id INTO v_uni_obj_7  FROM objective WHERE title LIKE 'Enhance curriculum with innovative%'                LIMIT 1;
    SELECT id INTO v_uni_obj_8  FROM objective WHERE title LIKE 'Increase number of new RIT Dubai student%'         LIMIT 1;
    SELECT id INTO v_uni_obj_9  FROM objective WHERE title LIKE 'Funding of start-up should grow%'                  LIMIT 1;
    SELECT id INTO v_uni_obj_11 FROM objective WHERE title LIKE 'Engage the Innovation Center with community event%' LIMIT 1;
    SELECT id INTO v_uni_obj_16 FROM objective WHERE title LIKE 'Active annual participation in admission%'         LIMIT 1;

    SELECT id INTO v_uni_ini_3  FROM initiative WHERE title LIKE 'Each program helps co-op office with at least one industry connection%' LIMIT 1;
    SELECT id INTO v_uni_ini_4  FROM initiative WHERE title LIKE 'Establish a vibrant Academic Development%'        LIMIT 1;
    SELECT id INTO v_uni_ini_7  FROM initiative WHERE title LIKE 'Increase support for faculty research%'           LIMIT 1;
    SELECT id INTO v_uni_ini_14 FROM initiative WHERE title LIKE 'Introduce new programs and course offerings'      LIMIT 1;
    SELECT id INTO v_uni_ini_15 FROM initiative WHERE title LIKE 'Funds are identified by the center%'              LIMIT 1;
    SELECT id INTO v_uni_ini_16 FROM initiative WHERE title LIKE 'Collaborate with admission on sponsoring an event%' LIMIT 1;

    SELECT i.id INTO v_uni_ini_17
    FROM initiative i JOIN objective o ON i.objective_id = o.id
    WHERE i.title LIKE 'Each department holds at least one k-12 event%' AND o.id = v_uni_obj_9 LIMIT 1;
    SELECT i.id INTO v_uni_ini_21
    FROM initiative i JOIN objective o ON i.objective_id = o.id
    WHERE i.title LIKE 'Each department holds at least one k-12 event%' AND o.id = v_uni_obj_11 LIMIT 1;

    SELECT id INTO v_uni_ini_26 FROM initiative WHERE title LIKE 'Each Academic department is engaged in at least one event per term%' LIMIT 1;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (115, v_uni_obj_7);  -- new BS program → enhance curriculum
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (118, v_uni_obj_2);  -- industry relationships → 90% employability
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (119, v_uni_obj_3);  -- ASC retention → attrition below 10%
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (120, v_uni_obj_4);  -- faculty research → RIT Dubai rank score
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (121, v_uni_obj_11); -- annual promo event → Innovation Center community engagement
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (122, v_uni_obj_16); -- K-12/university bridge → admission initiatives participation
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (127, v_uni_obj_9);  -- K-12 engagement objective → start-up funding growth
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (128, v_uni_obj_8);  -- Innovation/Entrepreneurship Journey → Innovation Center businesses growth

    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (162, v_uni_ini_14); -- new BS program → introduce new programs/courses
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (165, v_uni_ini_3);  -- industry relationships → co-op industry connection
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (166, v_uni_ini_4);  -- ASC workshops → Academic Development unit
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (167, v_uni_ini_7);  -- faculty research output → faculty research support
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (168, v_uni_ini_21); -- annual promo event → dept k-12 event (OCE)
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (169, v_uni_ini_26); -- K-12/university bridge talks → dept engaged in event per term
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (174, v_uni_ini_16); -- K-12 admission event → collaborate with admission on k-12 event
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (175, v_uni_ini_17); -- Innovation Center K-12 event → dept k-12 event (I&E)
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (176, v_uni_ini_15); -- Innovation/Entrepreneurship Journey review → Innovation Journey deployment

    RAISE NOTICE 'V18 LA: objective mappings (8) and initiative mappings (9) inserted.';
END $$;
