-- V10: Add objective and initiative mappings for DSAI department
--
-- Source: 22-27-Strategic plan mapping-AR Tracking.xlsx (A&R, OCE, IE sheets)
--         plus DSAI department vision actions file (DB-stored as "GPR" rows in tracking sheet)
--
-- Method: The A&R, OCE, and IE sheets each have blocks of ~10 rows corresponding
-- to one university initiative. Each block contains one row per department.
-- The text in that row identifies which dept goal maps to that university
-- initiative block, allowing initiative-level mapping.
--
-- Note: DSAI is labelled "GPR" (Graduate Programs) in the university tracking
-- sheet's department rows, so GPR rows = DSAI department rows. Verified DB
-- title text for strategy 3 (DSAI) below matches GPR-style content exactly.
--
-- Department covered: DSAI (strategy 3, obj 20-53, ini 31-64)

DO $$
DECLARE
    -- University objective IDs
    v_uni_obj_2  BIGINT;   -- Achieve 90% employability
    v_uni_obj_4  BIGINT;   -- Increase RIT Dubai rank score in UAE MoE
    v_uni_obj_8  BIGINT;   -- Increase number of RIT Dubai businesses through Innovation Center
    v_uni_obj_10 BIGINT;   -- Meet 10 Gov entities to promote courses (OCE goal 1 obj 1)
    v_uni_obj_11 BIGINT;   -- Engage Innovation Center with community (OCE goal 1 obj 2)
    v_uni_obj_12 BIGINT;   -- Raise interns/coop quality
    v_uni_obj_13 BIGINT;   -- Engage with industry and gov partners
    v_uni_obj_14 BIGINT;   -- Establish Alumni Club
    v_uni_obj_16 BIGINT;   -- Active annual participation in admission initiatives
    v_uni_obj_17 BIGINT;   -- Support initiatives engaging faculty with outside entities

    -- University initiative IDs
    v_uni_ini_1  BIGINT;   -- More engagement with Alumni
    v_uni_ini_7  BIGINT;   -- Increase support for faculty research
    v_uni_ini_8  BIGINT;   -- Maintain and expand local and international accreditations
    v_uni_ini_9  BIGINT;   -- Establish taskforce for the centers with KPIs
    v_uni_ini_10 BIGINT;   -- Engage students with faculty to improve teaching, research and service
    v_uni_ini_11 BIGINT;   -- Increase use of IT to streamline tasks
    v_uni_ini_12 BIGINT;   -- Formalize innovative teaching models
    v_uni_ini_15 BIGINT;   -- Funds are identified by Innovation Center; application process established
    v_uni_ini_16 BIGINT;   -- Collaborate with admission on sponsoring event with k-12 school
    v_uni_ini_17 BIGINT;   -- Each department holds at least one k-12 event per year in Innovation Center
    v_uni_ini_18 BIGINT;   -- Utilize RIT365 and extra coaching sessions to raise interns/coop quality
    v_uni_ini_19 BIGINT;   -- Create 6 applied research/consultancy projects-coop
    v_uni_ini_20 BIGINT;   -- Keep Alumni data completed and accurate
    v_uni_ini_22 BIGINT;   -- Each Academic department is engaged in at least one event per term
    v_uni_ini_23 BIGINT;   -- Meet 10 Gov. entities to promote courses (OCE goal 5)
    v_uni_ini_24 BIGINT;   -- Target 2 event sponsors
    v_uni_ini_25 BIGINT;   -- Facilitate engagement of degree programs with all programs like EE&C
    v_uni_ini_26 BIGINT;   -- Academic programs engage with all programs like EE&C promoting executive education

    -- DSAI department objective IDs (obj 20-53)
    v_dsai_obj_21 BIGINT;  v_dsai_obj_25 BIGINT;  v_dsai_obj_27 BIGINT;  v_dsai_obj_29 BIGINT;
    v_dsai_obj_30 BIGINT;  v_dsai_obj_31 BIGINT;  v_dsai_obj_32 BIGINT;  v_dsai_obj_33 BIGINT;
    v_dsai_obj_43 BIGINT;  v_dsai_obj_44 BIGINT;  v_dsai_obj_45 BIGINT;  v_dsai_obj_46 BIGINT;
    v_dsai_obj_47 BIGINT;  v_dsai_obj_48 BIGINT;  v_dsai_obj_49 BIGINT;  v_dsai_obj_50 BIGINT;
    v_dsai_obj_51 BIGINT;  v_dsai_obj_52 BIGINT;  v_dsai_obj_53 BIGINT;

    -- DSAI initiative IDs (ini 31-64)
    v_dsai_ini_31 BIGINT;  v_dsai_ini_32 BIGINT;  v_dsai_ini_33 BIGINT;  v_dsai_ini_34 BIGINT;
    v_dsai_ini_35 BIGINT;  v_dsai_ini_36 BIGINT;  v_dsai_ini_37 BIGINT;  v_dsai_ini_38 BIGINT;
    v_dsai_ini_39 BIGINT;  v_dsai_ini_40 BIGINT;  v_dsai_ini_41 BIGINT;  v_dsai_ini_42 BIGINT;
    v_dsai_ini_43 BIGINT;  v_dsai_ini_44 BIGINT;  v_dsai_ini_45 BIGINT;  v_dsai_ini_46 BIGINT;
    v_dsai_ini_47 BIGINT;  v_dsai_ini_48 BIGINT;  v_dsai_ini_49 BIGINT;  v_dsai_ini_50 BIGINT;
    v_dsai_ini_51 BIGINT;  v_dsai_ini_52 BIGINT;  v_dsai_ini_53 BIGINT;  v_dsai_ini_54 BIGINT;
    v_dsai_ini_55 BIGINT;  v_dsai_ini_56 BIGINT;  v_dsai_ini_57 BIGINT;  v_dsai_ini_58 BIGINT;
    v_dsai_ini_59 BIGINT;  v_dsai_ini_60 BIGINT;  v_dsai_ini_61 BIGINT;  v_dsai_ini_62 BIGINT;
    v_dsai_ini_63 BIGINT;  v_dsai_ini_64 BIGINT;

