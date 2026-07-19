-- V20: Correct Business Management (BUS) department seed.
--
-- The data previously seeded under strategy 7 (in V6/V14) used the wrong
-- structure: each row from the source Excel's column B (initiatives) was
-- collapsed into a single condensed initiative per objective, and the
-- column A title was rewritten as an abbreviation of C2 section headers
-- rather than preserved verbatim. Column C achievements were combined rather
-- than split per initiative.
--
-- Correct structure (mirrors how MAS was done):
--   Col A  -> one objective per row, title is the full verbatim C1 text.
--   Col B  -> one initiative per "-" bullet, with its section header in
--             parentheses: "(Section Header) bullet text".
--   Col C  -> one achievement per "-" bullet per assessment period,
--             associated to the closest matching C2 initiative.
--
-- A cascade DELETE on the goal wipes objectives, initiatives, measurements,
-- achievements, objective_mapping, and initiative_mapping for strategy 7,
-- then we re-seed everything correctly.

DO $$
DECLARE
    v_admin_id BIGINT := 1;
    v_ap_2224  BIGINT := 4;
    v_ap_2425  BIGINT := 5;
    v_ap_2526  BIGINT := 6;

    v_dg BIGINT;
    v_do BIGINT;
    v_di BIGINT;
    v_dm BIGINT;

    v_uni_obj_2  BIGINT;
    v_uni_obj_4  BIGINT;
    v_uni_obj_5  BIGINT;
    v_uni_obj_7  BIGINT;
    v_uni_obj_8  BIGINT;
    v_uni_obj_16 BIGINT;

    v_uni_ini_2  BIGINT;
    v_uni_ini_3  BIGINT;
    v_uni_ini_4  BIGINT;
    v_uni_ini_7  BIGINT;
    v_uni_ini_8  BIGINT;
    v_uni_ini_10 BIGINT;
    v_uni_ini_15 BIGINT;
    v_uni_ini_19 BIGINT;
    v_uni_ini_22 BIGINT;

