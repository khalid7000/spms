-- V14: Add objective and initiative mappings for IAD department
--
-- Source: 22-27-Strategic plan mapping-AR Tracking.xlsx (A&R, OCE, IE sheets)
-- IAD is labelled "BusMng" in the university tracking sheet's department rows.
--
-- A&R sheet analysis for "BusMng:" rows:
--   Block (faculty PD)        -> BusMng: "Curriculum Enhancement...faculty dev programs..."
--   Block (co-op industry)    -> BusMng: "Partnership Development...three new partnerships..."
--   Block (Academic Dev)      -> BusMng: "Diverse Teaching Strategies...Innovative Assessment..."
--   Block (Faculty research)  -> BusMng: "Research Culture...faculty research workshop..."
--   Block (Accreditations)    -> BusMng: "Maintain Existing Accreditations: 100% renewal..."
--   Block (Engage students)   -> BusMng: "Course Material Development...involve students..."
--   OCE Block (research coop) -> BusMng: "Collaboration Rates...10% active research projects..."
--   OCE Block (each dept event) -> BusMng: "Events...three major public events..."
--   IE Block (Innovation Center funds) -> BusMng: startup competition
--
-- Department covered: IAD (strategy 7, obj 72-81, ini 83-92)
-- V6 already mapped obj_72, obj_73, obj_75, obj_76; obj_74,77,78,79,80,81 missing.

DO $$
DECLARE
    v_uni_obj_2  BIGINT;   -- Achieve 90% employability
    v_uni_obj_5  BIGINT;   -- Establish research and innovation centers of excellence
    v_uni_obj_8  BIGINT;   -- Increase number of RIT Dubai businesses through Innovation Center
    v_uni_obj_16 BIGINT;   -- Active annual participation in admission initiatives

    v_uni_ini_2  BIGINT;   -- Provide faculty with diverse PD opportunities
    v_uni_ini_3  BIGINT;   -- Each program helps co-op with at least one industry connection
    v_uni_ini_4  BIGINT;   -- Establish vibrant Academic Development unit
    v_uni_ini_7  BIGINT;   -- Increase support for faculty research
    v_uni_ini_8  BIGINT;   -- Maintain and expand local and international accreditations
    v_uni_ini_10 BIGINT;   -- Engage students with faculty to improve teaching, research and service
    v_uni_ini_15 BIGINT;   -- Funds are identified by Innovation Center; application process established
    v_uni_ini_19 BIGINT;   -- Create 6 applied research/consultancy projects-coop
    v_uni_ini_22 BIGINT;   -- Each Academic department is engaged in at least one event per term

    v_iad_obj_74 BIGINT;  v_iad_obj_77 BIGINT;  v_iad_obj_78 BIGINT;
    v_iad_obj_79 BIGINT;  v_iad_obj_80 BIGINT;  v_iad_obj_81 BIGINT;

    v_iad_ini_83 BIGINT;  v_iad_ini_84 BIGINT;  v_iad_ini_85 BIGINT;  v_iad_ini_86 BIGINT;
    v_iad_ini_87 BIGINT;  v_iad_ini_88 BIGINT;  v_iad_ini_89 BIGINT;  v_iad_ini_90 BIGINT;
    v_iad_ini_91 BIGINT;  v_iad_ini_92 BIGINT;