BEGIN
    -- Resolve university objective IDs
    SELECT id INTO v_uni_obj_2  FROM objective WHERE title LIKE 'Achieve 90% employability%'                 LIMIT 1;
    SELECT id INTO v_uni_obj_4  FROM objective WHERE title LIKE 'Increase RIT Dubai rank score%'              LIMIT 1;
    SELECT id INTO v_uni_obj_8  FROM objective WHERE title LIKE 'Increase number of new RIT Dubai student%'   LIMIT 1;
    SELECT id INTO v_uni_obj_10 FROM objective WHERE title LIKE 'Meet 10 Gov. entities to promote courses'    LIMIT 1;
    SELECT id INTO v_uni_obj_11 FROM objective WHERE title LIKE 'Engage the Innovation Center%'               LIMIT 1;
    SELECT id INTO v_uni_obj_12 FROM objective WHERE title LIKE 'Raise interns/coop quality'                  LIMIT 1;
    SELECT id INTO v_uni_obj_13 FROM objective WHERE title LIKE 'Engage with industry and government partners%' LIMIT 1;
    SELECT id INTO v_uni_obj_14 FROM objective WHERE title LIKE 'Establish Alumni Club'                       LIMIT 1;
    SELECT id INTO v_uni_obj_16 FROM objective WHERE title LIKE 'Active annual participation in admission%'   LIMIT 1;
    SELECT id INTO v_uni_obj_17 FROM objective WHERE title LIKE 'Support initiatives that engage faculty%'    LIMIT 1;

    -- Resolve university initiative IDs
    SELECT id INTO v_uni_ini_1  FROM initiative WHERE title LIKE 'More engagement with Alumni%'                                     LIMIT 1;
    SELECT id INTO v_uni_ini_7  FROM initiative WHERE title LIKE 'Increase support for faculty research%'                           LIMIT 1;
    SELECT id INTO v_uni_ini_8  FROM initiative WHERE title LIKE 'Maintain and expand local and international accreditations'       LIMIT 1;
    SELECT id INTO v_uni_ini_9  FROM initiative WHERE title LIKE 'Establish taskforce for the centers with KPIs'                    LIMIT 1;
    SELECT id INTO v_uni_ini_10 FROM initiative WHERE title LIKE 'Engage students with faculty to improve teaching%'                LIMIT 1;
    SELECT id INTO v_uni_ini_11 FROM initiative WHERE title LIKE 'Increase use of IT to streamline%'                                LIMIT 1;
    SELECT id INTO v_uni_ini_12 FROM initiative WHERE title LIKE 'Formalize innovative teaching models%'                            LIMIT 1;
    SELECT id INTO v_uni_ini_15 FROM initiative WHERE title LIKE 'Funds are identified by the center%'                              LIMIT 1;
    SELECT id INTO v_uni_ini_16 FROM initiative WHERE title LIKE 'Collaborate with admission on sponsoring%'                        LIMIT 1;
    SELECT id INTO v_uni_ini_17 FROM initiative WHERE title LIKE 'Each department holds at least one k-12 event%Innovation Center%' LIMIT 1;
    SELECT id INTO v_uni_ini_18 FROM initiative WHERE title LIKE 'Utilize RIT365 and extra coaching sessions%'                      LIMIT 1;
    SELECT id INTO v_uni_ini_19 FROM initiative WHERE title LIKE 'Create 6 applied research/consultancy%'                           LIMIT 1;
    SELECT id INTO v_uni_ini_20 FROM initiative WHERE title LIKE 'Keep Alumni data completed and accurate for at least 75%%'        LIMIT 1;
    SELECT id INTO v_uni_ini_22 FROM initiative WHERE title LIKE 'Each Academic department is engaged in at least one event%'       LIMIT 1;
    SELECT id INTO v_uni_ini_23 FROM initiative WHERE title LIKE 'Meet 10 Gov. entities to promote courses.'                        LIMIT 1;
    SELECT id INTO v_uni_ini_24 FROM initiative WHERE title LIKE 'Target 2 event sponsors%'                                         LIMIT 1;
    SELECT id INTO v_uni_ini_25 FROM initiative WHERE title LIKE 'Facilitate engagement of degree programs with all programs%'      LIMIT 1;
    SELECT id INTO v_uni_ini_26 FROM initiative WHERE title LIKE 'Academic programs engage with all programs%promoting%'            LIMIT 1;

    -- Resolve DSAI objective IDs (only those missing mappings after V6)
    SELECT id INTO v_dsai_obj_21 FROM objective WHERE title LIKE 'Ensure competitiveness of programs'                                          LIMIT 1;
    SELECT id INTO v_dsai_obj_25 FROM objective WHERE title LIKE 'Maintain high thesis completion rates'                                       LIMIT 1;
    SELECT id INTO v_dsai_obj_27 FROM objective WHERE title LIKE 'Ensure students'' proficiency in "writing" and "presentation"'               LIMIT 1;
    SELECT id INTO v_dsai_obj_29 FROM objective WHERE title LIKE 'Hold symposia or Innovation Days%publicize programs'                         LIMIT 1;
    SELECT id INTO v_dsai_obj_30 FROM objective WHERE title LIKE 'Create separate web pages for GPR%'                                          LIMIT 1;
    SELECT id INTO v_dsai_obj_31 FROM objective WHERE title LIKE 'Use Alumni as marketing tool'                                                LIMIT 1;
    SELECT id INTO v_dsai_obj_32 FROM objective WHERE title LIKE 'Create a database of bright and impactful students%'                         LIMIT 1;
    SELECT id INTO v_dsai_obj_33 FROM objective WHERE title LIKE 'Seek support from Advisory Board on student recruitment'                     LIMIT 1;
    SELECT id INTO v_dsai_obj_43 FROM objective WHERE title LIKE 'Build relationships with govt departments, industry, and NGOs via Advisory%' LIMIT 1;
    SELECT id INTO v_dsai_obj_44 FROM objective WHERE title LIKE 'Collect challenges from the advisory board members%'                         LIMIT 1;
    SELECT id INTO v_dsai_obj_45 FROM objective WHERE title LIKE 'Establish community outreach programs involving students%'                   LIMIT 1;
    SELECT id INTO v_dsai_obj_46 FROM objective WHERE title LIKE 'Integrate projects focused on social impact into%'                           LIMIT 1;
    SELECT id INTO v_dsai_obj_47 FROM objective WHERE title LIKE 'Collaborate with local educational institutions to offer workshops%data analytics%' LIMIT 1;
    SELECT id INTO v_dsai_obj_48 FROM objective WHERE title LIKE 'Engage students and faculty in research projects that address challenges%'   LIMIT 1;
    SELECT id INTO v_dsai_obj_49 FROM objective WHERE title LIKE 'Seek recognition and awards from local organizations%'                       LIMIT 1;
    SELECT id INTO v_dsai_obj_50 FROM objective WHERE title LIKE 'Invite students to innovation and entrepreneurship trainings%'               LIMIT 1;
    SELECT id INTO v_dsai_obj_51 FROM objective WHERE title LIKE 'Organize hackathons, data competitions, and coding challenges%'              LIMIT 1;
    SELECT id INTO v_dsai_obj_52 FROM objective WHERE title LIKE 'Invite entrepreneurs and innovators to deliver guest lectures%'              LIMIT 1;
    SELECT id INTO v_dsai_obj_53 FROM objective WHERE title LIKE 'Establish mentorship programs connecting students with industry professionals' LIMIT 1;

    -- Resolve DSAI initiative IDs (all)
    SELECT id INTO v_dsai_ini_31 FROM initiative WHERE title LIKE 'Keep curriculum current to incorporate cutting-edge skills'                  LIMIT 1;
    SELECT id INTO v_dsai_ini_32 FROM initiative WHERE title LIKE 'Ensure competitiveness of programs'                                          LIMIT 1;
    SELECT id INTO v_dsai_ini_33 FROM initiative WHERE title LIKE 'Maintain accreditation status'                                               LIMIT 1;
    SELECT id INTO v_dsai_ini_34 FROM initiative WHERE title LIKE 'Explore innovative teaching methods to keep up with educational trends'      LIMIT 1;
    SELECT id INTO v_dsai_ini_35 FROM initiative WHERE title LIKE 'Applying innovative teaching methods or tools'                               LIMIT 1;
    SELECT id INTO v_dsai_ini_36 FROM initiative WHERE title LIKE 'Maintain high thesis completion rates'                                       LIMIT 1;
    SELECT id INTO v_dsai_ini_37 FROM initiative WHERE title LIKE 'Maintain high quality of theses'                                             LIMIT 1;
    SELECT id INTO v_dsai_ini_38 FROM initiative WHERE title LIKE 'Ensure students'' proficiency in "writing" and "presentation"'               LIMIT 1;
    SELECT id INTO v_dsai_ini_39 FROM initiative WHERE title LIKE 'Maintain integrity standards by designing assessments resistant%'            LIMIT 1;
    SELECT id INTO v_dsai_ini_40 FROM initiative WHERE title LIKE 'Hold symposia or Innovation Days%'                                           LIMIT 1;
    SELECT id INTO v_dsai_ini_41 FROM initiative WHERE title LIKE 'Create separate web pages for GPR%'                                          LIMIT 1;
    SELECT id INTO v_dsai_ini_42 FROM initiative WHERE title LIKE 'Use Alumni as marketing tool'                                                LIMIT 1;
    SELECT id INTO v_dsai_ini_43 FROM initiative WHERE title LIKE 'Create a database of bright and impactful students%'                         LIMIT 1;
    SELECT id INTO v_dsai_ini_44 FROM initiative WHERE title LIKE 'Seek support from Advisory Board on student recruitment'                     LIMIT 1;
    SELECT id INTO v_dsai_ini_45 FROM initiative WHERE title LIKE 'Streamline scheduling for all 3 programs%'                                   LIMIT 1;
    SELECT id INTO v_dsai_ini_46 FROM initiative WHERE title LIKE 'Empower program coordinators'                                                LIMIT 1;
    SELECT id INTO v_dsai_ini_47 FROM initiative WHERE title LIKE 'Identify and address gaps in existing policies and procedures'               LIMIT 1;
    SELECT id INTO v_dsai_ini_48 FROM initiative WHERE title LIKE 'Through automation, improve course logistics%'                               LIMIT 1;
    SELECT id INTO v_dsai_ini_49 FROM initiative WHERE title LIKE 'Full-time faculty to publish at least one paper per year'                    LIMIT 1;
    SELECT id INTO v_dsai_ini_50 FROM initiative WHERE title LIKE 'Encourage faculty-student cooperation in converting theses%'                 LIMIT 1;
    SELECT id INTO v_dsai_ini_51 FROM initiative WHERE title LIKE 'Maintain high quality in theses and encourage student publication%'          LIMIT 1;
    SELECT id INTO v_dsai_ini_52 FROM initiative WHERE title LIKE 'Faculty to attend at least one conference per year%'                         LIMIT 1;
    SELECT id INTO v_dsai_ini_53 FROM initiative WHERE title LIKE 'Faculty to provide a list of research topics%'                               LIMIT 1;
    SELECT id INTO v_dsai_ini_54 FROM initiative WHERE title LIKE 'Build relationships with govt departments, industry, and NGOs%Advisory Board seats%' LIMIT 1;
    SELECT id INTO v_dsai_ini_55 FROM initiative WHERE title LIKE 'Collect challenges from the advisory board members to be converted to thesis%' LIMIT 1;
    SELECT id INTO v_dsai_ini_56 FROM initiative WHERE title LIKE 'Establish community outreach programs that involve students and faculty%data analytics%' LIMIT 1;
    SELECT id INTO v_dsai_ini_57 FROM initiative WHERE title LIKE 'Integrate projects focused on social impact%curriculum%'                     LIMIT 1;
    SELECT id INTO v_dsai_ini_58 FROM initiative WHERE title LIKE 'Collaborate with local educational institutions to offer workshops%data analytics%' LIMIT 1;
    SELECT id INTO v_dsai_ini_59 FROM initiative WHERE title LIKE 'Engage students and faculty in research projects that address specific challenges%' LIMIT 1;
    SELECT id INTO v_dsai_ini_60 FROM initiative WHERE title LIKE 'Seek recognition and awards from local organizations or authorities%'        LIMIT 1;
    SELECT id INTO v_dsai_ini_61 FROM initiative WHERE title LIKE 'Invite students to innovation and entrepreneurship trainings%'               LIMIT 1;
    SELECT id INTO v_dsai_ini_62 FROM initiative WHERE title LIKE 'Organize hackathons, data competitions, and coding challenges%'              LIMIT 1;
    SELECT id INTO v_dsai_ini_63 FROM initiative WHERE title LIKE 'Invite entrepreneurs and innovators to deliver guest lectures%'              LIMIT 1;
    SELECT id INTO v_dsai_ini_64 FROM initiative WHERE title LIKE 'Establish mentorship programs connecting students with industry professionals%' LIMIT 1;

    -- =========================================================================
    -- DSAI: Add missing objective mappings (V6 already mapped obj_20,22,23,24,26,28,34-42)
    -- =========================================================================
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) SELECT v_dsai_obj_21, v_uni_obj_4  WHERE NOT EXISTS (SELECT 1 FROM objective_mapping WHERE dept_objective_id = v_dsai_obj_21 AND university_objective_id = v_uni_obj_4);
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) SELECT v_dsai_obj_25, v_uni_obj_4  WHERE NOT EXISTS (SELECT 1 FROM objective_mapping WHERE dept_objective_id = v_dsai_obj_25 AND university_objective_id = v_uni_obj_4);
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) SELECT v_dsai_obj_27, v_uni_obj_12 WHERE NOT EXISTS (SELECT 1 FROM objective_mapping WHERE dept_objective_id = v_dsai_obj_27 AND university_objective_id = v_uni_obj_12);
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) SELECT v_dsai_obj_29, v_uni_obj_2  WHERE NOT EXISTS (SELECT 1 FROM objective_mapping WHERE dept_objective_id = v_dsai_obj_29 AND university_objective_id = v_uni_obj_2);
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) SELECT v_dsai_obj_30, v_uni_obj_2  WHERE NOT EXISTS (SELECT 1 FROM objective_mapping WHERE dept_objective_id = v_dsai_obj_30 AND university_objective_id = v_uni_obj_2);
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) SELECT v_dsai_obj_31, v_uni_obj_2  WHERE NOT EXISTS (SELECT 1 FROM objective_mapping WHERE dept_objective_id = v_dsai_obj_31 AND university_objective_id = v_uni_obj_2);
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) SELECT v_dsai_obj_32, v_uni_obj_14 WHERE NOT EXISTS (SELECT 1 FROM objective_mapping WHERE dept_objective_id = v_dsai_obj_32 AND university_objective_id = v_uni_obj_14);
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) SELECT v_dsai_obj_33, v_uni_obj_2  WHERE NOT EXISTS (SELECT 1 FROM objective_mapping WHERE dept_objective_id = v_dsai_obj_33 AND university_objective_id = v_uni_obj_2);
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) SELECT v_dsai_obj_43, v_uni_obj_10 WHERE NOT EXISTS (SELECT 1 FROM objective_mapping WHERE dept_objective_id = v_dsai_obj_43 AND university_objective_id = v_uni_obj_10);
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) SELECT v_dsai_obj_44, v_uni_obj_13 WHERE NOT EXISTS (SELECT 1 FROM objective_mapping WHERE dept_objective_id = v_dsai_obj_44 AND university_objective_id = v_uni_obj_13);
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) SELECT v_dsai_obj_45, v_uni_obj_17 WHERE NOT EXISTS (SELECT 1 FROM objective_mapping WHERE dept_objective_id = v_dsai_obj_45 AND university_objective_id = v_uni_obj_17);
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) SELECT v_dsai_obj_46, v_uni_obj_17 WHERE NOT EXISTS (SELECT 1 FROM objective_mapping WHERE dept_objective_id = v_dsai_obj_46 AND university_objective_id = v_uni_obj_17);
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) SELECT v_dsai_obj_47, v_uni_obj_11 WHERE NOT EXISTS (SELECT 1 FROM objective_mapping WHERE dept_objective_id = v_dsai_obj_47 AND university_objective_id = v_uni_obj_11);
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) SELECT v_dsai_obj_48, v_uni_obj_13 WHERE NOT EXISTS (SELECT 1 FROM objective_mapping WHERE dept_objective_id = v_dsai_obj_48 AND university_objective_id = v_uni_obj_13);
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) SELECT v_dsai_obj_49, v_uni_obj_16 WHERE NOT EXISTS (SELECT 1 FROM objective_mapping WHERE dept_objective_id = v_dsai_obj_49 AND university_objective_id = v_uni_obj_16);
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) SELECT v_dsai_obj_50, v_uni_obj_8  WHERE NOT EXISTS (SELECT 1 FROM objective_mapping WHERE dept_objective_id = v_dsai_obj_50 AND university_objective_id = v_uni_obj_8);
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) SELECT v_dsai_obj_51, v_uni_obj_8  WHERE NOT EXISTS (SELECT 1 FROM objective_mapping WHERE dept_objective_id = v_dsai_obj_51 AND university_objective_id = v_uni_obj_8);
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) SELECT v_dsai_obj_52, v_uni_obj_8  WHERE NOT EXISTS (SELECT 1 FROM objective_mapping WHERE dept_objective_id = v_dsai_obj_52 AND university_objective_id = v_uni_obj_8);
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) SELECT v_dsai_obj_53, v_uni_obj_8  WHERE NOT EXISTS (SELECT 1 FROM objective_mapping WHERE dept_objective_id = v_dsai_obj_53 AND university_objective_id = v_uni_obj_8);

    -- =========================================================================
    -- DSAI initiative mappings (derived from A&R/OCE/IE sheet block analysis, GPR rows)
    -- =========================================================================
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_31, v_uni_ini_12);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_32, v_uni_ini_8);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_33, v_uni_ini_8);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_34, v_uni_ini_12);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_35, v_uni_ini_12);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_36, v_uni_ini_7);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_37, v_uni_ini_7);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_38, v_uni_ini_18);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_39, v_uni_ini_12);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_40, v_uni_ini_1);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_41, v_uni_ini_1);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_42, v_uni_ini_1);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_43, v_uni_ini_20);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_44, v_uni_ini_1);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_45, v_uni_ini_11);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_46, v_uni_ini_11);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_47, v_uni_ini_11);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_48, v_uni_ini_11);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_49, v_uni_ini_7);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_50, v_uni_ini_10);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_51, v_uni_ini_7);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_52, v_uni_ini_7);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_53, v_uni_ini_9);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_54, v_uni_ini_22);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_55, v_uni_ini_19);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_56, v_uni_ini_23);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_57, v_uni_ini_26);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_58, v_uni_ini_17);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_59, v_uni_ini_25);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_60, v_uni_ini_24);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_61, v_uni_ini_15);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_62, v_uni_ini_16);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_63, v_uni_ini_15);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_dsai_ini_64, v_uni_ini_15);

    RAISE NOTICE 'V10 DSAI: objective and initiative mappings inserted.';
END $$;