BEGIN
    -- Cascade-delete all prior content under strategy 7 (objectives,
    -- initiatives, measurements, achievements, both mapping tables).
    DELETE FROM goal WHERE strategy_id = 7;

    -- University-side IDs resolved by title for mapping inserts.
    SELECT id INTO v_uni_obj_2  FROM objective WHERE title LIKE 'Achieve 90% employability%' LIMIT 1;
    SELECT id INTO v_uni_obj_4  FROM objective WHERE title LIKE 'Increase RIT Dubai rank score%' LIMIT 1;
    SELECT id INTO v_uni_obj_5  FROM objective WHERE title LIKE 'Establish research and innovation centers%' LIMIT 1;
    SELECT id INTO v_uni_obj_7  FROM objective WHERE title LIKE 'Enhance curriculum with innovative%' LIMIT 1;
    SELECT id INTO v_uni_obj_8  FROM objective WHERE title LIKE 'Increase number of new RIT Dubai student%' LIMIT 1;
    SELECT id INTO v_uni_obj_16 FROM objective WHERE title LIKE 'Active annual participation in admission%' LIMIT 1;

    SELECT id INTO v_uni_ini_2  FROM initiative WHERE title LIKE 'Provide faculty members with diverse professional%' LIMIT 1;
    SELECT id INTO v_uni_ini_3  FROM initiative WHERE title LIKE 'Each program helps co-op office%' LIMIT 1;
    SELECT id INTO v_uni_ini_4  FROM initiative WHERE title LIKE 'Establish a vibrant Academic Development%' LIMIT 1;
    SELECT id INTO v_uni_ini_7  FROM initiative WHERE title LIKE 'Increase support for faculty research%' LIMIT 1;
    SELECT id INTO v_uni_ini_8  FROM initiative WHERE title LIKE 'Maintain and expand local and international accreditations' LIMIT 1;
    SELECT id INTO v_uni_ini_10 FROM initiative WHERE title LIKE 'Engage students with faculty to improve%' LIMIT 1;
    SELECT id INTO v_uni_ini_15 FROM initiative WHERE title LIKE 'Funds are identified by the center%' LIMIT 1;
    SELECT id INTO v_uni_ini_19 FROM initiative WHERE title LIKE 'Create 6 applied research/consultancy%' LIMIT 1;
    SELECT id INTO v_uni_ini_22 FROM initiative WHERE title LIKE 'Each Academic department is engaged in at least one event%' LIMIT 1;

    -- Goal
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (7, 'Business Management Strategic Objectives 2022-2027', 1, v_admin_id)
    RETURNING id INTO v_dg;

    -- =========================================================================
    -- OBJECTIVE 1: Academic Excellence
    -- =========================================================================
    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Academic Excellence: The primary focus will be on providing high quality education in Business and Management at UG and PG levels by continuously enhancing the curriculum, teaching, and research strategies, to ensure that students receive world-class education.', 1, v_admin_id)
    RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_uni_obj_7);

    -- I1.1 (Curriculum Enhancement) Review and update curriculum annually
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Curriculum Enhancement) Aim to review and update the curriculum at least once every academic year', 1, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Curriculum enhancement takes place every semester and accumulatively once a year based on retreat results. Learning Platforms and Simulations are added to the curriculum.', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Curriculum review and update conducted during 2024-2025 for both undergraduate and graduate programs; refinements aligned with QFEmirates and accreditation frameworks.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Curriculum to be reviewed across all undergraduate programs, with proposed updates submitted by May 2026.', NULL, 1, v_admin_id, v_ap_2526);

    -- I1.2 (Curriculum Enhancement) Incorporate at least one new cutting-edge course per year
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Curriculum Enhancement) Incorporate at least one new cutting-edge course each academic year related to emerging business trends or technologies', 2, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'New course added under MGMT489 Seminar in Management on "Business Digital Transformation" delivered first time in Fall 2023.', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Various courses proposed for launch, and revisions initiated in existing courses to reflect digital transformation and sustainability themes.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'At least one new course related to emerging business trends or technologies (e.g. AI in Marketing, digital business) will be introduced.', NULL, 1, v_admin_id, v_ap_2526);

    -- I1.3 (Teaching Quality) At least two faculty development programs per year
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Teaching Quality) Conduct or attend at least two faculty development programs per year to update faculty on latest teaching methods and technologies', 3, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, '(a) Gen AI Symposium organized by the Department in collaboration with Dubai Police and Dubai Police Academy (Jan 2024) attended by all faculty. (b) Gen AI Workshop in Feb 2024 attended by all faculty.', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Faculty participated in multiple development activities, including internal workshops on GenAI integration, business simulations, and inclusive pedagogy (2+ sessions per faculty on average).', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'A minimum of 80% of full-time faculty will attend at least two professional development workshops or webinars focused on teaching.', NULL, 1, v_admin_id, v_ap_2526);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_di, v_uni_ini_2);

    -- I1.4 (Teaching Quality) Average rating of 3.8 in end-of-semester student feedback
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Teaching Quality) Average rating of 3.8 in end-of-semester student feedback on teaching quality', 4, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Average students'' rating of Faculty (all sections Fall 2023): 4.22.', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Average student feedback rating on teaching quality exceeded 4.0 across all sections, with multiple faculty scoring 4.5+ in instructional effectiveness and learning environment.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'The department to maintain an average rating of 3.8 or above in student evaluations for "Instructor Effectiveness".', NULL, 1, v_admin_id, v_ap_2526);

    -- I1.5 (Research Strategies) 10% year-on-year increase in peer-reviewed publications
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Research Strategies) 10% year-on-year increase in the number of faculty peer-reviewed publications', 5, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Research output 140% higher from 2022 to 2023.', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Department achieved >10% year-on-year increase in peer-reviewed journal publications (Q1/Q2), led by both senior and junior faculty.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Faculty peer-reviewed publications to increase by at least 10% compared to the 2024-2025 academic year.', NULL, 1, v_admin_id, v_ap_2526);

    -- I1.6 (Research Strategies) Initiate at least one interdisciplinary research project per year
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Research Strategies) Initiate at least one interdisciplinary research project each year', 6, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Interdisciplinary research project in progress for Spring 2024.', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'At least two interdisciplinary research projects initiated, including work at the intersection of strategy, analytics, and leadership.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'At least one interdisciplinary research project to be initiated and tracked by the departmental research lead.', NULL, 1, v_admin_id, v_ap_2526);

    -- =========================================================================
    -- OBJECTIVE 2: Pedagogical Innovation
    -- =========================================================================
    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Pedagogical Innovation: Faculty are encouraged to explore diverse teaching (e.g. flipped classrooms, problem-solving, and experiential learning) and assessment (e.g. case study analyses, ePortfolios, peer assessments, simulations and gamification) strategies to enhance the learning experience and foster creative and analytical thinking, problem-solving skills, and adaptability.', 2, v_admin_id)
    RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_uni_obj_7);

    -- I2.1 (Diverse Teaching Strategies) At least 60% of faculty implementing diverse strategies
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Diverse Teaching Strategies) At least 60% of faculty are implementing at least one diverse teaching strategy (flipped classrooms, problem-solving, experiential learning, simulations, etc.)', 1, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, '60% of faculty implement at least one diverse teaching strategy (problem-solving, experiential learning, flipped classroom, case-based and project-based teaching) [e.g. Kokkalis, Klincar, Tahir, Strate].', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Over 60% of faculty reported implementing active learning strategies such as flipped classrooms, team-based simulations, role plays, and experiential group projects. Department-wide integration of business simulations (Edumundo, CAPSIM, HBP) enhanced experiential learning.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'At least 60% of faculty to implement one or more innovative teaching strategies (e.g. flipped classrooms, simulations, problem-based learning).', NULL, 1, v_admin_id, v_ap_2526);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_di, v_uni_ini_4);

    -- I2.2 (Innovative Assessment Methods) At least 50% of courses adopt alternative assessments
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Innovative Assessment Methods) At least 50% of courses adopt alternative assessment methods (case studies, ePortfolios, peer assessments, etc.)', 2, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, '60% of faculty adopt some form of differentiated assessment strategy that involves case study analyses, experiential projects, simulations, group projects, reflective journals, etc.', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'At least 50% of courses reported using alternative assessments, including ePortfolios, reflection papers, group simulations, and peer reviews. Faculty redesigned assessments to support critical thinking.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'At least 50% of courses will incorporate alternative assessments such as case studies, peer evaluations, or portfolios.', NULL, 1, v_admin_id, v_ap_2526);

    -- I2.3 (Learning Experience) 70% student satisfaction on impact of pedagogical innovation
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Learning Experience) 70% students'' satisfaction rate on the impact of pedagogical innovation on the learning experience', 3, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Incorporate a question in the SRATE or SS Survey related to impact of pedagogical innovation on learning.', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Student satisfaction with the learning experience remained strong; qualitative feedback from MGMT 215, MGMT 102, and marketing courses noted positive response to innovation in pedagogy and assessment.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Student satisfaction with the learning experience to be measured through end-of-course surveys, targeting a minimum satisfaction rate of 70%.', NULL, 1, v_admin_id, v_ap_2526);

    -- I2.4 (Faculty Recognition) Bi-annually recognize and reward faculty for pedagogical innovations
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Faculty Recognition) Bi-annually recognize and reward faculty who have successfully implemented pedagogical innovations', 4, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Implement bi-annual faculty rewards in recognition of pedagogical innovations (planned).', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Faculty innovation was internally acknowledged and shared during department meetings and AoL sessions. Plans in development to institutionalize recognition mechanisms for pedagogical innovation in 2025-2026.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'A formal recognition system to be developed to acknowledge faculty contributions to pedagogical innovation, with at least one round of recognition conducted by Spring 2026.', NULL, 1, v_admin_id, v_ap_2526);

    -- =========================================================================
    -- OBJECTIVE 3: Student/Faculty Engagement
    -- =========================================================================
    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Student/Faculty Engagement: Frequent and unbiased interaction between students and faculty is crucial in achieving high levels of academic excellence, student engagement, and personal and professional development. Involving students in course material development, assessments and feedback, and research initiatives can enhance students'' understanding of academic content and build strong foundations academically and in their future careers.', 3, v_admin_id)
    RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_uni_obj_5);

    -- I3.1 (Course Material Development) Involve students in at least 15% of course material initiatives
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Course Material Development) Aim to involve students in at least 15% of all course material development initiatives', 1, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Student involvement in material development (e.g. TAs) being assessed.', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Student input incorporated into the design of pitching competitions, simulation exercises, and in-class leadership case discussions, meeting the 15% target. Instructors revised assignments based on student feedback.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'At least 15% of courses to involve students in elements of course material development or feedback processes.', NULL, 1, v_admin_id, v_ap_2526);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_di, v_uni_ini_10);

    -- I3.2 (Assessments and Feedback) Integrate peer assessment in at least 35% of courses
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Assessments and Feedback) Integrate peer assessment in at least 35% of courses offered in one academic year within the department', 2, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Peer assessment being measured across courses (in progress).', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Peer assessment mechanisms embedded in more than 35% of courses, including marketing projects, group activities, and capstone evaluations.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Peer assessment to be embedded in at least 35% of courses across undergraduate and graduate programs.', NULL, 1, v_admin_id, v_ap_2526);

    -- I3.3 (Research Initiatives) 10% increase in students in faculty-led research annually
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Research Initiatives) 10% increase in the number of students involved in faculty-led research projects annually', 3, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Faculty-Students Research Initiatives to be measured first time at the end of Spring 2024.', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Student research participation grew by more than 10%, notably in interdisciplinary areas (e.g., business-analytics crossover projects).', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'The number of students participating in faculty-led research projects to increase by 10% compared to the previous academic year.', NULL, 1, v_admin_id, v_ap_2526);

    -- I3.4 (Overall Engagement) Host at least one open forum per year with students and faculty
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Overall Engagement) Host at least one open forum per year where students and faculty can discuss ways to improve engagement', 4, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Two major meetings conducted in Spring 2023 with students from all majors (in collaboration with the SG Rep for Business and Psychology).', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'The department hosted multiple structured student-faculty interactions: pitching competition showcases, delegation visits, guest speaker debriefs, and open Q&A forums with visiting institutions.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'At least one student-faculty open forum to be organized to gather input on engagement practices and identify areas for improvement.', NULL, 1, v_admin_id, v_ap_2526);

    -- =========================================================================
    -- OBJECTIVE 4: Accreditation and Quality Assurance
    -- =========================================================================
    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Accreditation and Quality Assurance: To maintain and improve the high quality of educational experience, it is paramount to maintain local (CAA) and international (AACSB) accreditations for the programs offered by the department and seek further opportunities for international accreditations (e.g. EQUIS, AMBA, etc.). Additionally, the department will explore opportunities for new degrees and minors/concentrations at both UG and PG levels.', 4, v_admin_id)
    RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_uni_obj_4);

    -- I4.1 (Maintain Existing Accreditations) 100% renewal of CAA and AACSB
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Maintain Existing Accreditations) Ensure 100% renewal of existing accreditations (CAA, AACSB) in the next five years', 1, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'AACSB Accreditation due in Spring 2025. SCA for BS GBM approved (September 2022). SCA for MS OLI approved (June 2024). Self Studies not needed due to dual accreditation (AACSB and CAA).', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'AACSB re-accreditation successfully completed during the review period; department met all core standards, including AoL, faculty qualifications, curriculum alignment, and deployment.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'All course assessment reports (CARs), AoL reports, and curriculum mapping submissions will be completed and submitted on schedule. The department to remain in full compliance with CAA and AACSB requirements.', NULL, 1, v_admin_id, v_ap_2526);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_di, v_uni_ini_8);

    -- I4.2 (Maintain Existing Accreditations) 80% of faculty meet accrediting body qualifications
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Maintain Existing Accreditations) Maintain a faculty composition where at least 80% meet or exceed the qualifications outlined by accrediting bodies (e.g. SA as per AACSB)', 2, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'CAA and AACSB accreditation standards being maintained; faculty qualification tracking underway.', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, '80% of faculty classified as SAs under AACSB guidelines; faculty sufficiency and qualifications monitored and documented consistently. CAA compliance maintained across programs.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Faculty qualification records as per AACSB standards (SA, PA, etc.) to be reviewed to ensure at least 80% of faculty meet AACSB standards.', NULL, 1, v_admin_id, v_ap_2526);

    -- I4.3 (New Accreditations) Identify and apply for at least one new international accreditation
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(New Accreditations) Identify and apply for at least one new international accreditation (EQUIS, AMBA, etc.)', 3, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'In the process of identifying one new international accreditation.', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'EQUIS and AMBA standards were reviewed and benchmarked to inform long-term strategy; initial gap assessment underway.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'A task force will be established to explore feasibility of new international accreditation (e.g. EQUIS or AMBA) with an initial recommendation by end of Spring 2026.', NULL, 1, v_admin_id, v_ap_2526);

    -- I4.4 (Program Expansions) Submit proposals for at least one new degree or minor
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Program Expansions) Develop and submit proposals for at least one new degree and/or one minor at UG or PG levels', 4, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'In the process of securing one or more masters programs and one minor (in Accounting) or UG new program (e.g. BS in Accounting).', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'New undergraduate program in Management Information Systems (MIS) planned to be proposed. Exploratory planning commenced for MS in Finance and micro-credential modules in MBA and Analytics.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'At least one new academic program or minor (UG or PG) to be proposed and formally submitted for internal or external approval.', NULL, 1, v_admin_id, v_ap_2526);

    -- =========================================================================
    -- OBJECTIVE 5: High Quality and Impactful Research
    -- =========================================================================
    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'High Quality and Impactful Research: Promote a culture of research and innovation amongst faculty and students so that the department will become a hub for impactful and innovative research projects. Faculty and students will be incentivized to publish in high-caliber academic journals and conferences and engage in collaborative, interdisciplinary research projects, contributing to the advancement of knowledge in the fields of Business and Management.', 5, v_admin_id)
    RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_uni_obj_4);

    -- I5.1 (Research Culture) 80% of faculty in at least one research-focused workshop per year
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Research Culture) Organize (or 80% of faculty participate) in at least one research-focused workshop or seminar per year', 1, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'In the process of organizing a faculty seminar series pertaining to all RIT Dubai. Expected event in late April-early May 2024.', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Over 90% of faculty participated in at least one research workshop, peer review session, or interdisciplinary collaboration planning session.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'At least one department-wide research workshop or seminar to be organized, with 80% of faculty in attendance.', NULL, 1, v_admin_id, v_ap_2526);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_di, v_uni_ini_7);

    -- I5.2 (Research Culture) 10% increase in students in faculty-led research annually
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Research Culture) 10% increase in the number of students involved in faculty-led research projects annually', 2, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Need to more actively pursue participation of students in faculty-led projects (typically Masters students when the MS OLI is under way).', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Student participation in research grew by more than 10%, particularly in projects tied to course-based data analysis and OB/strategy research collaborations.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'The number of students involved in faculty-led research projects to grow by at least 10% over the previous year.', NULL, 1, v_admin_id, v_ap_2526);

    -- I5.3 (Publications Quality) 20% of publications in Q1/Q2 Scopus journals
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Publications Quality) 20% of faculty publications to be in Q1/Q2 Scopus-indexed journals relevant to the fields of Business and Management', 3, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Tracking Q1/Q2 Scopus publications underway; faculty asked to confirm publications for 2022-2023.', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'More than 50% of all faculty publications in 2024-2025 appeared in Q1/Q2 Scopus-indexed journals.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'A minimum of 20% of total faculty publications to appear in Q1 or Q2 Scopus-indexed journals.', NULL, 1, v_admin_id, v_ap_2526);

    -- I5.4 (Publications Quality) 60% of faculty present at national/international conference
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Publications Quality) 60% of faculty to present at least one academic research paper at a national or international conference', 4, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Tracking conference presentations for 2022-2023: Dr. Rizwan attended EURAM; other faculty participation being confirmed.', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Over 50% of faculty presented papers at national or international conferences and other domain-relevant platforms.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'At least 60% of full-time faculty to present at least one research paper at a national or international conference.', NULL, 1, v_admin_id, v_ap_2526);

    -- I5.5 (Incentivization) Obtain at least one significant research grant per year
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Incentivization) Obtain at least one significant research grant (internal or external) per year that aligns with the department''s areas of focus', 5, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Exploring research grant opportunities; faculty asked to report any grant receipts for 2022-2023.', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Additional grant proposals submitted for upcoming cycles. Faculty encouraged to align research themes with departmental priorities (sustainability, innovation, strategy, analytics).', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'The department to aim to secure at least one internal or external research grant aligned with priority areas (e.g. sustainability, innovation, finance).', NULL, 1, v_admin_id, v_ap_2526);

    -- =========================================================================
    -- OBJECTIVE 6: Intra- and Inter-disciplinary Research Collaborations
    -- =========================================================================
    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Intra- and Inter-disciplinary Research Collaborations: Collaborations amongst faculty and students from various departments will be encouraged as the potential of innovation often arises at the intersection of different fields. Faculty and students who create research projects that bring together various disciplines (e.g. Business, Management, Engineering, Computer sciences, etc.) to solve real-world problems will be incentivized.', 6, v_admin_id)
    RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_uni_obj_5);

    -- I6.1 (Collaboration Rates) 10% of research projects involve cross-dept collaboration
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Collaboration Rates) At least 10% of all active research projects in the Department to involve collaboration across different departments within the university', 1, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Faculty to begin engagement with colleagues across the University or across campuses on research projects (expected Fall 2024).', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Over 10% of active departmental research projects involved collaboration across disciplines. Enhanced research ties with RIT NY and other RIT Global campuses, furthering intra-institutional collaboration.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'At least 10% of active departmental research projects to involve collaboration with other departments at RIT Dubai or RIT Global campuses.', NULL, 1, v_admin_id, v_ap_2526);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_di, v_uni_ini_19);

    -- I6.2 (Incentivization) Annual awards for best interdisciplinary research project
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Incentivization) Implement annual awards for the best interdisciplinary research project, based on innovation, impact, and collaborative spirit', 2, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'As part of the expected implementation of research seminar series, initialize annual rewards for best interdisciplinary projects (expected Fall 2024).', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'While no formal award was implemented in 2024-2025, informal recognition was provided through internal meetings; planning is underway to establish an annual interdisciplinary research award starting 2025-2026.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'An annual award should be launched to recognize the best interdisciplinary research project based on innovation and cross-disciplinary engagement.', NULL, 1, v_admin_id, v_ap_2526);

    -- I6.3 (Outreach) Publish bi-annual departmental newsletters on interdisciplinary research
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Outreach) Publish bi-annual departmental newsletters or updates highlighting the successes and opportunities in interdisciplinary research', 3, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Begin designing content for a bi-annual departmental newsletter and a departmental presence on LinkedIn to showcase teaching, research, and community service activities.', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'The department circulated periodic internal updates. A Business and Management Research Symposium is scheduled for April 2026 to showcase collaborative research across disciplines.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'A bi-annual departmental newsletter or digital report to be published, highlighting interdisciplinary research successes and opportunities.', NULL, 1, v_admin_id, v_ap_2526);

    -- =========================================================================
    -- OBJECTIVE 7: Community Partnerships
    -- =========================================================================
    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Community Partnerships: Establish partnerships with local businesses, non-profit organizations, and government agencies with the assistance of the University''s Co-op Office, to collaborate on projects, research, and initiatives that address community needs and challenges. These partnerships will be beneficial for faculty and students as they can provide co-op opportunities, internships, apprenticeships, and real-world experiences while benefiting the local community.', 7, v_admin_id)
    RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_uni_obj_2);

    -- I7.1 (Partnership Development) Minimum 3 new partnerships per year with Co-op Office assistance
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Partnership Development) With the assistance of the Co-op office, establish a minimum of three new partnerships per year with local businesses, non-profit organizations, and government agencies', 1, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Ongoing discussions with the Co-Op Office to establish new partnerships on a yearly basis. Initial results expected at the end of Fall 2024.', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Three new partnerships established during 2024-2025 with Columbus State University (USA), Golisano Institute for Business and Entrepreneurship (USA), and Coventry University (UK), with student-facing activities and public engagements conducted.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'The department to coordinate with the Co-op Office to establish at least three new partnerships with businesses, NGOs, or government bodies across diverse sectors.', NULL, 1, v_admin_id, v_ap_2526);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_di, v_uni_ini_3);

    -- I7.2 (Partnership Development) 15% year-over-year increase in co-op placements
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Partnership Development) 15% year-over-year increase in co-op placement opportunities for students in the department', 2, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'In collaboration with the Co-Op Office, identifying the number of co-op places available to department''s students in 2022 and 2023.', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Department supported a >15% year-on-year increase in co-op placement opportunities through direct engagement with the Co-op Office and Advisory Board contacts.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Co-op placement opportunities for students to increase by at least 15% compared to the previous academic year.', NULL, 1, v_admin_id, v_ap_2526);

    -- I7.3 (Partnership Development) 80% student satisfaction in co-op and internship experiences
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Partnership Development) 80% or higher satisfaction rate among students participating in co-op, internships, and real-world projects associated with these partnerships', 3, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Analyzing available data related to satisfaction rate of students participating in co-ops in 2022 and 2023.', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Student feedback indicated >80% satisfaction with the relevance and quality of co-op and internship experiences.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'A satisfaction survey to be administered to students in experiential learning opportunities, targeting an 80% or higher satisfaction rate.', NULL, 1, v_admin_id, v_ap_2526);

    -- I7.4 (Partnership Development) At least one coordination meeting per year with Co-op Office
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Partnership Development) Hold at least one coordination meeting per year with the University''s Co-op Office to align goals, share data, and jointly pursue new partnership opportunities', 4, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Initialize a regular meeting with the Co-op Office (per semester) starting Spring 2024.', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Multiple informal and one formal coordination meeting held with the Co-op Office per semester; shared data on departmental placement trends helped align outreach efforts.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'At least two coordination meetings with the Co-op Office to be held during the academic year to align partnership development goals and share placement data.', NULL, 1, v_admin_id, v_ap_2526);

    -- =========================================================================
    -- OBJECTIVE 8: Start-up Competitions
    -- =========================================================================
    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Start-up Competitions: Organize regular start-up competitions and utilize existing incubators and funds, where students in Business and Management can pitch their business ideas to a panel of judges including successful entrepreneurs, investors, and faculty. These competitions can provide funding, mentorship, and networking opportunities for future entrepreneurs.', 8, v_admin_id)
    RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_uni_obj_8);

    -- I8.1 Organize at least one startup competition per academic year
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Organize at least one startup competition per academic year, with an increasing number of participants each year and achieve a participation rate of at least 20% of the business and management student body', 1, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'As a Business and Management Department, there is a need to engage in start-up competitions for students. Expected to start in Fall 2024.', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Successfully organized two major competitions: (a) MGMT 102 Pitching Competition (Spring 2025) with over 100 student participants, exceeding the 20% departmental participation target; (b) Innovation and Entrepreneurship Day (Fall 2024), engaging school students (Grades 11-12) across the UAE.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'At least two start-up or innovation-focused competitions to be organized during the academic year, targeting participation from at least 20% of the business student body. The Innovation & Entrepreneurship School Competition to be institutionalized as an annual signature event.', NULL, 1, v_admin_id, v_ap_2526);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_di, v_uni_ini_15);

    -- I8.2 Mentorship program for at least 60% of competing teams
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Establish a mentorship program where at least 60% of the participating teams are paired with industry mentors for at least 6 months post-competition and provide access to incubator space for at least 40% of finalist teams', 2, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Upon successful completion of the first start-up competition, provisions need to be made for mentorship programs for finalists and successful teams/individuals.', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Industry mentors supported finalist teams in the MGMT 102 competition; 20%+ of these teams maintained contact for guidance post-event. Initial steps taken to link finalist teams with incubator space starting 2025-2026.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Industry mentors to be paired with at least 50% of competing teams, with mentorship lasting a minimum of six months.', NULL, 1, v_admin_id, v_ap_2526);

    -- I8.3 Secure external funding for grants to at least three winning teams per competition
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Secure funding (through external sponsors) to award grants to at least three winning teams per competition, with the size of the grants increasing by 5% each year', 3, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Exploring funding opportunities beginning in Spring 2024. Expected outcomes in Fall 2024.', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Current prizes offered through in-kind support; a proposal is being finalized to secure external sponsorship and scale funding for at least three winning teams annually, with targeted 5% growth in prize value.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'The department to secure external sponsorship to fund grants for at least three winning teams, aiming for a 5% increase in total prize value compared to the previous year.', NULL, 1, v_admin_id, v_ap_2526);

    -- =========================================================================
    -- OBJECTIVE 9: Public Events and Fora
    -- =========================================================================
    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Public Events and Fora: Organize regularly public events, forums, symposia, workshops, guest speakers, and roundtable discussions on relevant business and management topics. These events can engage the community and serve as platforms for thought leadership, knowledge sharing, and networking for both faculty and students.', 9, v_admin_id)
    RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_uni_obj_16);

    -- I9.1 (Events) Organize at least three major public events per year and establish flagship event
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Events) Organize at least three major public events each academic year (e.g. symposia, workshops, guest speaker sessions, and roundtable discussions) and establish at least one flagship annual event', 1, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'The Department organized the 1st Gen AI Symposium in Jan 2024 attended by 173 in-person and 33 online delegates, of whom 70% were from outside RIT Dubai. The event is expected to become the flagship annual event for the Department.', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'The department organized three major events during 2024-2025: (a) Innovation and Entrepreneurship Day (Nov 2024) with 150+ school participants; (b) Guest Speaker Sessions integrated into various courses; (c) Leadership Workshop "Leading with Purpose" with 20 attendees.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'The department to organize at least three major public-facing events. At least one flagship event (e.g. RIT Dubai Research Symposium or AI in Business Forum) to be positioned as a recurring annual benchmark initiative.', NULL, 1, v_admin_id, v_ap_2526);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_di, v_uni_ini_22);

    -- I9.2 (Events) 70% faculty and 30% students in events, 20% external attendees
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Events) 70% of faculty and 30% of students to be involved in at least one public event per year and at least 20% of attendees to come from outside the university', 2, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'In the next edition of the Gen AI Symposium (expected March-April 2025) it is expected that 30% of delegates will be students and more than 60% of delegates will come from government entities, local businesses, and community members.', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Faculty participation exceeded 70%, and student involvement was approximately 40%. External audience attendance at Innovation and Entrepreneurship Day and guest events surpassed the 20% benchmark.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'At least 70% of faculty and 30% of business students will be involved in public events as organizers, speakers, or attendees. At least 20% of total attendees across events will be from outside RIT.', NULL, 1, v_admin_id, v_ap_2526);

    -- I9.3 (Events) Post-event surveys targeting 75% satisfaction and debriefing meetings
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Events) Conduct post-event surveys aiming for a satisfaction rate of over 75% and hold a debriefing meeting after each event to review what went well and identify areas for improvement', 3, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Post-event surveys are currently underway (ongoing in Spring 2024). Debriefing meeting to be held in April 2024.', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Informal feedback and internal reviews conducted post-events; qualitative feedback indicated high satisfaction regarding relevance, engagement, and organization. Plans for formal post-event surveys and structured debriefs in place for AY 2025-2026.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Post-event surveys to be distributed with a goal of 75% satisfaction rate and a debriefing protocol to be followed to review outcomes and improvements.', NULL, 1, v_admin_id, v_ap_2526);

    -- =========================================================================
    -- OBJECTIVE 10: Sustainability and Environmental Initiatives
    -- =========================================================================
    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Sustainability and Environmental Initiatives: The publication of UN''s SDGs has put sustainability and environmental responsibility at the core of social concern for all. Promoting these principles by implementing initiatives capable of reducing the Department''s environmental footprint is important. These efforts can inspire students, faculty, and the community to adopt sustainable practices.', 10, v_admin_id)
    RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_uni_obj_16);

    -- I10.1 (Implementation and Adoption) 2+ sustainability initiatives per year, 30% faculty 40% students
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Implementation and Adoption) Launch at least two new sustainability initiatives and/or practices per academic year involving 30% of faculty and 40% of students', 1, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Currently in discussion on deciding on the strategy of sustainability initiatives (expected results at the end of Fall 2024).', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'While formal departmental sustainability initiatives were limited in 2024-2025, several faculty incorporated sustainability content into courses (OB, Marketing, Leadership). Informal paper- and plastic-reduction practices adopted in several classes and events.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'At least two new sustainability-focused initiatives (e.g. plastic reduction, digital submission policies, energy savings) to be launched during the academic year. A Sustainability and Social Impact Lab/Club to be established.', NULL, 1, v_admin_id, v_ap_2526);
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_di, v_uni_ini_22);

    -- I10.2 (Implementation and Adoption) Publish bi-annual sustainability report
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Implementation and Adoption) Publish a departmental bi-annual sustainability report outlining achievements, challenges, and future plans', 2, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Initiate discussions on sustainability practices and assign a task-force team to organize events and publish a sustainability report bi-annually.', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'No formal bi-annual sustainability report was issued; however, a plan has been initiated to introduce a Departmental Sustainability and Social Impact Lab/Club in 2025-2026 to coordinate reporting and student-led campaigns.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'A departmental sustainability report to be published by the end of Spring 2026, documenting achievements, challenges, and future plans.', NULL, 1, v_admin_id, v_ap_2526);

    -- I10.3 (Implementation and Adoption) 2 partnerships with local sustainability organizations
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '(Implementation and Adoption) Establish at least two partnerships with local organizations focused on environmental sustainability and involve at least one corporate partner per year in sustainability initiatives', 3, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'In collaboration with the Co-Op Office and RIT''s SG, initiate the establishment of partnerships with local organizations focusing on environmental sustainability (expected results in Fall 2024).', NULL, 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'No formal partnerships were recorded in this area for the review period. Efforts will be made to engage at least one corporate partner in the coming year, leveraging existing Advisory Board contacts and competition sponsors.', NULL, 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'The department to initiate at least two partnerships with local or regional organizations working in environmental sustainability. One corporate partner to be engaged in a co-curricular or academic sustainability initiative.', NULL, 1, v_admin_id, v_ap_2526);

    RAISE NOTICE 'V20 BUS: 10 objectives, 39 initiatives, 117 achievements, 10 objective_mappings, 10 initiative_mappings inserted.';
END $$;
