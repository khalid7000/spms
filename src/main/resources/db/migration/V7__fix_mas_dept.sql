-- V7: Rebuild MAS department strategy from actual spreadsheet data
-- Col A = goal title (also used as objective title per dept convention)
-- Col B = initiatives (delimited list; same across all 3 tabs)
-- Col C = reported achievements per assessment period (tabs: 2022-2024, 2024-2025, 2025-2026)

DO $$
DECLARE
    v_admin_id  BIGINT;
    v_cycle_id  BIGINT;
    v_ap_2224   BIGINT;
    v_ap_2425   BIGINT;
    v_ap_2526   BIGINT;
    v_dept_mas  BIGINT;
    v_ds_mas    BIGINT;
    v_o_ar2_4   BIGINT;
    v_o_ar3_6   BIGINT;
    v_dg        BIGINT;
    v_do        BIGINT;
    v_di        BIGINT;
    v_dm        BIGINT;
BEGIN
    SELECT id INTO v_admin_id FROM app_user WHERE is_admin = true ORDER BY id LIMIT 1;
    SELECT id INTO v_cycle_id FROM planning_cycle WHERE name = '2022-2027 University Strategic Plan';
    SELECT id INTO v_ap_2224  FROM assessment_period WHERE planning_cycle_id = v_cycle_id AND name = '2022-2024';
    SELECT id INTO v_ap_2425  FROM assessment_period WHERE planning_cycle_id = v_cycle_id AND name = '2024-2025';
    SELECT id INTO v_ap_2526  FROM assessment_period WHERE planning_cycle_id = v_cycle_id AND name = '2025-2026';
    SELECT id INTO v_dept_mas FROM department WHERE code = 'MAS';
    SELECT id INTO v_o_ar2_4  FROM objective WHERE title LIKE 'Establish research and innovation centers%' LIMIT 1;
    SELECT id INTO v_o_ar3_6  FROM objective WHERE title LIKE 'Enhance curriculum with innovative%' LIMIT 1;

    -- -------------------------------------------------------------------------
    -- Remove existing MAS strategy (cascaded)
    -- -------------------------------------------------------------------------
    SELECT id INTO v_ds_mas FROM strategy WHERE department_id = v_dept_mas AND planning_cycle_id = v_cycle_id;
    IF v_ds_mas IS NOT NULL THEN
        DELETE FROM achievement WHERE measurement_id IN (
            SELECT m.id FROM measurement m
            JOIN initiative i ON m.initiative_id = i.id
            JOIN objective  o ON i.objective_id  = o.id
            JOIN goal       g ON o.goal_id        = g.id
            WHERE g.strategy_id = v_ds_mas);
        DELETE FROM measurement WHERE initiative_id IN (
            SELECT i.id FROM initiative i
            JOIN objective o ON i.objective_id = o.id
            JOIN goal      g ON o.goal_id      = g.id
            WHERE g.strategy_id = v_ds_mas);
        DELETE FROM initiative WHERE objective_id IN (
            SELECT o.id FROM objective o
            JOIN goal g ON o.goal_id = g.id
            WHERE g.strategy_id = v_ds_mas);
        DELETE FROM objective_mapping WHERE dept_objective_id IN (
            SELECT o.id FROM objective o
            JOIN goal g ON o.goal_id = g.id
            WHERE g.strategy_id = v_ds_mas);
        DELETE FROM objective WHERE goal_id IN (SELECT id FROM goal WHERE strategy_id = v_ds_mas);
        DELETE FROM goal            WHERE strategy_id = v_ds_mas;
        DELETE FROM role_assignment WHERE strategy_id = v_ds_mas;
        DELETE FROM audit_log       WHERE strategy_id = v_ds_mas;
        DELETE FROM strategy        WHERE id          = v_ds_mas;
    END IF;

    -- -------------------------------------------------------------------------
    -- Recreate MAS strategy
    -- -------------------------------------------------------------------------
    INSERT INTO strategy (planning_cycle_id, department_id, strategy_type, state, title)
    VALUES (v_cycle_id, v_dept_mas, 'DEPARTMENT', 'DEPLOYED',
            'Math and Sciences 2022-2027 Departmental Strategic Plan')
    RETURNING id INTO v_ds_mas;
    INSERT INTO role_assignment (user_id, strategy_id, role) VALUES (v_admin_id, v_ds_mas, 'OWNER');

    -- =========================================================================
    -- GOAL 1 (Row 2, Col A): To promote and encourage Math and science research-based activities.
    -- Col B: 5 initiatives
    -- =========================================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_ds_mas, 'To promote and encourage Math and science research-based activities', 1, v_admin_id)
    RETURNING id INTO v_dg;
    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'To promote and encourage Math and science research-based activities', 1, v_admin_id)
    RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar2_4);

    -- 1.1: Direct research collaboration
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Direct collaboration with other departments in research-based projects (capstone/Master thesis) to encourage MaS faculty into research', 1, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'Research collaboration progress and faculty publications', 1)
    RETURNING id INTO v_dm;
    -- 2022-2024
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Alzheimer''s research with Computing & EE department - Ongoing; one publication published July 2024', 5, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Forming a Pathology lab with Computing & EE department - Ongoing; funding being sourced', 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Rema Amawi published "A Machine Learning Approach to Evaluating the Impact of Natural Oils on Alzheimer''s Disease Progression" - July 2024', 2, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Ms. Nastaran published paper on shape of axisymmetric meniscus in a static liquid pool in IMA Journal of Applied Mathematics', 2, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Rouba published and presented paper on DocCert: Blockchain Document Verification Solution at BCCA2023 - Fall 2023', 2, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Manisha published 3 papers in Math and data analytics in highly ranked journals', 2, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Mr. Haroon Moidu published paper on Chemical Profiling of Antidiabetic Polyherbal Formulation in IJGHC - Fall 2023', 2, v_admin_id, v_ap_2224);
    -- 2024-2025
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Alzheimer''s research with Computing & EE department - Completed; publication published July 2024', 5, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Alzheimer''s research with GMU - Ongoing; publication being finalized for submission June 2025', 5, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Pathology lab formation with Computing & EE - Paused; Funding NA', 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Rema Amawi published ML/Alzheimer''s paper July 2024; Dr. Manisha published 3 papers (2 Q3, 1 Q4); Dr. Belal published 2 papers (1 Q1, 1 Q2)', 2, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Proposal submitted to senate and AC committee for policy supporting transition from teaching to research contract for consistently active researchers', 1, v_admin_id, v_ap_2425);
    -- 2025-2026
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Rema, Dr. Shamat, Dr. Dali, Dr. Vidhya, Ms. Wardah and Ms. Rawan initiated multiple research projects with aim of publications and conference presentations', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Rema, Ms. Rawan and Ms. Wardah submitted article for publication on model-based research activity', 2, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Successfully completed first phase of agrowaste valorization research study; second phase continues September 2026 with further publications expected 2027', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Research on biomaterial fabrication from agrowaste initiated by Dr. Rema, Dr. Dali, Dr. Vidhya, Ms. Rawan and Ms. Leena; review article to be submitted June 2026', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Book chapter "Myco-architectures for Future Sustainable Habitats in Extreme Environments" by Dr. Dali and Dr. Rema accepted for Springer publication - May 2026', 2, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Rouba to complete and submit one research paper by end of AY 2025/2026', 2, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Abhilasha published one high-impact journal Spring 2025; submitting one journal and 2-3 conference papers by end of 2025; working on underwater robotics and AI-based grid project', 2, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Rema served as judge for Mustadeem 2 Hackathon organized by Ministry of Climate Change and Environment', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Zohreh to complete and submit research paper to journal; to attend and present at international conference in 2025/2026', 2, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Belal to complete and submit one research paper by end of AY 2025/2026', 2, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Manisha published one research paper in Fall 2025; Dr. Manisha and Dr. Ali working on statistical analysis research', 2, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Samar and Dr. Nastaran co-mentoring capstone projects in collaboration with Electrical Engineering department', 1, v_admin_id, v_ap_2526);

    -- 1.2: Advisory board
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Include an advisory board to support research ideas more connected to industry and outer market, nationally and globally', 2, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'Advisory board formation and engagement progress', 1)
    RETURNING id INTO v_dm;
    -- 2022-2024
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Communications being made to form advisory board for MaS; task paused until Bioinformatics minor is finalized as per management suggestion', 1, v_admin_id, v_ap_2224);
    -- 2024-2025
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Communications ongoing to form advisory board for MaS; paused pending Bioinformatics minor finalization', 1, v_admin_id, v_ap_2425);
    -- 2025-2026
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Ali to hold meeting with management regarding forming advisory board for MaS department during 2025-26 AY', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Rema suggested waiting for Bioinformatics Analysis minor launch before forming advisory board', 1, v_admin_id, v_ap_2526);

    -- 1.3: Encourage students in research
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Encourage students to consider Math, Physics and Sciences as areas to focus for research purposes', 3, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'Student involvement in research activities and publications', 1)
    RETURNING id INTO v_dm;
    -- 2022-2024
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Faculty to include students in research-based activity (undergraduate or Master), at least 1 paper per year; July 2024 publication included student from Computing & EE', 1, v_admin_id, v_ap_2224);
    -- 2024-2025
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Alzheimer''s publication July 2024 included student from Computing & EE; faculty to continue involving students in research activities through 24-25', 1, v_admin_id, v_ap_2425);
    -- 2025-2026
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Vidya''s students (Mohamed Nabil Ansari, Blen Nima & Merlin Igwe) selected in top 10 for Agricultural Hackathon by Ministry of Climate Change and Environment', 3, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Student Jean Paul Tarazi presented research supervised by Dr. Rema Amawi, Ms. Wardah Hasan and Ms. Rawan Abusirdaneh at 6th Annual Undergraduate Global Humanities Conference - RIT China, May 2026', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Rema Amawi and student researcher Joyce James Keeriath published research in Ro''ya Magazine', 2, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Rema, Dr. Dali and Ms. Wardah supervised students (Javeria Mohsin and Wafa Jaferi) in agrowaste valorization project; results presented at 19th Dubai International Food Safety Conference (DIFSC 25)', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Ms. Leena and Ms. Wardah involved two students in recording 3rd video for mini series uploaded to RIT Dubai website', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Manisha completed two research papers with students on Statistical Research - December 2025; papers submitted to journals for consideration', 2, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Ms. Wardah and Dr. Dali selected as mentors for Mustadeem 2; mentored students achieved 1st, 2nd, 3rd and 4th places', 3, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Shamat and Dr. Dali selected to mentor students for Agricultural Hackathon at Agriculture Exhibition - April 2026, Al Ain Convention Centre', 1, v_admin_id, v_ap_2526);

    -- 1.4: Allocate direct fund for research activities
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Allocating direct fund for research-based activities including conferences', 4, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'Research funding allocation and grant activity', 1)
    RETURNING id INTO v_dm;
    -- 2022-2024
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Fund allocated within proposed budget for 24-25 AY for conferences and seminars; UK conference planned for Dr. Rema in Spring', 4, v_admin_id, v_ap_2224);
    -- 2024-2025
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Fund allocated for 24-25 AY related to conferences and seminars - Done', 4, v_admin_id, v_ap_2425);
    -- 2025-2026
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Drs. Rema, Vidhya and Dali submitted research proposal for Sheikh Hamdan Bin Rashid Al Maktoum Health Research Grant', 4, v_admin_id, v_ap_2526);

    -- 1.5: Initiate new program in MaS department
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Initiate developing a new program in MaS department', 5, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'New program and immersion development progress', 1)
    RETURNING id INTO v_dm;
    -- 2022-2024
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Fund allocated for Bio-Organics courses to support developing a science program; postponed to 2025 AY; Bioinformatics minor at feasibility study phase', 1, v_admin_id, v_ap_2224);
    -- 2024-2025
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Immersion in Mathematics introduced, activated and offered to all students in non-Engineering majors - Done', 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Immersion in Mathematics now offered to CESEC and CIT students (started Fall 2024) - Done', 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Minor in Mathematics under process with NY support; plan to offer from Fall 2025; proposal submitted to RIT-D CC - Under process', 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Fund allocated for Bio-Organics courses; Bioinformatics minor pending approvals from NY', 4, v_admin_id, v_ap_2425);
    -- 2025-2026
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Rema continuing work on advanced certificate in Health Informatics and Bioinformatics Minor', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Science faculty participated in Immersions and Minors Fair to support Bioinformatics Analysis Minor - 13 April 2026', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Science faculty participated in open day and student for a day events to support upcoming Bioinformatics minor launch - November 2025', 1, v_admin_id, v_ap_2526);

    -- =========================================================================
    -- GOAL 2 (Row 3, Col A): To invite speakers through webinars, workshops, etc,
    --                         in the areas related to Math (applied Math) and Science.
    -- Col B: 7 initiatives
    -- =========================================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_ds_mas, 'To invite speakers through webinars, workshops, etc, in areas related to Math (applied Math) and Science', 2, v_admin_id)
    RETURNING id INTO v_dg;
    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'To invite speakers through webinars, workshops, etc, in areas related to Math (applied Math) and Science', 1, v_admin_id)
    RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar2_4);

    -- 2.1: External institutional collaboration
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Direct or indirect collaboration with external institutions including healthcare sectors, RIT global and other universities', 1, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'External institutional collaboration and partnership outcomes', 1)
    RETURNING id INTO v_dm;
    -- 2022-2024
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'New collaboration with RIT NY and AUD for research in Physics; AED 15,000 fund granted from RIT research committee', 5, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Connections being made with external institutions in healthcare areas such as Fakeeh hospital, MBRU and DU - Ongoing', 5, v_admin_id, v_ap_2224);
    -- 2024-2025
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Joint Research Project with Gulf Medical University on Alzheimer''s Research', 5, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Joint Research Project with Gulf Medical University on Cancer Research', 5, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Joint Research Project with Computing & EE at RIT Dubai and College of Science at RIT Global', 5, v_admin_id, v_ap_2425);
    -- 2025-2026
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Plans to collaborate with experts and invite guest speakers from biology and chemistry through webinars and workshops to enrich students'' learning experiences', 5, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Guest lecture "From Engineering Fundamentals to Life-Saving Products" by Dr. Mohammed Alkattan, Director at Gentell UK (contact of Dr. Mohammed Shamat)', 1, v_admin_id, v_ap_2526);

    -- 2.2: Hands-on sessions in Math / Applied Math
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Promote more hands-on sessions in Math to promote Applied Math among students', 2, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'Hands-on Math and Applied Math promotion activities', 1)
    RETURNING id INTO v_dm;
    -- 2024-2025
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'High school visits planned with Admissions team to promote hands-on Math sessions and Bioinformatics program', 1, v_admin_id, v_ap_2425);
    -- 2025-2026
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'yMath Club held 3 seminars in Fall 2025 on applications of mathematics; one seminar planned in Spring; calling colleagues from all departments to collaborate', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Planning to organize a Physics Open Day with guidance of Dr. Salameh for students to explore physics concepts practically', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'yMath Club with guidance of Dr. Manisha to conduct seminar on Real Life Applications of Statistics', 1, v_admin_id, v_ap_2526);

    -- 2.3: Promote recent developments in teaching
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Promote recent developments in the area of teaching', 3, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'Teaching development seminars and invited talks', 1)
    RETURNING id INTO v_dm;
    -- 2022-2024
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Breast Cancer Seminar held by medical doctor from DSO Polyclinic - Fall 2023', 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Managing Stress & Anxiety Seminar held by Joint Space Polyclinic - Fall 2023', 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'AI, Cybersecurity & Metaverse Symposium held - Fall 2023', 1, v_admin_id, v_ap_2224);
    -- 2024-2025
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Haroon Moidu presented "Introduction to Gradescope" at FDC Seminar Series 2024 - October 10, 2024', 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Haroon Moidu presented CLO Assessment session workshop - December 4, 2024', 1, v_admin_id, v_ap_2425);
    -- 2025-2026
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Sathya Dev delivered online talk "Engineering for the Heart" for students', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'BIOG 240 and CHMG 201 demo held in support of launching new courses', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Science faculty launched Vol 1 Issue 1 of magazine "The Catalyst - Igniting Curiosity" - February 2026', 2, v_admin_id, v_ap_2526);

    -- 2.4: Collaborate with schools via talks and events
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Collaborate with schools in terms of talks, seminars and other relative events', 4, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'School outreach events and competition outcomes', 1)
    RETURNING id INTO v_dm;
    -- 2022-2024
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Workshops in Physics, Biology and Chemistry held during school visits - Done', 1, v_admin_id, v_ap_2224);
    -- 2024-2025
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'High school visits planned with Admissions to promote Bioinformatics program', 1, v_admin_id, v_ap_2425);
    -- 2025-2026
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Second Annual Science Fair expanded to host high school students in collaboration with Admissions to boost enrollment', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Third Annual Biology and Chemistry competitions held early February, Spring semester 2026', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'International Chemistry Competition 2026: student Peter Pshikali completed qualifying round and selected for semifinals', 3, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Science faculty participated in Immersions and Minors Fair to support launch of Bioinformatics Analysis Minor - 13 April 2026', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Continuation of mini video series for Biology and Chemistry uploaded on RIT Dubai website', 1, v_admin_id, v_ap_2526);

    -- 2.5: Collaboration with other department colleagues in MaS lessons
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Collaboration with colleagues from other departments to present students with applications of MaS subjects within major courses', 5, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'Interdepartmental guest lectures and collaborative sessions in MaS courses', 1)
    RETURNING id INTO v_dm;
    -- 2022-2024
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Faculty to invite one colleague from other majors as guest speaker once per semester; identified as main focus area for 24-25 AY with more outcomes expected', 1, v_admin_id, v_ap_2224);
    -- 2024-2025
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Talk organized with external speaker from business and marketing on applications of Statistics - April 2025', 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Seminar by Dr. Wael on applications of Linear Algebra in Mechanical Engineering (yMath Club event)', 1, v_admin_id, v_ap_2425);

    -- 2.6: Faculty engagement in student for a day
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Enhance engagement of faculty in student for a day workshop activities', 6, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'Faculty participation in student for a day events', 1)
    RETURNING id INTO v_dm;
    -- 2022-2024
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Physics and sciences divisions actively engaged with student for a day activity during Fall - Done', 1, v_admin_id, v_ap_2224);
    -- 2025-2026
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Science faculty signed up to numerous open days and student for a day events with admissions team', 1, v_admin_id, v_ap_2526);

    -- 2.7: Industrial visits
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Engage with day trips to industrial visits to build connection and collaborate in talks, seminars, etc', 7, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'Industrial visit engagement and planning', 1)
    RETURNING id INTO v_dm;
    -- 2022-2024
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Internal collaboration with EE and ME departments for industrial visits with students; discussed with chairs; positive feedback; follow-up planned for Fall 2024', 1, v_admin_id, v_ap_2224);

    -- =========================================================================
    -- GOAL 3 (Row 4, Col A): To prepare training workshops for faculty in Math and
    --                         Science to support the personal development process.
    -- Col B: 2 initiatives
    -- =========================================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_ds_mas, 'To prepare training workshops for faculty in Math and Science to support the personal development process', 3, v_admin_id)
    RETURNING id INTO v_dg;
    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'To prepare training workshops for faculty in Math and Science to support the personal development process', 1, v_admin_id)
    RETURNING id INTO v_do;

    -- 3.1: FDC collaboration - at least 1 development activity per AY
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Collaboration with FDC through department representative or RIT-D organized events, conducting at least 1 development activity per AY', 1, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'FDC workshops and faculty development activities per academic year', 1)
    RETURNING id INTO v_dm;
    -- 2022-2024
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'FDC workshop on accessing RIT global library resources for research activities (Spring 2024) - Done', 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Seminar on effect of AI in education organized by RIT-D - February 2024 - Done', 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Course Folder training seminar offered to all newly hired adjuncts to comply with new ministry requirements - December 2023 - Done', 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'First Aid & Fire Fighting training completed for all science faculty - September 2023 - Done', 1, v_admin_id, v_ap_2224);
    -- 2024-2025
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'August 2024: FDC in-person workshop on enhancing teamwork with external speaker. September 2024: two training workshops on MyCourses usage with speaker from RIT NY via Zoom', 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'First Aid & Fire Fighting training completed for all science faculty - September 2024', 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'MaS faculty regularly updated about FD committee events including seminars, talks, workshops related to AI and teaching', 1, v_admin_id, v_ap_2425);
    -- 2025-2026
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Ali to hold meeting with FD committee chair to plan workshops on integration of AI in teaching and learning with focus on Math/Sciences areas', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'MaS faculty to attend FDC workshops as planned and suggest areas for improvement to department head', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Dali and Ms. Wardah will be teaching BIOG 115 - Garden Science in Fall 2026', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Mohammed Shamat will be teaching CHMG 123 - Chemistry of Material in Fall 2026', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Vidhya will be teaching BIOL 231 - Introduction to Bioinformatics Programming in Fall 2026', 1, v_admin_id, v_ap_2526);

    -- 3.2: Advisory board guidance for faculty development
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Guidance and suggestions from advisory board (once formed) for faculty development planning', 2, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'Advisory board input into faculty development planning', 1)
    RETURNING id INTO v_dm;
    -- 2025-2026
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Ali to hold meeting with management regarding forming advisory board for MaS during 2025-26 AY', 1, v_admin_id, v_ap_2526);

    -- =========================================================================
    -- GOAL 4 (Row 5, Col A): To hold internal Math and Science competitions on campus
    --                         with focus on Engineering, computing, Data analysis, etc.
    --                         Competitions may extend to include other universities.
    -- Col B: 4 initiatives
    -- =========================================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_ds_mas, 'To hold internal Math and Science competitions on campus with focus on Engineering, computing, Data analysis; competitions may extend to include other universities', 4, v_admin_id)
    RETURNING id INTO v_dg;
    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'To hold internal Math and Science competitions on campus with focus on Engineering, computing, Data analysis; competitions may extend to include other universities', 1, v_admin_id)
    RETURNING id INTO v_do;

    -- 4.1: Competitions among schools
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Organizing and holding competitions among schools', 1, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'Inter-school competition events and participation', 1)
    RETURNING id INTO v_dm;
    -- 2022-2024
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Physics (Science) lab sessions for high school students - Ongoing', 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Chemistry Competition held on 19th February - Done', 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Biology Competition held on 26th February - Done', 1, v_admin_id, v_ap_2224);
    -- 2024-2025
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'First Annual Science Fair held in October 2024', 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Second Annual Biology Competition held in February 2025', 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Second Annual Chemistry Competition held in February 2025', 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'High school students invited for physics lab demonstrations during Spring 2025; some events cancelled due to school-side reasons', 1, v_admin_id, v_ap_2425);
    -- 2025-2026
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Second Annual Science Fair held 20th & 22nd October 2025 with high level of student participation', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Third Annual Biology and Chemistry competitions held 9th & 11th February 2026 with high level of student participation', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Organizing open Physics Day for high school students', 1, v_admin_id, v_ap_2526);

    -- 4.2: Students union and ASC collaboration
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Collaboration with RIT-D students union and ASC', 2, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'Student union and ASC collaborative activities', 1)
    RETURNING id INTO v_dm;
    -- 2025-2026
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Ms. Wardah and Dr. Dali selected as mentors for Mustadeem 2 by Ministry of Climate Change; mentored students achieved 1st, 2nd, 3rd and 4th places in the competition', 3, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Rema served as judge for Mustadeem 2 Hackathon organized by Ministry of Climate Change and Environment', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Vidhya, Ms. Deepika, Ms. Wardah and Ms. Leena participated in student government event for Diabetes awareness - 19 November 2025', 1, v_admin_id, v_ap_2526);

    -- 4.3: Student MATH club
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Forming a student MATH club (extendable to Physics, Sciences) led by faculty', 3, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'MATH club formation and activity progress', 1)
    RETURNING id INTO v_dm;
    -- 2022-2024
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Meeting held with students governor to form MATH club led by faculty; two students selected to lead from students'' side; Dr. Nastaran to lead; all to be finalized in Fall 2024 retreat - Ongoing', 1, v_admin_id, v_ap_2224);
    -- 2024-2025
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'yMath Club launched with focus on applications of mathematics; student awarded Bronze Medal (Honor) in International Youth Math Challenge - first time at RIT-D', 3, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'yMath Club: seminar by Dr. Ali Sayyad on Math in real life with robotics arm demo; Kenken competition (Spring); first RIT-D internal Math competition (Fall); LinkedIn page for yMath created and active; talk by Dr. Salameh on Physics April 2025', 1, v_admin_id, v_ap_2425);
    -- 2025-2026
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'yMath Club holding second annual Math competition; Dr. Manisha and Dr. Nastaran to organize Statistics seminar; Dr. Salameh to present Applied Math topic to RIT-D students', 1, v_admin_id, v_ap_2526);

    -- 4.4: Internal competitions in MaS subjects
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Holding internal competitions around different subjects taught in MaS to encourage and motivate RIT-D students and support retention', 4, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'Annual internal Math competition organization and outcomes', 1)
    RETURNING id INTO v_dm;
    -- 2024-2025
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Rouba organized Seventh Annual MATH Competition - Thursday November 7th 2024; high school students hosted by EE department', 1, v_admin_id, v_ap_2425);
    -- 2025-2026
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Organizing eighth annual MATH competition for High School students in Fall 2025', 1, v_admin_id, v_ap_2526);

    -- =========================================================================
    -- GOAL 5 (Row 6, Col A): To share positive teaching and research-based activities
    --                         among all peers.
    -- Col B: 5 initiatives
    -- =========================================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_ds_mas, 'To share positive teaching and research-based activities among all peers', 5, v_admin_id)
    RETURNING id INTO v_dg;
    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'To share positive teaching and research-based activities among all peers', 1, v_admin_id)
    RETURNING id INTO v_do;

    -- 5.1: GLEC representation
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Collaboration and communication through RIT-D representative in GLEC to promote achievements made throughout the year', 1, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'GLEC representation and achievements promotion', 1)
    RETURNING id INTO v_dm;

    -- 5.2: Faculty presentations at department meetings
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Engage faculty in presenting research-based activities and achievements during department meetings', 2, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'Faculty research sharing in department meetings', 1)
    RETURNING id INTO v_dm;
    -- 2022-2024
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Science updates are an integral part of department meetings, shared by Dr. Rema Amawi and her team - ongoing', 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Activities and achievements continuously shared during department meetings throughout 2023-24; will continue next AY', 1, v_admin_id, v_ap_2224);
    -- 2024-2025
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Science updates continue as integral part of department meetings, shared by Dr. Rema Amawi and her team', 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Rema gave talk on Machine Learning approach in Alzheimer''s research at Gulf Medical University to faculty members and Master''s students - Fall 2024', 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Rema invited by GMU to apply for GMU Annual Research Grant - Fall 2024', 4, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'MaS faculty active in community activities including fairs, competitions and seminars as required', 1, v_admin_id, v_ap_2425);
    -- 2025-2026
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Science faculty sharing progress of research activities throughout the year to encourage each other', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'All activities by science faculty shared during department meetings and minuted throughout the academic year', 1, v_admin_id, v_ap_2526);

    -- 5.3: MaS LinkedIn page
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Develop MaS LinkedIn page', 3, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'LinkedIn page creation and regular content activity', 1)
    RETURNING id INTO v_dm;
    -- 2022-2024
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Science updates posted on RIT-Dubai social media; sharing on MaS LinkedIn page planned from next year onwards', 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Communications with marketing team for MaS LinkedIn page ongoing; page created and available to all', 1, v_admin_id, v_ap_2224);
    -- 2024-2025
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Science updates and all department achievements regularly posted on MaS LinkedIn page this year', 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'MaS LinkedIn page active; department activities posted regularly', 1, v_admin_id, v_ap_2425);
    -- 2025-2026
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'yMath club activities regularly posted on Department''s LinkedIn page and club''s page; Ms. Leena serving as social media representative for Sciences on MaS LinkedIn page', 1, v_admin_id, v_ap_2526);

    -- 5.4: MaS page on RIT Dubai website
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Develop MaS page on RIT Dubai website with all necessary department information and current activities', 4, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'Website page development and content updates', 1)
    RETURNING id INTO v_dm;
    -- 2022-2024
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'MaS page on RIT-D website brainstormed and planned in Spring; page created and updates shared with marketing', 1, v_admin_id, v_ap_2224);
    -- 2024-2025
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'New chemistry content being developed to be posted on RIT-Dubai website', 1, v_admin_id, v_ap_2425);
    -- 2025-2026
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Ms. Leena and Ms. Wardah involved two students in recording 3rd video for mini series uploaded to RIT Dubai website', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'First issue of "The Catalyst - Igniting Curiosity" released by Biology and Chemistry faculty team highlighting student achievements, faculty research and science initiatives', 2, v_admin_id, v_ap_2526);

    -- 5.5: Faculty engagement in open days/evenings
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Enhance faculty engagement in open days and evenings', 5, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'Faculty participation in open days and evening events', 1)
    RETURNING id INTO v_dm;
    -- 2022-2024
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Full open days planning shared with team; faculty actively engaged during Fall - Done; department will continue to maximize participation from all', 1, v_admin_id, v_ap_2224);
    -- 2024-2025
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'MaS department not heavily requested by marketing for open days due to absence of dedicated program; continues to participate as required', 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'MaS participated in immersion/minor fair organized by advising; presented students with MATH immersion and Bioinformatics minor updates', 1, v_admin_id, v_ap_2425);
    -- 2025-2026
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Science Faculty signed up to seven open days with admissions team and all student for a day events over the next academic year', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Science faculty participated in open day and student for a day events to support upcoming Bioinformatics minor launch - 19th and 26th November 2025', 1, v_admin_id, v_ap_2526);

    -- =========================================================================
    -- GOAL 6 (Row 7, Col A): To enhance the quality of teaching and learning and
    --                         to improve the overall students' progress.
    -- Col B: 8 initiatives
    -- =========================================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_ds_mas, 'To enhance the quality of teaching and learning and to improve the overall students'' progress', 6, v_admin_id)
    RETURNING id INTO v_dg;
    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'To enhance the quality of teaching and learning and to improve the overall students'' progress', 1, v_admin_id)
    RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar3_6);

    -- 6.1: CLO enhancement with NY
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Enhancement of course learning outcomes to comply better with needs of other departments in cooperation with NY campus', 1, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'CLO revision and NY campus approval progress', 1)
    RETURNING id INTO v_dm;
    -- 2022-2024
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'All MaS course syllabi being revised for CLO enhancement; all Bio, Chem and Physics courses finalized and approved by NY; 3 Math courses finalized; 7 MATH/STAT courses under NY review', 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'CLO-PLO mapping revised for all MaS courses; annual assessment chart created; courses selected for PLO assessment per semester; shared with NY GenEd office; benchmark established', 1, v_admin_id, v_ap_2224);
    -- 2024-2025
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Prerequisites for all 100 level Math courses revised; enhanced drafts shared with main campus; some courses approved by NY - process ongoing but slow from NY side', 1, v_admin_id, v_ap_2425);
    -- 2025-2026
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Frequent communications between Dr. Rema and NY throughout the academic year for completion of tailor-made Bioinformatics Minor for RIT-Dubai campus', 1, v_admin_id, v_ap_2526);

    -- 6.2: Meetings with other department chairs
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Hold frequent meetings with chairs of other departments to enhance courses offered in MaS per AY', 2, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'Interdepartmental course enhancement collaboration', 1)
    RETURNING id INTO v_dm;
    -- 2022-2024
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Ali communicating with other department chairs; ideas exchanged include: course chart modifications, joint teaching/research, inviting faculty from major departments as guest speakers in MaS courses', 1, v_admin_id, v_ap_2224);
    -- 2024-2025
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Ali met with heads of Mechanical and Electrical departments to discuss collaboration including joint minors, mini degrees, MaS faculty involvement in capstone projects and co-mentoring', 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Rema worked closely with Dean and department chairs to design Bioinformatics minor tailored for RIT-Dubai campus', 1, v_admin_id, v_ap_2425);
    -- 2025-2026
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Rouba supervising two students for independent study of 400-level math course offered for first time at RIT-Dubai', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Abhilasha will be teaching BIOL 231 at RIT Dubai for the first time in Spring 2026', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Inviting colleagues from other departments to participate in FDC activities', 1, v_admin_id, v_ap_2526);

    -- 6.3: Teaching quality in new faculty hiring
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Consider quality of teaching while hiring new faculty', 3, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'Faculty hiring quality standards and new faculty mentorship', 1)
    RETURNING id INTO v_dm;
    -- 2022-2024
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Math and sciences hiring committees fully guided on importance of faculty quality (adjunct and full time); practice followed and will continue', 1, v_admin_id, v_ap_2224);
    -- 2024-2025
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Mentorship program for new faculty: course leader or senior faculty assigned to mentor newly joined faculty during first semester to ensure quality in teaching, assessments, grading and course folders', 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Rema worked closely with 3 newly hired adjuncts to prepare for upcoming year: AI incorporation in all courses, new textbooks and coursewares utilizing AI, new lab manuals for all lab courses', 1, v_admin_id, v_ap_2425);

    -- 6.4: Faculty workshops and training on teaching/learning
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Provide faculty with workshops, trainings, talks and similar activities around the area of teaching and learning', 4, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'Faculty professional development in teaching and learning per AY', 1)
    RETURNING id INTO v_dm;
    -- 2022-2024
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'All faculty to attend at least 1 PD per year related to teaching and learning; actively participating in HR/FD organized events during 23-24; database to be created to collect relative information', 1, v_admin_id, v_ap_2224);
    -- 2024-2025
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'MaS faculty regularly updated about FD committee events including seminars, talks, workshops and AI-related events in teaching and learning', 1, v_admin_id, v_ap_2425);
    -- 2025-2026
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Planning to make a presentation to help students prepare for major exams', 1, v_admin_id, v_ap_2526);

    -- 6.5: New teaching methods including technology
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Explore and initiate new methods of teaching including use of technology during lectures', 5, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'Technology integration and innovative teaching method adoption', 1)
    RETURNING id INTO v_dm;
    -- 2022-2024
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Online supporting platforms (McGraw Hill, ALEKS, Pearson) used frequently across MaS courses; platforms support student understanding and provide practice questions - Ongoing', 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Taskforce formed to explore effective use of AI technology within lectures; initial discussions done; colleagues sharing ideas; follow-up in Fall 2024', 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'MATH90 now offered in-person during Spring 2024; budget approved for 2024-25 AY; will continue in-person', 1, v_admin_id, v_ap_2224);
    -- 2025-2026
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Incorporating AI-based math tools in Calculus courses: Desmos, Symbollab, Integral Calculator', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Transitioning all science courses to include AI; piloted this academic year after summer preparation', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Ms. Rawan closely monitored and planned incorporation of AI tools in science courses with all science faculty members', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Nastaran initiated AI integration pilot in MATH 241 Linear Algebra with two AI-focused TAs; extending to MATH 182 Calculus II and MATH 233 in Spring 2026', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Dr. Abhilasha planning to implement AI tools like Desmos/Geogebra for MATH90 for visualization and concept understanding (not allowed in exams)', 1, v_admin_id, v_ap_2526);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'All science faculty implementing various activities to enhance innovation in the classroom', 1, v_admin_id, v_ap_2526);

    -- 6.6: ASC peer-controlled support
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Increase peer-controlled support provided by ASC to improve students'' understanding and overall progress', 6, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'ASC collaboration and student support outcomes', 1)
    RETURNING id INTO v_dm;
    -- 2022-2024
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'ASC heavily involved in supporting MaS with less-abled students through student-led support (especially 100 level); MTH90 recitation sessions managed by ASC in Spring 2024; extra tutoring provided in precalc, College Algebra and Calculus courses', 1, v_admin_id, v_ap_2224);
    -- 2024-2025
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Students in 100 level Math courses regularly directed to ASC workshops for extra support; MaS representative maintains efficient link with ASC throughout AY', 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Ms. Madeeha Durrani (chemistry visiting faculty) provided a workshop for ASC to benefit students from different disciplines', 1, v_admin_id, v_ap_2425);

    -- 6.7: Communications with advising bodies for at-risk students
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Frequent and clear communications with advising bodies, especially in relation to students at risk', 7, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'Early alert and advising communications for at-risk students; DFW rate monitoring', 1)
    RETURNING id INTO v_dm;
    -- 2022-2024
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Communications with advising body through direct and indirect methods (Starfish); all faculty actively using Starfish and early alerts during 23-24; will continue next AY', 1, v_admin_id, v_ap_2224);
    -- 2024-2025
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'DFW rate enhanced to lowest level since Fall 2021 through parallel section meetings and early-stage student progress reviews; W rate still needs improvement', 1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'DFW monitored by Dr. Rema throughout the year for all science courses; rates very successful at approximately 10% average', 1, v_admin_id, v_ap_2425);

    -- 6.8: Promote PLO assessment importance
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Promote the importance of assessing PLOs among students and while assessing CLOs', 8, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'PLO assessment cycle adherence and benchmark application', 1)
    RETURNING id INTO v_dm;
    -- 2022-2024
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'CLO-PLO mapping revised for all MaS courses; annual assessment chart created with courses selected for PLO assessment per semester; PLOs shared with NY GenEd office for streamlining', 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id) VALUES
    (v_dm, 'Benchmark established for PLO assessment in line with NY GenEd instructions; benchmark applied for all MaS courses selected for PLO assessment', 1, v_admin_id, v_ap_2224);

    RAISE NOTICE 'V7 MAS department rebuild completed successfully.';
END $$;