BEGIN
    SELECT id INTO v_uni_obj_2  FROM objective WHERE title LIKE 'Achieve 90% employability%'                      LIMIT 1;
    SELECT id INTO v_uni_obj_5  FROM objective WHERE title LIKE 'Establish research and innovation centers%'      LIMIT 1;
    SELECT id INTO v_uni_obj_8  FROM objective WHERE title LIKE 'Increase number of new RIT Dubai student%'       LIMIT 1;
    SELECT id INTO v_uni_obj_16 FROM objective WHERE title LIKE 'Active annual participation in admission%'       LIMIT 1;

    SELECT id INTO v_uni_ini_2  FROM initiative WHERE title LIKE 'Provide faculty members with diverse professional%'         LIMIT 1;
    SELECT id INTO v_uni_ini_3  FROM initiative WHERE title LIKE 'Each program helps co-op office%'                           LIMIT 1;
    SELECT id INTO v_uni_ini_4  FROM initiative WHERE title LIKE 'Establish a vibrant Academic Development%'                  LIMIT 1;
    SELECT id INTO v_uni_ini_7  FROM initiative WHERE title LIKE 'Increase support for faculty research%'                     LIMIT 1;
    SELECT id INTO v_uni_ini_8  FROM initiative WHERE title LIKE 'Maintain and expand local and international accreditations' LIMIT 1;
    SELECT id INTO v_uni_ini_10 FROM initiative WHERE title LIKE 'Engage students with faculty to improve teaching%'          LIMIT 1;
    SELECT id INTO v_uni_ini_15 FROM initiative WHERE title LIKE 'Funds are identified by the center%'                        LIMIT 1;
    SELECT id INTO v_uni_ini_19 FROM initiative WHERE title LIKE 'Create 6 applied research/consultancy%'                     LIMIT 1;
    SELECT id INTO v_uni_ini_22 FROM initiative WHERE title LIKE 'Each Academic department is engaged in at least one event%' LIMIT 1;

    SELECT id INTO v_iad_obj_74 FROM objective WHERE title LIKE 'Course Material Development and Research Initiatives'       LIMIT 1;
    SELECT id INTO v_iad_obj_77 FROM objective WHERE title LIKE 'Interdisciplinary Collaboration and Incentivization'        LIMIT 1;
    SELECT id INTO v_iad_obj_78 FROM objective WHERE title LIKE 'Partnership Development and Co-op Engagement'              LIMIT 1;
    SELECT id INTO v_iad_obj_79 FROM objective WHERE title LIKE 'Start-up Competition Organization and Mentorship'          LIMIT 1;
    SELECT id INTO v_iad_obj_80 FROM objective WHERE title LIKE 'Events Organization and External Engagement'               LIMIT 1;
    SELECT id INTO v_iad_obj_81 FROM objective WHERE title LIKE 'Sustainability Initiatives and Reporting'                  LIMIT 1;

    SELECT id INTO v_iad_ini_83 FROM initiative WHERE title LIKE 'Review and update the curriculum at least once every academic year%'    LIMIT 1;
    SELECT id INTO v_iad_ini_84 FROM initiative WHERE title LIKE 'At least 60% of faculty implementing at least one diverse teaching%'    LIMIT 1;
    SELECT id INTO v_iad_ini_85 FROM initiative WHERE title LIKE 'Involve students in at least 15% of all course material development%'   LIMIT 1;
    SELECT id INTO v_iad_ini_86 FROM initiative WHERE title LIKE 'Ensure 100% renewal of existing accreditations%CAA%AACSB%'             LIMIT 1;
    SELECT id INTO v_iad_ini_87 FROM initiative WHERE title LIKE 'Organize%or ensure 80% of faculty participate in at least one research-focused workshop%' LIMIT 1;
    SELECT id INTO v_iad_ini_88 FROM initiative WHERE title LIKE 'At least 10% of all active research projects%involve collaboration%different departments%' LIMIT 1;
    SELECT id INTO v_iad_ini_89 FROM initiative WHERE title LIKE 'Establish a minimum of three new partnerships per year%local businesses%' LIMIT 1;
    SELECT id INTO v_iad_ini_90 FROM initiative WHERE title LIKE 'Organize at least one startup competition per academic year%'             LIMIT 1;
    SELECT id INTO v_iad_ini_91 FROM initiative WHERE title LIKE 'Organize at least three major public events each academic year%'          LIMIT 1;
    SELECT id INTO v_iad_ini_92 FROM initiative WHERE title LIKE 'Launch at least two new sustainability initiatives%'                      LIMIT 1;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_iad_obj_74, v_uni_obj_5);  -- course material dev/student research → research centers
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_iad_obj_77, v_uni_obj_5);  -- interdisciplinary collaboration → research centers
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_iad_obj_78, v_uni_obj_2);  -- partnership/co-op → employability
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_iad_obj_79, v_uni_obj_8);  -- startup competition/mentorship → RIT Dubai Innovation Center businesses
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_iad_obj_80, v_uni_obj_16); -- events/external engagement → active admission participation
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_iad_obj_81, v_uni_obj_16); -- sustainability initiatives → active admission participation

    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_iad_ini_83, v_uni_ini_2);  -- curriculum/faculty dev → faculty PD opportunities
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_iad_ini_84, v_uni_ini_4);  -- diverse teaching/assessment → academic development unit
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_iad_ini_85, v_uni_ini_10); -- students in course material dev → engage students with faculty
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_iad_ini_86, v_uni_ini_8);  -- renew accreditations → maintain accreditations
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_iad_ini_87, v_uni_ini_7);  -- research workshop/publications → faculty research support
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_iad_ini_88, v_uni_ini_19); -- interdisciplinary research → research/consultancy coop
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_iad_ini_89, v_uni_ini_3);  -- new partnerships/coop placements → each program helps co-op
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_iad_ini_90, v_uni_ini_15); -- startup competition → Innovation Center
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_iad_ini_91, v_uni_ini_22); -- public events → dept engaged in event per term
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_iad_ini_92, v_uni_ini_22); -- sustainability events → dept engaged in event per term

    RAISE NOTICE 'V14 IAD: objective mappings (6 new) and initiative mappings (10) inserted.';
END $$;
