-- V6: Seed 2022-2027 RIT Dubai Strategic Plan data
-- Idempotent: wrapped in an existence check so re-running is safe.

DO $$
DECLARE
    v_admin_id         BIGINT;
    v_cycle_id         BIGINT;
    v_ap_2224          BIGINT;
    v_ap_2425          BIGINT;
    v_ap_2526          BIGINT;
    v_ay_2224          BIGINT;
    v_ay_2425          BIGINT;
    v_ay_2526          BIGINT;

    -- University strategy
    v_univ_strat_id    BIGINT;

    -- Vision areas
    v_va_ar            BIGINT;
    v_va_ie            BIGINT;
    v_va_oce           BIGINT;

    -- University goals
    v_g_ar1  BIGINT; v_g_ar2  BIGINT; v_g_ar3  BIGINT;
    v_g_ie1  BIGINT;
    v_g_oce1 BIGINT; v_g_oce2 BIGINT; v_g_oce3 BIGINT;
    v_g_oce4 BIGINT; v_g_oce5 BIGINT; v_g_oce6 BIGINT;

    -- University objectives
    v_o_ar1_1  BIGINT; v_o_ar1_2  BIGINT;
    v_o_ar2_3  BIGINT; v_o_ar2_4  BIGINT;
    v_o_ar3_5  BIGINT; v_o_ar3_6  BIGINT;
    v_o_ie1_1  BIGINT; v_o_ie1_2  BIGINT;
    v_o_oce1_1 BIGINT; v_o_oce1_2 BIGINT;
    v_o_oce2_3 BIGINT; v_o_oce2_4 BIGINT;
    v_o_oce3_5 BIGINT; v_o_oce3_6 BIGINT;
    v_o_oce4_7 BIGINT;
    v_o_oce5_8 BIGINT;
    v_o_oce6_9 BIGINT; v_o_oce6_10 BIGINT;

    -- Departments
    v_dept_dsai  BIGINT; v_dept_eecs  BIGINT; v_dept_adv   BIGINT;
    v_dept_meie  BIGINT; v_dept_iad   BIGINT; v_dept_inse  BIGINT;
    v_dept_coop  BIGINT; v_dept_mas   BIGINT;
    v_dept_bus   BIGINT; v_dept_asc   BIGINT; v_dept_la    BIGINT;

    -- Dept strategy IDs
    v_ds_dsai BIGINT; v_ds_eecs BIGINT; v_ds_adv  BIGINT;
    v_ds_meie BIGINT; v_ds_iad  BIGINT; v_ds_inse BIGINT;
    v_ds_coop BIGINT; v_ds_mas  BIGINT;

    -- Reusable scalars
    v_dg BIGINT;   -- dept goal
    v_do BIGINT;   -- dept/univ objective
    v_di BIGINT;   -- initiative (dept and univ)
    v_dm BIGINT;   -- measurement (dept and univ)

BEGIN
    IF EXISTS (SELECT 1 FROM planning_cycle WHERE name = '2022-2027 University Strategic Plan') THEN
        RAISE NOTICE 'V6 seed already applied, skipping.';
        RETURN;
    END IF;

    SELECT id INTO v_admin_id FROM app_user WHERE is_admin = true ORDER BY id LIMIT 1;
    IF v_admin_id IS NULL THEN
        RAISE EXCEPTION 'No admin user found. Create an admin account before running this seed.';
    END IF;

    -- -------------------------------------------------------------------------
    -- Planning cycle
    -- -------------------------------------------------------------------------
    INSERT INTO planning_cycle (name, start_year, end_year, active)
    VALUES ('2022-2027 University Strategic Plan', 2022, 2027, false)
    RETURNING id INTO v_cycle_id;

    -- -------------------------------------------------------------------------
    -- Assessment periods
    -- -------------------------------------------------------------------------
    INSERT INTO assessment_period (planning_cycle_id, name, start_date, end_date, sort_order)
    VALUES (v_cycle_id, '2022-2024', '2022-09-01', '2024-08-31', 1) RETURNING id INTO v_ap_2224;

    INSERT INTO assessment_period (planning_cycle_id, name, start_date, end_date, sort_order)
    VALUES (v_cycle_id, '2024-2025', '2024-09-01', '2025-08-31', 2) RETURNING id INTO v_ap_2425;

    INSERT INTO assessment_period (planning_cycle_id, name, start_date, end_date, sort_order)
    VALUES (v_cycle_id, '2025-2026', '2025-09-01', '2026-08-31', 3) RETURNING id INTO v_ap_2526;

    -- -------------------------------------------------------------------------
    -- Academic years
    -- -------------------------------------------------------------------------
    INSERT INTO academic_year (name, start_date, end_date, closed)
    VALUES ('2022-2024', '2022-09-01', '2024-08-31', true) RETURNING id INTO v_ay_2224;

    INSERT INTO academic_year (name, start_date, end_date, closed)
    VALUES ('2024-2025', '2024-09-01', '2025-08-31', true) RETURNING id INTO v_ay_2425;

    INSERT INTO academic_year (name, start_date, end_date, closed)
    VALUES ('2025-2026', '2025-09-01', '2026-08-31', false) RETURNING id INTO v_ay_2526;

    -- -------------------------------------------------------------------------
    -- University strategy
    -- -------------------------------------------------------------------------
    INSERT INTO strategy (planning_cycle_id, department_id, strategy_type, state, title, description)
    VALUES (v_cycle_id, NULL, 'UNIVERSITY', 'FROZEN',
            'RIT Dubai 2022-2027 Strategic Plan',
            'University-wide strategic plan for RIT Dubai covering academic years 2022 through 2027.')
    RETURNING id INTO v_univ_strat_id;

    INSERT INTO role_assignment (user_id, strategy_id, role)
    VALUES (v_admin_id, v_univ_strat_id, 'OWNER');

    -- -------------------------------------------------------------------------
    -- Vision areas
    -- -------------------------------------------------------------------------
    INSERT INTO vision_area (strategy_id, name, sort_order)
    VALUES (v_univ_strat_id, 'Academic & Research', 1) RETURNING id INTO v_va_ar;

    INSERT INTO vision_area (strategy_id, name, sort_order)
    VALUES (v_univ_strat_id, 'Innovation & Entrepreneurship', 2) RETURNING id INTO v_va_ie;

    INSERT INTO vision_area (strategy_id, name, sort_order)
    VALUES (v_univ_strat_id, 'Outreach & Community Engagement', 3) RETURNING id INTO v_va_oce;

    -- =========================================================================
    -- ACADEMIC & RESEARCH
    -- =========================================================================

    INSERT INTO goal (strategy_id, area_id, title, sort_order, created_by)
    VALUES (v_univ_strat_id, v_va_ar,
            'RIT Dubai is recognized for high quality rigorous education proven by high ranking in the UAE and demanded students in the job market.',
            1, v_admin_id) RETURNING id INTO v_g_ar1;

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_g_ar1, 'Achieve 90% employability for graduated students', 1, v_admin_id)
    RETURNING id INTO v_o_ar1_1;

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_o_ar1_1, 'More engagement with Alumni. Devoted resource to complete DB and establish connection. Start more regular events.', 1, v_admin_id)
    RETURNING id INTO v_di;

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_o_ar1_1, 'Provide faculty members with diverse professional development opportunities', 2, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, '65% of faculty members achieve approach exceed expectations or higher in the course student evaluations part of the annual evaluation', 1)
    RETURNING id INTO v_dm;

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_o_ar1_1, 'Each program helps co-op office with at least one industry connection every year', 3, v_admin_id)
    RETURNING id INTO v_di;

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_g_ar1, 'Maintain attrition to below 10%', 2, v_admin_id)
    RETURNING id INTO v_o_ar1_2;

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_o_ar1_2, 'Establish a vibrant Academic Development unit', 1, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'Full-time resources are added to the unit. Activities within the unit increase by at least 50% year on year. More connections between faculty and advising. Faculty training in key areas like GenAI and Innovation.', 1)
    RETURNING id INTO v_dm;

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_o_ar1_2, 'More effective use of Early Alert with better connection among faculty, advisors and ASC', 2, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'Outreach to reported students is streamlined with 100% recorded closure on students with multiple alerts', 1)
    RETURNING id INTO v_dm;

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_o_ar1_2, 'Target freshman students with program specific events to integrate them within the department and RIT Dubai', 3, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'At least one initiative by each program targeting new Freshmen students every academic year', 1)
    RETURNING id INTO v_dm;

    INSERT INTO goal (strategy_id, area_id, title, sort_order, created_by)
    VALUES (v_univ_strat_id, v_va_ar, 'Produce research that addresses the needs and challenges of the community', 2, v_admin_id)
    RETURNING id INTO v_g_ar2;

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_g_ar2, 'Increase RIT Dubai rank score in the UAE Ministry of Education risk assessment system to above 90 by 2027', 1, v_admin_id)
    RETURNING id INTO v_o_ar2_3;

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_o_ar2_3, 'Increase support for faculty research giving priority to CAA rank parameters', 1, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, '50% more research funds every year', 1) RETURNING id INTO v_dm;

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_o_ar2_3, 'Maintain and expand local and international accreditations', 2, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, '100% achievement of all accreditation applications', 1) RETURNING id INTO v_dm;

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_g_ar2, 'Establish research and innovation centers of excellence in support of the UAE 2050 vision (Sustainability and Energy, Smart Cities, Innovation and entrepreneurship)', 2, v_admin_id)
    RETURNING id INTO v_o_ar2_4;

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_o_ar2_4, 'Establish taskforce for the centers with KPIs', 1, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'Task force established. Processes and bylaws established. Funds are identified for each of the three centers. At least 5 projects sponsored by each center annually.', 1)
    RETURNING id INTO v_dm;

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_o_ar2_4, 'Engage students with faculty to improve teaching, research and service', 2, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'More students are involved in teaching activities like tutoring, TAs, curriculum development. More research production involving both faculty and students. More student contributions in servicing all RIT Dubai constituencies.', 1)
    RETURNING id INTO v_dm;

    INSERT INTO goal (strategy_id, area_id, title, sort_order, created_by)
    VALUES (v_univ_strat_id, v_va_ar, 'Increase RIT Dubai effectiveness, reach and academic success using technology', 3, v_admin_id)
    RETURNING id INTO v_g_ar3;

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_g_ar3, 'Automate repetitive internal Processes', 1, v_admin_id)
    RETURNING id INTO v_o_ar3_5;

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_o_ar3_5, 'Increase use of IT to streamline highly repetitive tasks', 1, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'Move paper forms to online forms. Improve course file processing. Improve academic dashboard and its use to promote sharing and breaking of silos.', 1)
    RETURNING id INTO v_dm;

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_g_ar3, 'Enhance curriculum with innovative and new material and teaching models', 2, v_admin_id)
    RETURNING id INTO v_o_ar3_6;

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_o_ar3_6, 'Formalize innovative teaching models for relevant courses that demonstrate pedagogical advantage in such models.', 1, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'Implement at least one innovative model in 20% of existing courses (Flipped, hybrid, experiential learning, etc.)', 1)
    RETURNING id INTO v_dm;

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_o_ar3_6, 'Establish a strategy for all programs to introduce new and recent advances in courses. Example: Strategy for deploying GenAI in all programs.', 2, v_admin_id)
    RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_di, 'Annually consider and plan introduction of at least one new advancement and technology into the curriculum and demonstrate analysis of the success of such initiatives (e.g. AI, gen AI, Robotics, FinTech, etc.).', 1)
    RETURNING id INTO v_dm;

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_o_ar3_6, 'Introduce new programs and course offerings', 3, v_admin_id)
    RETURNING id INTO v_di;

    -- =========================================================================
    -- INNOVATION & ENTREPRENEURSHIP
    -- =========================================================================

    INSERT INTO goal (strategy_id, area_id, title, sort_order, created_by)
    VALUES (v_univ_strat_id, v_va_ie, 'RIT Dubai Innovation Center has engagements with multiple partners becoming a hub for community innovation and start-ups', 1, v_admin_id)
    RETURNING id INTO v_g_ie1;

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_g_ie1, 'Increase number of new RIT Dubai student and faculty launched businesses through the Innovation Center by 20% year-on-year', 1, v_admin_id)
    RETURNING id INTO v_o_ie1_1;

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_o_ie1_1, 'Funds are identified by the center. Application process is established by designated taskforce. Projects are submitted and launched. Improve the deployment of the Innovation Journey in all programs through proper measurement and faculty training.', 1, v_admin_id)
    RETURNING id INTO v_di;

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_g_ie1, 'Funding of start-up should grow by 10% year on year', 2, v_admin_id)
    RETURNING id INTO v_o_ie1_2;

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_o_ie1_2, 'Collaborate with admission on sponsoring an event with a k-12 school', 1, v_admin_id)
    RETURNING id INTO v_di;

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_o_ie1_2, 'Each department holds at least one k-12 event per year in the Innovation Center', 2, v_admin_id)
    RETURNING id INTO v_di;

    -- =========================================================================
    -- OUTREACH & COMMUNITY ENGAGEMENT
    -- =========================================================================

    INSERT INTO goal (strategy_id, area_id, title, sort_order, created_by)
    VALUES (v_univ_strat_id, v_va_oce, 'Support UAE Development Agenda - social responsibility.', 1, v_admin_id)
    RETURNING id INTO v_g_oce1;

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_g_oce1, 'Meet 10 Gov. entities to promote courses', 1, v_admin_id)
    RETURNING id INTO v_o_oce1_1;

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_o_oce1_1, 'Deliver at least 2 training diplomas to UAE government entities', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_o_oce1_1, 'Participate and/or deliver competitions with UAE government entities', 2, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_o_oce1_1, 'Meet Gov. entities to build relationship', 3, v_admin_id) RETURNING id INTO v_di;

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_g_oce1, 'Engage the Innovation Center with community event and activities to foster a welcoming relationship', 2, v_admin_id)
    RETURNING id INTO v_o_oce1_2;

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_o_oce1_2, 'Each department holds at least one k-12 event per year in the Innovation Center', 1, v_admin_id) RETURNING id INTO v_di;

    INSERT INTO goal (strategy_id, area_id, title, sort_order, created_by)
    VALUES (v_univ_strat_id, v_va_oce, 'Become university interns & alumni of choice for Employers', 2, v_admin_id)
    RETURNING id INTO v_g_oce2;

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_g_oce2, 'Raise interns/coop quality', 1, v_admin_id) RETURNING id INTO v_o_oce2_3;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_o_oce2_3, 'Utilize RIT365 and extra coaching sessions to raise interns/coop quality', 1, v_admin_id) RETURNING id INTO v_di;

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_g_oce2, 'Engage with industry and government partners in more innovative ways', 2, v_admin_id)
    RETURNING id INTO v_o_oce2_4;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_o_oce2_4, 'Create 6 applied research/consultancy projects-coop', 1, v_admin_id) RETURNING id INTO v_di;

    INSERT INTO goal (strategy_id, area_id, title, sort_order, created_by)
    VALUES (v_univ_strat_id, v_va_oce, 'Enhance alumni relations & mutual benefits', 3, v_admin_id)
    RETURNING id INTO v_g_oce3;

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_g_oce3, 'Establish Alumni Club', 1, v_admin_id) RETURNING id INTO v_o_oce3_5;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_o_oce3_5, 'Keep Alumni data completed and accurate for at least 75% of all Alumni', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_o_oce3_5, 'Conduct One Major Alumni event per year', 2, v_admin_id) RETURNING id INTO v_di;

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_g_oce3, '10% annual increase in employers number and 10% annual increase in employers attending career fairs', 2, v_admin_id)
    RETURNING id INTO v_o_oce3_6;

    INSERT INTO goal (strategy_id, area_id, title, sort_order, created_by)
    VALUES (v_univ_strat_id, v_va_oce, 'Contribute to enrollment numbers year on year.', 4, v_admin_id)
    RETURNING id INTO v_g_oce4;

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_g_oce4, 'Active annual participation in admission initiatives that include: Open House, Highschool visits and workshops, etc.', 1, v_admin_id)
    RETURNING id INTO v_o_oce4_7;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_o_oce4_7, 'Each Academic department is engaged in at least one event per term', 1, v_admin_id) RETURNING id INTO v_di;

    INSERT INTO goal (strategy_id, area_id, title, sort_order, created_by)
    VALUES (v_univ_strat_id, v_va_oce, 'Become a favorite Gov. & Corp. Partner', 5, v_admin_id)
    RETURNING id INTO v_g_oce5;

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_g_oce5, 'Support initiatives that engage faculty with outside entities to create relationships and promote collaboration', 1, v_admin_id)
    RETURNING id INTO v_o_oce5_8;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_o_oce5_8, 'Meet 10 Gov. entities to promote courses.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_o_oce5_8, 'Target 2 event sponsors (Gov. or Corp.)', 2, v_admin_id) RETURNING id INTO v_di;

    INSERT INTO goal (strategy_id, area_id, title, sort_order, created_by)
    VALUES (v_univ_strat_id, v_va_oce, 'Increase contribution to executive training and become a main provider for continuous education', 6, v_admin_id)
    RETURNING id INTO v_g_oce6;

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_g_oce6, 'Define 2 relevant Prof. courses annually. Define 6 Prof. course offering total', 1, v_admin_id)
    RETURNING id INTO v_o_oce6_9;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_o_oce6_9, 'Facilitate engagement of degree programs with all programs like EE&C', 1, v_admin_id) RETURNING id INTO v_di;

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_g_oce6, 'Increase awareness level for RIT Dubai executive education by 25% in the coming 5 years', 2, v_admin_id)
    RETURNING id INTO v_o_oce6_10;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_o_oce6_10, 'Academic programs engage with all programs, especially EE&C, in promoting executive education offerings', 1, v_admin_id) RETURNING id INTO v_di;

    -- =========================================================================
    -- DEPARTMENTS
    -- =========================================================================
    INSERT INTO department (name, code, active) VALUES ('Data Science and Artificial Intelligence', 'DSAI', true)
    ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name RETURNING id INTO v_dept_dsai;

    INSERT INTO department (name, code, active) VALUES ('EE and Computing', 'EECS', true)
    ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name RETURNING id INTO v_dept_eecs;

    INSERT INTO department (name, code, active) VALUES ('Academic Advising', 'ADV', true)
    ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name RETURNING id INTO v_dept_adv;

    INSERT INTO department (name, code, active) VALUES ('Mechanical and Industrial Engineering', 'MEIE', true)
    ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name RETURNING id INTO v_dept_meie;

    INSERT INTO department (name, code, active) VALUES ('Interactive Arts and Design', 'IAD', true)
    ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name RETURNING id INTO v_dept_iad;

    INSERT INTO department (name, code, active) VALUES ('Institute for Natural Sciences and Engineering', 'INSE', true)
    ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name RETURNING id INTO v_dept_inse;

    INSERT INTO department (name, code, active) VALUES ('Co-op and Outreach', 'COOP', true)
    ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name RETURNING id INTO v_dept_coop;

    INSERT INTO department (name, code, active) VALUES ('Math and Sciences', 'MAS', true)
    ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name RETURNING id INTO v_dept_mas;

    INSERT INTO department (name, code, active) VALUES ('Business Management', 'BUS', true)
    ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name RETURNING id INTO v_dept_bus;

    INSERT INTO department (name, code, active) VALUES ('Academic Support Center', 'ASC', true)
    ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name RETURNING id INTO v_dept_asc;

    INSERT INTO department (name, code, active) VALUES ('Liberal Arts', 'LA', true)
    ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name RETURNING id INTO v_dept_la;

    -- =========================================================================
    -- DSAI
    -- =========================================================================
    INSERT INTO strategy (planning_cycle_id, department_id, strategy_type, state, title)
    VALUES (v_cycle_id, v_dept_dsai, 'DEPARTMENT', 'DEPLOYED', 'DSAI 2022-2027 Departmental Strategic Plan')
    RETURNING id INTO v_ds_dsai;
    INSERT INTO role_assignment (user_id, strategy_id, role) VALUES (v_admin_id, v_ds_dsai, 'OWNER');

    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_ds_dsai, 'DSAI Strategic Objectives 2022-2027', 1, v_admin_id) RETURNING id INTO v_dg;

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Keep curriculum current to incorporate cutting-edge skills', 1, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar3_6);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Keep curriculum current to incorporate cutting-edge skills', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Regular checks with faculty during retreats about new trends and topics. Incorporation of agreed topics in courses.',
        'Regular checks with faculty during retreats about new trends and topics Incorporation of agreed topics in courses. Announcement of updates on the website catalog, in the news, and with stakeholders.',
        1, v_admin_id, v_ap_2224);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Ensure competitiveness of programs', 2, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Ensure competitiveness of programs', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Yearly benchmark with competing programs from other universities',
        'Yearly benchmark with competing programs from other universities', 1, v_admin_id, v_ap_2224);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Maintain accreditation status', 3, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar2_3);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Maintain accreditation status', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Follow the official calendars. Minimize requirements.',
        'Follow the official calendars. Minimize requirements', 1, v_admin_id, v_ap_2224);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Explore innovative teaching methods to keep up with educational trends', 4, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar3_6);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Explore innovative teaching methods to keep up with educational trends', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'IE to communicate a yearly executive summary on new trends and organize training for faculty.',
        'IE to communicate a yearly executive summary on new trends and organize training (self-paced online sessions) for faculty', 1, v_admin_id, v_ap_2224);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Applying innovative teaching methods or tools', 5, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar3_6);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Applying innovative teaching methods or tools', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Faculty to report during retreats on the experience in applying those methods and their impact on the learning process. Surveys to be run with students.',
        'Faculty to report during retreats on the experience in applying those methods and their impact on the learning process. Surveys to be run with students on the impact of the new methods or tools.', 1, v_admin_id, v_ap_2224);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Maintain high thesis completion rates', 6, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Maintain high thesis completion rates', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, '80% completed on time', '80% completed on time', 1, v_admin_id, v_ap_2224);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Maintain high quality of theses', 7, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar2_3);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Maintain high quality of theses', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Faculty to provide a list of research topics for theses. Target to maintain at least 10% Very Good and 30% Good.',
        'Faculty to provide a list of research topics for theses in the strategic areas of RIT Provide an internal benchmark during the defense (Very Good, Good, Average, Below Average) based on the quality and usefulness of the thesis. Target to maintain at least 10% Very Good and 30% Good.', 1, v_admin_id, v_ap_2224);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Ensure students'' proficiency in "writing" and "presentation"', 8, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Ensure students'' proficiency in "writing" and "presentation"', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Assess students'' writing and presentation skills before joining the program. Offer students preparatory courses on "Writing Skills" and "Presentation Skills".',
        'Assess students'' writing and presentation skills before joining the program. Offer students preparatory courses on "Writing Skills" and "Presentation Skills" or Executive Education before graduation.', 1, v_admin_id, v_ap_2224);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Maintain integrity standards by designing assessments resistant to cheating', 9, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar3_6);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Maintain integrity standards by designing assessments resistant to cheating', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'At least one oral exercise per course (ex: oral defense of major projects)',
        'At least one oral exercise per course (ex: oral defense of major projects)', 1, v_admin_id, v_ap_2224);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Hold symposia or Innovation Days in Spring 2024 and beyond to publicize programs', 10, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Hold symposia or Innovation Days in Spring 2024 and beyond to publicize programs', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Measure the number of respondents to the program',
        'Measure the number of respondents to the program', 1, v_admin_id, v_ap_2224);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Create separate web pages for GPR dept programs', 11, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Create separate web pages for GPR dept programs', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'End of Summer 24 Review and update every Summer',
        'End of Summer 24 Review and update every Summer', 1, v_admin_id, v_ap_2224);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Use Alumni as marketing tool', 12, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Use Alumni as marketing tool', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Identify at least 4 alumni per cohort from both genders',
        'Identify at least 4 alumni per cohort from both genders', 1, v_admin_id, v_ap_2224);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Create a database of bright and impactful students to form the core alumni', 13, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Create a database of bright and impactful students to form the core alumni', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Faculty to report bright students at the end of each course. Outreach to follow-up with students on career progress. Collect testimonials.',
        'Faculty to report bright students at the end of each course. Outreach to follow-up with the students on their career progress. Collect testimonials and add them to the news.', 1, v_admin_id, v_ap_2224);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Seek support from Advisory Board on student recruitment', 14, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Seek support from Advisory Board on student recruitment', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Organize with Advisory Board members visits to prospect organizations, consulates, and NGOs to promote programs. At least one visit per Board Member per semester.',
        'Organize with Advisory Board members visits to prospect organizations, consulates, and NGOs to promote our programs. At least one visit per Board Member during the SPRING and FALL semesters.', 1, v_admin_id, v_ap_2224);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Streamline scheduling for all 3 programs aligning them with RO', 15, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar3_6);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Streamline scheduling for all 3 programs aligning them with RO', 1, v_admin_id) RETURNING id INTO v_di;

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Empower program coordinators', 16, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar3_6);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Empower program coordinators', 1, v_admin_id) RETURNING id INTO v_di;

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Identify and address gaps in existing policies and procedures', 17, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar3_6);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Identify and address gaps in existing policies and procedures', 1, v_admin_id) RETURNING id INTO v_di;

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Through automation, improve course logistics efficiency', 18, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar3_5);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Through automation, improve course logistics efficiency: course creation on SIS and mycourses, students'' email creation, proper scheduling of SRATE timings.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'SIS, mycourses, students'' emails must be ready before course start',
        'SIS, mycourses, students'' emails must be ready before course start', 1, v_admin_id, v_ap_2224);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Full-time faculty to publish at least one paper per year', 19, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar2_3);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Full-time faculty to publish at least one paper per year', 1, v_admin_id) RETURNING id INTO v_di;

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Encourage faculty-student cooperation in converting theses to joint publication', 20, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar2_3);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Encourage faculty-student cooperation in converting theses to joint publication', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'At least 1 thesis per cohort converted to a publication',
        'At least 1 thesis per cohort converted to a publication', 1, v_admin_id, v_ap_2224);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Maintain high quality in theses and encourage student publication following thesis', 21, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar2_3);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Maintain high quality in theses and encourage student publication following thesis', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, '1-2 per cohort', '1-2 per cohort', 1, v_admin_id, v_ap_2224);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Faculty to attend at least one conference per year to present research and/or publicize programs', 22, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar2_3);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Faculty to attend at least one conference per year to present research and/or publicize programs', 1, v_admin_id) RETURNING id INTO v_di;

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Faculty to provide a list of research topics for theses in the strategic areas of RIT', 23, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar3_5);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Faculty to provide a list of research topics for theses in the strategic areas of RIT', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Topics to be published before 2nd semester', 'Topics to be published before 2nd semester', 1, v_admin_id, v_ap_2224);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Build relationships with govt departments, industry, and NGOs via Advisory Board', 24, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Build relationships with govt departments, industry, and NGOs, and involve them via Advisory Board seats, curriculum involvement, executive education', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Revise advisory board on a yearly basis. Try to attract advisors from government, industry, and NGO sectors.',
        'Revise advisory board on a yearly basis. Try to attract advisors from government, industry, and NGO sectors', 1, v_admin_id, v_ap_2224);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Collect challenges from the advisory board members to be converted to thesis topics', 25, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Collect challenges from the advisory board members to be converted to thesis topics. Ask contacts to provide possible datasets and/or mentors around those challenges.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'List of challenges from every board member', 'List of challenges from every board member', 1, v_admin_id, v_ap_2224);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Establish community outreach programs involving students and faculty providing data analytics services', 26, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Establish community outreach programs that involve students and faculty members providing data analytics services to local businesses, nonprofits, or government agencies', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Track the number of outreach programs initiated and completed. Assess the impact of DA, SC and FFP services on the community partners.',
        'Track the number of outreach programs initiated and completed. Assess the impact of DA, SC and FFP services on the community partners. Collect feedback from community partners on the value of the program.', 1, v_admin_id, v_ap_2224);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Integrate projects focused on social impact into the curriculum', 27, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Integrate projects focused on social impact, such as solving community challenges or addressing societal issues, into the curriculum.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Evaluate the success of projects in achieving social impact goals. Measure engagement and satisfaction of students.',
        'Evaluate the success of projects in achieving social impact goals. Measure the engagement and satisfaction of students involved in these projects. Collect testimonials or case studies showcasing the positive outcomes.', 1, v_admin_id, v_ap_2224);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Collaborate with local educational institutions to offer workshops and mentorship programs in data analytics', 28, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Collaborate with local educational institutions to offer workshops, seminars, or mentorship programs aimed at promoting data analytics in schools and colleges.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Track the number of collaborative initiatives with local schools. Assess impact of workshops and programs on student awareness.',
        'Track the number of collaborative initiatives with local schools. Assess the impact of workshops and programs on student awareness and interest in data analytics and smart cities'' topics. Collect feedback from educators and students involved.', 1, v_admin_id, v_ap_2224);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Engage students and faculty in research projects that address challenges faced by the local community', 29, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Engage students and faculty in research projects that address specific challenges faced by the local community, demonstrating the practical applications of data analytics.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Evaluate the relevance and impact of research projects on community issues. Measure the extent to which research findings are adopted.',
        'Evaluate the relevance and impact of research projects on community issues. Measure the extent to which research findings are adopted or implemented by the community. Assess student and faculty involvement in community-focused research.', 1, v_admin_id, v_ap_2224);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Seek recognition and awards from local organizations for the program''s positive impact on the community', 30, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Seek recognition and awards from local organizations or authorities for the program''s positive impact on the community.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Monitor awards and recognitions received by the program. Assess the visibility and credibility of the program in the community.',
        'Monitor awards and recognitions received by the program. Assess the visibility and credibility of the program in the community. Track community sentiment and support following awards.', 1, v_admin_id, v_ap_2224);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Invite students to innovation and entrepreneurship trainings during the year', 31, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Invite students to innovation and entrepreneurship trainings during the year.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Develop statistical reports on number of students joining these activities. Collect surveys about students'' feedback on the value of these events.',
        'Develop statistical reports on number of students joining these activies Collect surveys about students'' feedback on the value of these events', 1, v_admin_id, v_ap_2224);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Organize hackathons, data competitions, and coding challenges within the RIT strategic research centers', 32, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Organize hackathons, data competitions, and coding challenges to stimulate problem-solving skills within the RIT strategic research centers and foster a competitive yet collaborative environment.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, '1 challenge per semester in a selected theme. Report on number of contributions per semester.',
        '1 challenge per semester in a selected theme related to one of the RIT strategic research centers from DA or SC perspectives. Report on number of contributions per semester.', 1, v_admin_id, v_ap_2224);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Invite entrepreneurs and innovators to deliver guest lectures and share insights into emerging trends', 33, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Invite entrepreneurs and innovators to deliver guest lectures and share insights into emerging trends and technologies in data analytics.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Try to get one guest lecture per delivered course.',
        'Try to get one guest lecture per delivered course.', 1, v_admin_id, v_ap_2224);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Establish mentorship programs connecting students with industry professionals', 34, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Establish mentorship programs connecting students with industry professionals to provide guidance and networking opportunities.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Start developing a mentors'' database through our network. Evaluate the mentors'' contribution to students'' theses.',
        'Start developing a mentors'' database through our network. Evaluate the mentors'' contribution to students'' theses.', 1, v_admin_id, v_ap_2224);

    -- =========================================================================
    -- EECS
    -- =========================================================================
    INSERT INTO strategy (planning_cycle_id, department_id, strategy_type, state, title)
    VALUES (v_cycle_id, v_dept_eecs, 'DEPARTMENT', 'DEPLOYED', 'EECS 2022-2027 Departmental Strategic Plan')
    RETURNING id INTO v_ds_eecs;
    INSERT INTO role_assignment (user_id, strategy_id, role) VALUES (v_admin_id, v_ds_eecs, 'OWNER');

    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_ds_eecs, 'EECS Strategic Objectives 2022-2027', 1, v_admin_id) RETURNING id INTO v_dg;

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Advocate academic excellence and maintain alignment with main campus; foster zero tolerance to cheating', 1, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar3_6);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Advocate by example teaching rigor and effective learning environment. Maintain alignment with the main campus on instruction, curricula, and faculty qualifications through mutual visits, GLEC platform.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'NY assigned point of contacts for CIT and CSEC. GLEC committee is engaging to incorporate the input of RIT Dubai into the updates to the curriculum. AI option has been added to the BSEE degree.',
        'NY assigned point of contacts for CIT and CSEC. GLEC committee is enaging to incorporate the input of RIT Dubai into the updates to the curriculum. iSchool director and assistant director are planning a visit to RIT Dubai. AI option has been added to the BSEE degree.',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'The department has submitted two dual degree applications to NY and received approval. The degree options offer high-achieving students an accelerated path to complete the BS and MS degrees in EE and Cybersecurity in 5 years.',
        'The department has submitted two of the dual degree applications to NY and received approval. The degree options offer high-achieving students an accelerated path to complete the BS and MS degrees in EE and Cybersecurity in 5 years. The ministry requirements for the three computing programs have been addressed.',
        1, v_admin_id, v_ap_2425);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Create research groups and assign faculty mentors; ensure diversity', 2, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar1_1);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Create research groups with focus on themes such as health care and digital transformation. Assign a faculty mentor for each course or area. Ensure diversity.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'A research group in AI has been established in Fall 2023 with focus on healthcare. Capstone theme in digital transformation will be introduced in Fall 2024.',
        'A research group in AI has been established in Fall 2023 with focus on healthcare. Capstone theme in digital transfromation will be introduced in Fall 2024. Faculty have been supporting each other through mentorship.',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'The Digital Twins course has been designed to serve both engineering and computing students, reflecting strong interdisciplinary collaboration between EECS and MEIE departments.',
        'The Digital Twins course has been designed to serve both engineering and computing students. This offering reflects a strong interdisciplinary collaboration between the EECS and MEIE departments, with faculty from computing and industrial engineering jointly delivering the course content.',
        1, v_admin_id, v_ap_2425);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Create new framework for capstone operation and standing committees', 3, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar1_1);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Create a new framework for the capstone operation to improve faculty and student engagement. Add standing committees. Establish a working group for the Gen AI roll out plan. Create a rotation plan.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'The new capstone framework has been established since Fall 2023 along with a standing committee and is leading to several improvements in the delivery of the projects.',
        'The new capstone framework has been in established since Fall 2023 along with a standing committee and is leading to several improvements in the delivery of the projects. The model of the capstone and research committees is promising and will be enhanced and replicated in other areas.',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'A new lead has been assigned for the CIT program. A new committee has been created to review and enhance the process for Master''s theses and graduate papers/capstones.',
        'A new lead Dr. Ali Assi has been assigned for the CIT program. A new committee has been created to review and enhance the process for Master''s theses and graduate papers/capstones within the EE and Cybersecurity programs.',
        1, v_admin_id, v_ap_2425);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Promote better research production and publications; secure research funds', 4, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar2_3);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Promote better research production evidenced by quality publications and possibly patents and collaboration with top 200 universities. Organize one symposium and one conference by 2024. Secure new internal and external research funds.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'All EECS full-time faculty members have produced publications in 2021-22 and 2022-23. AI research group established in Fall 2023 with 7 members.',
        'All EECS full-time faculty members have prodcued publications in 2021-22, and 2022-23 and many have recived research grants. Research Committee established since the start of 2021-22. AI research group has been established starting in Fall 2023 with 7 members and counting.',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'EECS full-time faculty members have produced publications including journal and conference papers. Three RDI grants have been secured with a total of AED 2 millions.',
        'EECS full-time faculty members have produced publications including journal and conference papers. Three RDI grants have been secured with a total of AED 2 millions. The RDI grants include collaborations with universities, such as MBRU, UoD, AUD, and McGill, in addition to government entities.',
        1, v_admin_id, v_ap_2425);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Engagements in feasibility studies, new programs, accreditation, and competitions with partners', 5, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_oce1_1);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Engagements in the feasibility studies and in developing new minors, concentrations, and majors. Engagements in the accreditation processes. New competitions and challenges in collaboration with partners. Establish capstone project themes.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Capstones and theses sponsored by industry since Fall 2022. Collaboration with NewBridge Pharmaceutical, TechMed, MBRU, WIA, and Fakeeh Hospital on various AI based projects.',
        'Since Fall 2022, capstones and theses were sponsored by industry. Collaboration with NewBridge Pharmaceutical, TechMed, MBRU, WIA, and Fakeeh Hospital on various AI based projects utlizing the capability of the DT lab. Dr. Jinane and Dr. Omar developed a drone AI-enabled system for agriculture.',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'The EECS Department delivered training in Digital Design to 200 Year 12 students from DIA. Dr. Jinane received USD 10,000 from Rochester to form a team of students.',
        'The EECS Department delivered training in Digital Design to 200 Year 12 students from DIA, with support from volunteer EE students and members of the AI/Robotics student group. Dr. Jinane received USD 10,000 from Rochester to form a team of students who will work in collaboration with the NY campus.',
        1, v_admin_id, v_ap_2425);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Expand RIT Dubai degree offerings in ICT; reach out to key partners and government entities', 6, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar3_6);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Expand RIT Dubai degree offerings in ICT by adding new degrees, minors/options, dual BS/MS degrees, and micromasters. Reach out to key partners and government entities to sponsor elite students. Offer GRAs to qualified students.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'A support system has been put in place since Fall 2023 to allow EECS faculty to participate in open days. Worked out an agreement with Emirates Airlines to sponsor 5 students in CIT. Dubai Police sponsored students.',
        'A support system has been put in place since Fall 2023 to allow EECS faculty to participate in open days, school visits to RIT, and workshops. Worked out an agreement with Emirates Airlines to sponsor 5 students in CIT. Dubai Police sponsored students.',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'The dual BS/MS degree in EE and CSEC has received approval from New York, and a proposal has been submitted to the Ministry of Education for accreditation.',
        'The dual BS/MS degree in EE and CSEC has received approval from New York, and a proposal has been submitted to the Ministry of Education (MOE) for accreditation. Plans are underway to introduce new academic programs to the department, including a BS in Computer Engineering and an MS in Artificial Intelligence.',
        1, v_admin_id, v_ap_2425);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Maintain attrition below 10%; promote efficient student advising and diversify elective offerings', 7, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar1_2);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Maintain attrition to below 10%. Promote efficient student advising and better engagements. Diversify elective offerings. Help create new co-op opportunities. Offer students more co-curricular activities through clubs and advanced labs.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Attrition rate averaged over all EECS programs is at 9% in 2022-23. Created new industry co-op placements through the Cyber-forward program in collaboration with DIFC and Mastercard.',
        'Attrition rate averaged over all EECS programs is at 9% in 2022-23. Created new industry co-op placement through the Cyber-forward program in collaboration with DIFC and Mastercard. EECS supported research co-op placements since Fall 2022.',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Six student teams presented their projects at the AI Conference hosted by GDRFA during AI Week. A team of students in Electrical Engineering secured first place in the 12th Undergraduate Research and Innovation Competition 2025.',
        'Six student teams presented their projects at the AI Conference hosted by GDRFA during AI Week. A team of students in Electrical Engineering secured first place in the 12th Undergraduate Research and Innovation Competition 2025 organized by Abu Dhabi University (Spring 2025).',
        1, v_admin_id, v_ap_2425);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Secure BSEE, MSEE, CIT accreditations with ABET and Ministry; substantive changes for CSEC', 8, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar2_3);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Secure the BSEE accreditation with ABET. Secure the BSEE, MSEE, CIT accreditations with the ministry. Secure the approval of substantive changes for the BS CSEC with the ministry.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'The BSEE program accreditation has been renewed by ABET till Sept. 30, 2028. The BSEE, MSEE, and CIT program accreditations have been renewed by the ministry till Dec. 2028.',
        'The BSEE program accreditation has been renewed by ABET till Sept. 30, 2028. The BSEE program accreditation has been renewed by the ministry till Dec. 18, 2028. The MSEE program accreditation has been renewed by the ministry till Dec. 2028. The CIT program accreditation has been renewed by the ministry.',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'All program accreditations maintained. BSEE renewed by ABET till Sept. 30, 2028. All ministry accreditations for BSEE, MSEE, and CIT confirmed till Dec. 2028.',
        'The BSEE program accreditation has been renewed by ABET till Sept. 30, 2028. All ministry accreditations for BSEE, MSEE, and CIT confirmed till Dec. 2028.',
        1, v_admin_id, v_ap_2425);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Add one degree in the ICT domain within the next 2 years', 9, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar3_6);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Add one degree in the ICT domain within the next 2 years: Computer Engineering, Computer Science, or a master''s degree in collaboration with NY (50% online; 50% in person).', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'SE minor added since Fall 2022. The AI degree option added since Fall 2023. The MSEE degree was redesigned with focus areas in AI/Robotics and smart energy since Fall 2023.',
        'SE minor has been added since Fall 2022. The AI degree option has been added since Fall 2023. The MSEE degree was redesigned with focus areas in AI/Robotics and smart energy since Fall 2023 to support the demand of our students. At least one micromaster application will be prepared by the end.',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'The BS in Computer Engineering has been planned to be added. The MS in AI degree in collaboration with NY carries a lot of promise and has been planned to be added.',
        'The BS in Computer engineering has been planned to be added to the department''s programs. The MS in AI degree in collaboration with NY (50% online; 50% in person) carries a lot of promise and has been planned to be added. A minor in Game Design and Development has been considered.',
        1, v_admin_id, v_ap_2425);

    -- =========================================================================
    -- ADV (Academic Advising)
    -- =========================================================================
    INSERT INTO strategy (planning_cycle_id, department_id, strategy_type, state, title)
    VALUES (v_cycle_id, v_dept_adv, 'DEPARTMENT', 'DEPLOYED', 'Academic Advising 2022-2027 Departmental Strategic Plan')
    RETURNING id INTO v_ds_adv;
    INSERT INTO role_assignment (user_id, strategy_id, role) VALUES (v_admin_id, v_ds_adv, 'OWNER');

    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_ds_adv, 'Academic Advising Strategic Objectives 2022-2027', 1, v_admin_id) RETURNING id INTO v_dg;

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Offer greater support for graduate students; improve access to program information', 1, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Offer greater support for graduate students to understand their program requirements and improve access to program information for students', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Information sessions held at least once a year for each graduate program. Advisors to visit the first class of each graduate program cohort to introduce themselves. Clarify thesis/capstone process and make guidelines available.',
        'IN PROGRESS 1- Information sessions held at least once a year for each graduate program 2- Advisors to visit the first class of each graduate program cohort to introduce themselves 3- Clarify thesis/capstone process and make guidelines available PENDING 1- Look into using MyCourses or my.rit.edu.',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Information sessions held for graduate programs on graduation requirements, thesis process, academic policies. Worked with faculty and students to clean up confusion around Thesis enrollment and grading.',
        'Information sessions held for graduate programs on graduation requirements, thesis process, academic policies. Worked with faculty and students to clean up confusion around Thesis enrollment and grading. Held 1st Minors and Immersion Fair to introduce options to all undergraduate students.',
        1, v_admin_id, v_ap_2425);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Develop program of support for students on Probation and Deferred Suspension; streamline Suspension appeal process', 2, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Develop a program of support for students on Probation and Deferred Suspension, streamline the Suspension appeal process, and analyze success of students who start in foundation courses.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'COMPLETED: Coordinate with ASC to offer a Probation Workshop each semester. Move Suspension Appeal Form to Google Forms. Shorten timeline of appeals process.',
        'COMPLETED 1- Coordinate with ASC to offer a Probation Workshop each semester 2- Coordinate with ASC to offer a Study Skills course for students with Deferred Suspensions 3- Move Suspension Appeal Form to Google Forms 4- Shorten timeline of appeals process to meet RO deadlines 5- Tighten criteria.',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Worked with ASC to offer Probation Workshops to all students on Probation earlier in the semester. Clarified and streamlined Suspension Appeals process; unified and updated email templates.',
        'Worked with ASC to offer Probation Workshops to all students on Probation earlier in the semester. Clarified and streamlined Suspension Appeals process more, unified and updated email templates for accepted appeals, rejected appeals, email to parents/guardians.',
        1, v_admin_id, v_ap_2425);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Develop online repository of student advising resources; go paperless; create standardized documents', 3, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Develop an online repository of student advising resources. Go paperless and move all internal forms to Google Forms. Create standardized documents to capture important processes.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'COMPLETED: Share Advising calendar on Academic Dashboard. Create dubai_advising listserv. Independent Course Teaching Requests, Online Course Exceptions, Suspension Appeals Forms moved to Google Sheets.',
        'COMPLETED 1- Share Advising calendar on Academic Dashboard 2- Create dubai_advising listserv so faculty/staff can easily contact all advisors at once 3- Independent Course Teaching Requests, Online Course Exceptions, Suspension Appeals Forms moved to Google Sheets IN PROGRESS 1- Update and maintain.',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Educated students on the Advising pages on the RIT Dubai website. Continued to develop the "Advising Process and Procedures" document. Developed New Advisor Training.',
        'Educated students on the Advising pages on the RIT Dubai website in more places to expand student awareness of resources available. Continued to develop our "Advising Process and Procedures" document to ensure all advisors are on the same page. Developed New Advisor Training.',
        1, v_admin_id, v_ap_2425);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Increase availability and responsiveness of advisors; hiring plan; develop working relationships with department chairs', 4, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Increase availability and responsiveness of advisors to students, faculty, and staff. Hiring plan to reduce advisor-advisee ratio. Develop working relationship with other offices and Department Chairs. Professional development in interpersonal skills.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'COMPLETED: Meet with Admissions each semester to review Orientation. New hires to meet with Chairs during training. Share Advising calendar on Academic Dashboard. Email instructors when adding new students beyond add/drop deadline.',
        'COMPLETED 1- Meet with Admissions each semester to review how Orientation went and suggest improvements 2- New hires to meet with Chairs during training 3- Share Advising calendar on Academic Dashboard 4- Email instructors when adding new students in beyond add/drop deadline IN PROGRESS 1- All advisors.',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Advisors of graduate programs to attend graduate orientation. Provided training session for new faculty and new Faculty Advisors on advising processes. Continued to request the hiring of more advisors.',
        'Advisors of graduate programs to attend graduate orientation. Provided training session for new faculty and new Faculty Advisors on advising processes. Continued to request the hiring of more advisors to reduce student/advisor ratio. Created and shared Learning Outcomes of Advising.',
        1, v_admin_id, v_ap_2425);

    -- =========================================================================
    -- MEIE (Mechanical and Industrial Engineering)
    -- =========================================================================
    INSERT INTO strategy (planning_cycle_id, department_id, strategy_type, state, title)
    VALUES (v_cycle_id, v_dept_meie, 'DEPARTMENT', 'DEPLOYED', 'MEIE 2022-2027 Departmental Strategic Plan')
    RETURNING id INTO v_ds_meie;
    INSERT INTO role_assignment (user_id, strategy_id, role) VALUES (v_admin_id, v_ds_meie, 'OWNER');

    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_ds_meie, 'MEIE Strategic Objectives 2022-2027', 1, v_admin_id) RETURNING id INTO v_dg;

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Enhance curriculum; foster innovation via interdisciplinary projects; maintain accreditation; explore innovative teaching methods', 1, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar3_6);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Enhance the curriculum to incorporate new industry-relevant focus areas & certificates. Foster a culture of innovation via interdisciplinary projects and partnerships with local industries. Maintain local and international accreditation status. Explore innovative teaching methods.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Level of attainment rates of PLOs. Employability data. Number of MSD & theses sponsored by industry. % increase in student satisfaction rate re-cheating.',
        'Level of attainment rates of PLOs - Employability data - # of MSD & theses sponsored by industry - % increase in student satisfaction rate re-cheating', 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'The BSIE curriculum now includes a new course titled Data Analytics and Predictive Modeling. BSME, BSIE & MSME CAA reaccredited till 2028. A desktop filament maker, plastic shredder, and polymer dryer have been added to the lab.',
        'The BSIE curriculum now includes a new course titled Data Analytics and Predictive Modeling to align with recent industry trends and equip students with essential skills in this evolving field. BSME, BSIE & MSME CAA reaccredited till 2028. A desktop filament maker, plastic shredder, and polymer dryer added.',
        1, v_admin_id, v_ap_2425);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Enhance outreach workshops; increase faculty involvement in open days; create alumni opportunities', 2, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Enhance the current "Student for a Day" workshops to include more advanced and contemporary topics. Increase faculty & staff involvement in open days and introduce summer boot camps. Centralize all programs under one comprehensive webpage. Create opportunities for alumni.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, '% increase in enrollment rate. % increase in retention rate.',
        '% increase in enrollment rate - % increase in retention rate', 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'The ISEE 795 graduate seminar, offered for the first time, is being conducted via Zoom to enhance accessibility. BSME enrollment increased from 56 to 61 (Fall 2023 vs. Fall 2024). BSIE enrollment increased.',
        'The ISEE 795 graduate seminar, offered for the first time and similar to the MECE one, is being conducted via Zoom to enhance accessibility. BSME enrollment increased from 56 to 61 (Fall 2023 vs. Fall 2024). BSIE enrollment increased.',
        1, v_admin_id, v_ap_2425);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Develop faculty mentorship program; explore funding sources; increase support staff', 3, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Develop faculty mentorship program. Explore various funding sources to support faculty development. Promote a culture of mentorship and collaboration among faculty. Increase support staff members.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Faculty turnover rate. Faculty size. Department faculty co-authorship on papers.',
        'Faculty turnover rate - Faculty size - Department faculty co-authorship on papers', 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'A budget of AED 20k has been utilized for faculty development activities for the 2024-25 academic year. Successfully hired an ISE adjunct at the senior executive level from GE Aerospace.',
        'A budget of AED 20k has been utilized for faculty development activities for the 2024-25 academic year. Newly joined faculty member has been thoroughly briefed on the faculty evaluation rubrics. Successfully hired an ISE adjunct at the senior executive level from GE Aerospace.',
        1, v_admin_id, v_ap_2425);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Streamline admin processes; assign and recognize faculty administrative roles; automate PLO assessment', 4, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar3_5);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Streamline admin processes to reduce workload and enhance focus on teaching and research. Assign and recognize faculty administrative roles. Identify and address gaps in existing policies and procedures. Automate PLO assessment and benchmarking.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Faculty satisfaction rate with admin processes. Time spent compiling data and TaskStream.',
        'Faculty satisfaction rate with admin processes - Time spent compiling data and TaskStream', 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'For the BSME program-level assessment, all CLO data are now directly extracted from CARs to streamline the process. Three of the four programs have designated program coordinators.',
        'For the BSME program-level assessment, all CLO data are now directly extracted from CARs to streamline the process and avoid redundancy, eliminating the need for Excel sheets. Three of the four programs have designated program coordinators. All four programs received a detailed review.',
        1, v_admin_id, v_ap_2425);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Develop research groups and labs; network with industry leaders; collaborate with institutions; encourage student involvement in research', 5, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar2_3);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Develop research groups and labs with state-of-the-art equipment. Network with industry leaders to attract research funding. Collaborate with local and international institutions, industries, and government agencies. Encourage student involvement in research.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Number of publications/faculty. Number of conference talks/faculty. Number of research groups. Number of grants acquired.',
        'Number of publications/faculty - Number of conference talks/faculty - Number of research groups - Number of grants acquired', 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Secured AED 84,000 in funding from one of the MIT DesignX startup participants, through DSO, to support research focused on innovative cooling technologies for GPUs. The department hosted its first-ever research COOP student from the main campus.',
        'Secured AED 84,000 in funding from one of the MIT DesignX startup participants, through DSO, to support research focused on innovative cooling technologies for GPUs. The department hosted its first-ever research COOP student from the main campus. Multiple faculty publications, including journal contributions.',
        1, v_admin_id, v_ap_2425);

    -- =========================================================================
    -- IAD (Interactive Arts and Design / Business Management)
    -- =========================================================================
    INSERT INTO strategy (planning_cycle_id, department_id, strategy_type, state, title)
    VALUES (v_cycle_id, v_dept_iad, 'DEPARTMENT', 'DEPLOYED', 'IAD 2022-2027 Departmental Strategic Plan')
    RETURNING id INTO v_ds_iad;
    INSERT INTO role_assignment (user_id, strategy_id, role) VALUES (v_admin_id, v_ds_iad, 'OWNER');

    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_ds_iad, 'IAD Strategic Objectives 2022-2027', 1, v_admin_id) RETURNING id INTO v_dg;

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Curriculum Enhancement and Teaching Quality Improvement', 1, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar3_6);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Review and update the curriculum at least once every academic year. Incorporate at least one new cutting-edge course each academic year. Conduct or attend at least two faculty development programs per year.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Curriculum enhancement takes place every semester based on retreat results. New course added under MGMT489 on "Business Digital Transformation" delivered first time in Fall 2023.',
        'Curriculum enhancement takes place every semester and cumulatively once a year based on retreat results. New course added under MGMT489 Seminar in Management on "Business Digital Transformation" delivered first time in Fall 2023. Learning Platforms and Simulations are added to the curriculum.',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Curriculum review and update conducted during 2024-2025 for both undergraduate and graduate programs; refinements aligned with QFEmirates and accreditation frameworks. Various courses proposed for launch.',
        'Curriculum Enhancement Curriculum review and update conducted during 2024-2025 for both undergraduate and graduate programs; refinements aligned with QFEmirates and accreditation frameworks. Various courses proposed for launch, and revisions initiated in existing courses to reflect digital transformation.',
        1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Curriculum to be reviewed across all undergraduate programs, with proposed updates submitted by May 2026. At least one new course related to emerging business trends or technologies will be introduced.',
        'Curriculum to be reviewed across all undergraduate programs, with proposed updates submitted by May 2026. At least one new course related to emerging business trends or technologies (e.g. AI in Marketing, digital business) will be introduced. A minimum of 80% of full-time faculty will attend at least one FD program.',
        1, v_admin_id, v_ap_2526);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Diverse Teaching Strategies and Innovative Assessment Methods', 2, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar3_6);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'At least 60% of faculty implementing at least one diverse teaching strategy. At least 50% of courses adopt alternative assessment methods.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, '60% of faculty implement at least one diverse teaching strategy. 60% of faculty adopt some form of differentiated assessment strategy.',
        '60% of faculty implement at least one diverse teaching strategy (problem-solving, experiential learning, flipped classroom, case-based and project-based teaching). 60% of faculty adopt some form of differentiated assessment strategy that involves projects, presentations, or case studies.',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Over 60% of faculty reported implementing active learning strategies such as flipped classrooms, team-based simulations, role plays, problem-solving labs, and experiential group projects.',
        'Diverse Teaching Strategies Over 60% of faculty reported implementing active learning strategies such as flipped classrooms, team-based simulations, role plays, problem-solving labs, and experiential group projects. Departmental student satisfaction surveys consistently indicate positive responses.',
        1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'At least 60% of faculty to implement one or more innovative teaching strategies. At least 50% of courses will incorporate alternative assessments such as case studies, peer evaluations, or portfolios.',
        'At least 60% of faculty to implement one or more innovative teaching strategies (e.g. flipped classrooms, simulations, problem-based learning). At least 50% of courses will incorporate alternative assessments such as case studies, peer evaluations, or portfolios.',
        1, v_admin_id, v_ap_2526);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Course Material Development and Research Initiatives', 3, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Involve students in at least 15% of all course material development initiatives. Integrate peer assessment in at least 35% of courses. 10% increase in the number of students involved in faculty-led research projects annually.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Two major meetings conducted in Spring 2023 with students from all majors. Faculty-Students Research Initiatives to be measured first time at the end of Spring 2024.',
        'Student involvement in material development (e.g. TAs). Peer Assessment. Faculty-Students Research Initiatives to be measured first time at the end of Spring 2024. Two major meetings conducted in Spring 2023 with students from all majors in collaboration with student government.',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Student input was incorporated into the design of pitching competitions, simulation exercises, and in-class leadership case discussions, meeting the 15% target.',
        'Course Material Development Student input was incorporated into the design of pitching competitions, simulation exercises, and in-class leadership case discussions, meeting the 15% target. Instructors in MGMT, Marketing, and Finance courses reported revising assignments based on student feedback.',
        1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'At least 15% of courses to involve students in elements of course material development or feedback processes. Peer assessment to be embedded in at least 35% of courses.',
        'At least 15% of courses to involve students in elements of course material development or feedback processes. Peer assessment to be embedded in at least 35% of courses across undergraduate and graduate programs. The number of students participating in faculty-led research projects to increase by 10%.',
        1, v_admin_id, v_ap_2526);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Maintain Existing Accreditations and Pursue New Accreditations', 4, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar2_3);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Ensure 100% renewal of existing accreditations (CAA, AACSB) in the next five years. Maintain a faculty composition where at least 80% meet or exceed the qualifications outlined by accrediting bodies.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'AACSB Accreditation due in Spring 2025. SCA for BS GBM approved (September 2022). SCA for MS OLI approved (June 2024). In the process of identifying one new international accreditation.',
        'AACSB Accreditation due in Spring 2025. SCA for BS GBM approved (September 2022). SCA for MS OLI approved (June 2024). Self Studies for BS in GBM, Finance, and Marketing not needed due to dual accreditation (AACSB and CAA). In the process of identifying one new international accreditation.',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'AACSB re-accreditation successfully completed; department met all core standards including AoL, faculty qualifications, curriculum alignment, and deployment. 80% of faculty classified as SAs under AACSB guidelines.',
        'Maintain Existing Accreditations AACSB re-accreditation successfully completed during the review period; department met all core standards, including AoL, faculty qualifications, curriculum alignment, and deployment. 80% of faculty classified as SAs under AACSB guidelines; faculty sufficiency confirmed.',
        1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Faculty qualification records as per AACSB standards to be reviewed and maintained to ensure that at least 80% of faculty meet AACSB standards. All course assessment reports, AoL reports, and curriculum mapping submissions will be completed and submitted on schedule.',
        'Faculty qualification records as per AACSB standards (SA, PA, etc.) to be reviewed and maintained to ensure that at least 80% of faculty meet AACSB standards. All course assessment reports (CARs), AoL reports, and curriculum mapping submissions will be completed and submitted on schedule.',
        1, v_admin_id, v_ap_2526);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Research Culture and Publications Quality', 5, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar2_3);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Organize or ensure 80% of faculty participate in at least one research-focused workshop or seminar per year. 10% increase in students involved in faculty-led research. 20% of faculty publications to be in Q1/Q2 Scopus-indexed journals.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'In the process of organizing a faculty seminar series pertaining to all RIT Dubai. Need to pursue more actively participation of students in faculty-led projects.',
        'In the process of organizing a faculty seminar series pertaining to all RIT Dubai. Expected day of event in late April-early May 2024. Need to pursue more actively participation of students in faculty-led projects (typically Masters students when the MS OLI is under way).',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Over 90% of faculty participated in at least one research workshop, peer review session, or interdisciplinary collaboration planning session. Student participation in research grew by more than 10%.',
        'Research Culture Over 90% of faculty participated in at least one research workshop, peer review session, or interdisciplinary collaboration planning session. Student participation in research grew by more than 10%, particularly in projects tied to course-based data analysis and OB/strategy research.',
        1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'At least one department-wide research workshop or seminar to be organized, with 80% of faculty in attendance. A minimum of 20% of total faculty publications to appear in Q1 or Q2 Scopus-indexed journals.',
        'At least one department-wide research workshop or seminar to be organized, with 80% of faculty in attendance. The number of students involved in faculty-led research projects to grow by at least 10% over the previous year. A minimum of 20% of total faculty publications to appear in Q1 or Q2 Scopus-indexed journals.',
        1, v_admin_id, v_ap_2526);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Interdisciplinary Collaboration and Incentivization', 6, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'At least 10% of all active research projects in the Department to involve collaboration across different departments within the university. Implement annual awards for the best interdisciplinary research project.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Faculty to begin engagement with colleagues across the University or across campuses on research projects (expected Fall 2024). Annual rewards for best interdisciplinary projects planned.',
        'Faculty to begin engagement with colleagues across the University or across campuses on research projects (expected Fall 2024). As part of the expected implementation of research seminar series, initialize annual rewards for best interdisciplinary projects (expected Fall 2024).',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Over 10% of active departmental research projects involved collaboration across disciplines (Finance, Analytics, Management) with efforts to collaborate with Engineering and Computing departments.',
        'Collaboration Rates Over 10% of active departmental research projects involved collaboration across disciplines (e.g. Finance, Analytics, Management) and efforts have been done to collaborate with other departments within RIT Dubai, notably with Engineering and Computing in areas like AI, analytics.',
        1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'At least 10% of active departmental research projects to involve collaboration with other departments at RIT Dubai or RIT Global campuses. An annual award should be launched.',
        'At least 10% of active departmental research projects to involve collaboration with other departments at RIT Dubai or RIT Global campuses. An annual award should be launched to recognize the best interdisciplinary research project based on innovation and cross-disciplinary engagement.',
        1, v_admin_id, v_ap_2526);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Partnership Development and Co-op Engagement', 7, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Establish a minimum of three new partnerships per year with local businesses, non-profit organizations, and government agencies. 15% year-over-year increase in co-op placements available to students.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Ongoing discussions with the Co-Op Office to establish new partnerships on a yearly basis. Initial results expected at the end of Fall 2024.',
        'Ongoing discussions with the Co-Op Office to establish new partnerships on a yearly basis. Initial results expected at the end of Fall 2024. In collaboration with the Co-Op Office, identify the number of coop places available to department''s students in 2022 and 2023.',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Three new partnerships were established during 2024-2025 with Columbus State University (USA), Golisano Institute for Business and Entrepreneurship (USA), and Coventry University (UK).',
        'Partnership Development Three new partnerships were established during 2024-2025 with Columbus State University (USA), Golisano Institute for Business and Entrepreneurship (USA), and Coventry University (UK), with student-facing activities and public engagements conducted.',
        1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'The department to coordinate with the Co-op Office to establish at least three new partnerships with businesses, NGOs, or government bodies. Co-op placement opportunities to increase by at least 15%.',
        'The department to coordinate with the Co-op Office to establish at least three new partnerships with businesses, NGOs, or government bodies across diverse sectors. Co-op placement opportunities for students to increase by at least 15% compared to the previous academic year.',
        1, v_admin_id, v_ap_2526);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Start-up Competition Organization and Mentorship', 8, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Organize at least one startup competition per academic year. Establish a mentorship program where at least 60% of the participating teams are paired with mentors.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'As a Business and Management Department, there is a need to engage in start-up competitions for students. Expected to start in Fall 2024.',
        'As a Business and Management Department, there is a need to engage in start-up competitions for students. Expected to start in Fall 2024. Upon successful completion of the first start-up competition, provisions need to be made for mentorship programs for finalists.',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Successfully organized two major competitions: MGMT 102 Pitching Competition (Spring 2025) with over 100 student participants; Innovation and Entrepreneurship Day (Fall 2024).',
        'Competition Organization and Participation Successfully organized two major competitions: (a) MGMT 102 Pitching Competition (Spring 2025) with over 100 student participants across all sections, exceeding the 20% departmental participation target. (b) Innovation and Entrepreneurship Day (Fall 2024).',
        1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'At least two start-up or innovation-focused competitions to be organized during the academic year, targeting participation from at least 20% of the business student body. Industry mentors to be paired with at least 50% of competing teams.',
        'At least two start-up or innovation-focused competitions to be organized during the academic year, targeting participation from at least 20% of the business student body. Industry mentors to be paired with at least 50% of competing teams, with mentorship lasting a minimum of six months.',
        1, v_admin_id, v_ap_2526);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Events Organization and External Engagement', 9, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Organize at least three major public events each academic year and establish at least one flagship annual event. 70% of faculty and 30% of external participants per event.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'The Department organized the 1st Gen AI Symposium in Jan 2024 attended by 173 in-person and 33 online delegates of whom 70% were from outside RIT Dubai. The event is expected to be the flagship annual event.',
        'The Department organized the 1st Gen AI Symposium in Jan 2024 attended by 173 in-person and 33 online delegates of whom 70% were from outside RIT Dubai. The event is expected to be the flagship annual event for the Department and serve as a benchmark for its thought leadership.',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'The department organized three major events during 2024-2025: Innovation and Entrepreneurship Day (Nov 2024) with 150+ school participants; Guest Speaker Sessions; Leadership activities.',
        'Events and Participation The department organized three major events during 2024-2025: (a) Innovation and Entrepreneurship Day (Nov 2024) with 150+ school participants and external stakeholders. (b) Guest Speaker Sessions integrated into various courses (e.g. MGMT 215, MGMT 102, etc.) (c) Leadership activities.',
        1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'The department to organize at least three major public-facing events, such as guest speaker sessions, roundtables, or workshops. At least one flagship event to be positioned as a recurring annual benchmark initiative.',
        'The department to organize at least three major public-facing events, such as guest speaker sessions, roundtables, or workshops. At least one flagship event (e.g. RIT Dubai Research Symposium or AI in Business Forum) to be positioned as a recurring annual benchmark initiative.',
        1, v_admin_id, v_ap_2526);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Sustainability Initiatives and Reporting', 10, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Launch at least two new sustainability initiatives per academic year involving 30% of faculty and 40% of students. Publish a departmental bi-annual sustainability report.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Currently in discussion on deciding on the strategy of Sustainability initiatives (expected results at the end of Fall 2024). Initiate discussions with the involvement of all stakeholders.',
        'Currently in discussion on deciding on the strategy of Sustainability initiatives (expected results at the end of Fall 2024 -- worth discussing the possibility of organizing a student-led conference in relation to sustainability initiatives in December 2024). Initiate discussions with the involvement of all stakeholders.',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'While formal departmental sustainability initiatives were limited in 2024-2025, several faculty incorporated sustainability content into courses (OB, Marketing, and Leadership) and class discussions around ethical business and ESG.',
        'Implementation and Adoption While formal departmental sustainability initiatives were limited in 2024-2025, several faculty incorporated sustainability content into courses (e.g. OB, Marketing, and Leadership) and class discussions around ethical business and ESG. Informal paper- and plastic-reduction practices observed.',
        1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'At least two new sustainability-focused initiatives to be launched during the academic year. At least 30% of faculty and 40% of students to participate in sustainability-related activities.',
        'At least two new sustainability-focused initiatives (e.g. plastic reduction, digital submission policies, energy savings) to be launched during the academic year. At least 30% of faculty and 40% of students to participate in sustainability-related activities or campaigns. A departmental sustainability report to be published.',
        1, v_admin_id, v_ap_2526);

    -- =========================================================================
    -- INSE
    -- =========================================================================
    INSERT INTO strategy (planning_cycle_id, department_id, strategy_type, state, title)
    VALUES (v_cycle_id, v_dept_inse, 'DEPARTMENT', 'DEPLOYED', 'INSE 2022-2027 Departmental Strategic Plan')
    RETURNING id INTO v_ds_inse;
    INSERT INTO role_assignment (user_id, strategy_id, role) VALUES (v_admin_id, v_ds_inse, 'OWNER');

    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_ds_inse, 'INSE Strategic Objectives 2022-2027', 1, v_admin_id) RETURNING id INTO v_dg;

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Collect, analyse, interpret, and disseminate institutional data for better decision-making; engage in benchmarking activities', 1, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Collect, analyse, interpret, and disseminate institutional data in order to facilitate better decision-making and improve overall effectiveness. Engage in benchmarking activities to compare the institution''s performance against peer institutions.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Conduct surveys amongst the internal stakeholders and analyze and distribute the results in each semester to the respective departments in a timely manner. Conducting institutional and program-related benchmarking with peer institutions.',
        'Conduct surveys, amongst the internal stakeholders and analyze and distribute the results in each semester to the respective departments in a timely manner. Conducting institutional and program-related benchmarking with peer institutions. Submission of data to regulatory bodies for University Classification.',
        1, v_admin_id, v_ap_2224);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Ensure compliance and alignment with accreditation requirements by establishing consistent processes', 2, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar2_3);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Ensure compliance and alignment with accreditation requirements in a timely manner by establishing processes that consistently meet or exceed standards set by the local and international accreditation bodies.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Submit self-studies, ERT responses for renewal of institutional and program accreditation in compliance with CAA and international standards as per the IE calendar.',
        'Submit self-studies, ERT responses for renewal of institutional and program accreditation in compliance with CAA and international standards as per the IE calendar.', 1, v_admin_id, v_ap_2224);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Automate repetitive processes such as the course folder review process for efficient monitoring', 3, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar3_5);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Automate repetitive processes such as the course folder review process for efficient monitoring and maintenance of the course folders to achieve compliance with the CAA standards and to ensure effective usage of available human resources.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Acquire and implement course folder management software and train the faculty and IE staff on its efficient and effective usage.',
        'Acquire and implement course folder management software and train the faculty and IE staff on its efficient and effective usage.', 1, v_admin_id, v_ap_2224);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Enhance the evaluation framework for ongoing assessment and improvement of academic programs and administrative services', 4, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar3_5);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Enhance the evaluation framework for ongoing assessment and improvement, focusing on academic programs, administrative services, and supporting units. Provide necessary support for developing and refining institutional plans, utilizing data insights to drive RIT Dubai towards its goals.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Implement the course folder management software with the right integration with SIS and mycourses. Improve the quality of annual self-evaluation reports and action plans.',
        'Implement the course folder management software with the right integration with SIS and mycourses and create a provision for receiving automated reports for program assessment and institutional effectiveness. Improve the quality of annual self-evaluation reports and action plans.',
        1, v_admin_id, v_ap_2224);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Prepare, submit, and obtain accreditation documents for new programs', 5, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar2_3);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Prepare, submit, and obtain accreditation documents for new programs - BFA in New Media Design and BS in Advertising and Public Relations as per the required standards and in a timely manner.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Support MOE visits, and submit responses for ERT reviews for new programs and achieve initial program accreditation within the set timeline.',
        'Support MOE visits, and submit responses for ERT reviews for new programs and achieve initial program accreditation within the set timeline.', 1, v_admin_id, v_ap_2224);

    -- =========================================================================
    -- COOP
    -- =========================================================================
    INSERT INTO strategy (planning_cycle_id, department_id, strategy_type, state, title)
    VALUES (v_cycle_id, v_dept_coop, 'DEPARTMENT', 'DEPLOYED', 'Co-op and Outreach 2022-2027 Departmental Strategic Plan')
    RETURNING id INTO v_ds_coop;
    INSERT INTO role_assignment (user_id, strategy_id, role) VALUES (v_admin_id, v_ds_coop, 'OWNER');

    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_ds_coop, 'Co-op and Outreach Strategic Objectives 2022-2027', 1, v_admin_id) RETURNING id INTO v_dg;

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Meet 10 Gov. entities to promote courses, internships/coop, projects, research, and other collaboration', 1, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_oce1_1);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Invite 5 Gov. entities and ensure 2 attend every career fair, attracted by offering internships for Emirati students', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Attended: EMIRATES, Digital Dubai, ENOC, Commercial Bank of Dubai. 2 sessions were conducted (DNS Al Barsha). 1 Open Day: COOP Program at RIT Dubai Counselors workshop - Panel Discussion.',
        'Invite 5 Gov. entities and ensure 2 attend every career fair attracted by offering internships for Emirati students Attended: EMIRATES, Digital Dubai, ENOC, Commercial Bank of Dubai. 2 sessions were conducted. 1 Open day Open Day: COOP Program at RIT Dubai Counselors workshop - Panel Discussion.',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, '10 Gov. entities attended our 3 career fairs in 24-25.',
        'Invite 5 Gov. entities and ensure 2 attend every career fair attracted by offering internships for Emirati students (and other nationalities). Report: 10 Gov. entities attended our 3 career fairs on 24-25.',
        1, v_admin_id, v_ap_2425);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, '"At-School" workshops for G12 students regarding experiential learning', 2, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Conduct 2 "At-School" workshops and 1 Open day per year', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, '2 "At-School" workshops. 2 sessions were conducted (DNS Al Barsha). 1 Open Day: COOP Program at RIT Dubai Counselors workshop.',
        '2 "At-School" workshops 2 sessions were conducted (DNS Al Barsha) 1 Open day Open Day: COOP Program at RIT Dubai Counselors workshop - Panel Discussion.', 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, '2 workshops conducted about experiential learning and Coop concept: 1 for school counselors in the innovation center, 1 for accepted students in B-004 for Fall 2251.',
        'Instead, 2 workshops were conducted about experiential learning and Coop concept. 1 for school counselors in the innovation center. 1 for the accepted students in B-004 for fall 2251.',
        1, v_admin_id, v_ap_2425);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Improve the Coop prep course syllabus to be more hands-on and rigorous', 3, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Add "What makes you, You" experience in relation to the 2025 required skills according to the World Economic Forum. Monitor the progress in coaching sessions.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'An Experience was added to the course, facilitators were trained to conduct it, mapped to the coaching session meeting, and conducted by all sections.',
        'An Experience was added to the course, facilitators were trained to conducted, mapped to the coaching session meeting, and conducted by all sections.', 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'The attendance policy was amended to reduce the number of absence days from 4 to 2. Both the CV and DT assignments were (90%) completed in class.',
        'The attendance policy was amended to reduce the number of absence days from 4 to 2. Both the CV and DT assignments were (90%) completed in class.',
        1, v_admin_id, v_ap_2425);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Invite all faculty to recommend "Research/Consultancy" based coop to share with students; consider professional certificates', 4, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '1 consultancy per year. 5 research per year.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'The consultancy Coop was made by FARNEK. 29 Research Coop placements achieved.',
        'The consultency Coop was made by FARNEK but our students will be in direct reporting to the employer supervisor. 29 Research Coop.', 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, '4 consultancy research-based Coops are ongoing with GDRFA. 55 research Coops were conducted in 24-25.',
        '4 consultancy research-based Coops are ongoing with GDRFA 55 research Coops were conducted in 24-25.', 1, v_admin_id, v_ap_2425);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Keep Alumni data completed and accurate for at least 75% of all Alumni', 5, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_oce3_5);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Grant access to SIS for accurate alumni data. Create LinkedIn group for Alumni.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Data is collected when invitations are sent for events (Alumni dinners, Job opportunities, etc.). Also collected through GDS and exit surveys and monitored through LinkedIn profiles.',
        'Keep Alumni data completed and accurate for at least 75% of all Alumni. Create LinkedIn group for Alumni. This is happening through the GDS and exit surveys and is monitored through LinkedIn profiles too. Data is also collected when invitations are sent for events (Alumni dinners, Job opportunities, etc.).',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'The names, majors, grades, and year of graduation are 100% accurate for all alumni. The Contact details of the alumni updated. Create Alumni Board and WhatsApp groups.',
        'The names, majors, grades, and year of graduation are 100% accurate for all alumni. Create Alumni Board and WhatsApp groups (admin control). Report: The names, majors, grades, and year of graduation are 100% accurate for all alumni.',
        1, v_admin_id, v_ap_2425);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, '10% annual increase in employers'' number and 10% annual increase in employers attending career fairs', 6, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_oce3_6);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Use the multi-purpose hall for the Spring career fair. Attend one exhibition/event every quarter (Big 5, GITEX, etc.)', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, '10-15% increase in employers attending career fairs. Accurate detailed data will be provided soon.',
        '10% annual increase in employers'' number 10% annual increase in employers attending career fairs 10-15% Hence, accurate detailed data will be provided soon!', 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Exceeded an average of 3 additional new employers every week in 24-25. Maintained daily employer visits, meetings, and/or fruitful correspondence.',
        'Exceeded an average of 3 additional new employers every week in 24-25. Maintained daily employer visits, meetings, and/or fruitful correspondence.', 1, v_admin_id, v_ap_2425);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'One representative from coop team for every open day conducted by Admissions', 7, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_oce4_7);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Coop Office to attend all open days', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Coop Office members attended major open days. There are too many open days, which is too much for the Coop team to cover. This KPI needs discussion to be more effective.',
        'Coop Office to attend all open days Coop Office members attended major open days. There are too many open days, which is too much for the Coop team to cover. This KPI needs discussion to be more effective.', 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Each coop team member attended one or more open days with admissions.',
        'Each coop team member should attend one open day with admissions. Report: Each coop team member attended one or more open days with admissions.', 1, v_admin_id, v_ap_2425);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Target 2 event sponsors (Gov. or Corp.)', 8, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_oce5_8);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Approach Dubai Police to sponsor some students'' events or ensure free invitations for events attendance', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Most of the outcome of this effort was training programs and sponsored students for MS degree.',
        'Approach Dubai police to sponsor some students'' events or ensure free invitations for events attendance Most of the outcome of this effort was training programs and sponsored students for MS degree.', 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Approach Dubai Police and GDRFA to sponsor some students'' events or ensure free invitations for events attendance.',
        'Approach Dubai police and GDRFA to sponsor some students'' events or ensure free invitations for events attendance.', 1, v_admin_id, v_ap_2425);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Define and offer relevant Prof. courses annually', 9, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_oce6_9);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Define 6 relevant Prof. courses and offer 2 annually. Offer special rate certification courses for students from different majors.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Define 6 relevant Prof. courses and offer 2 annually. Offer special rate certification courses for students from different majors.',
        'Define 6 relevant Prof. courses and offer 2 annually Offer special rate certification courses for student from different majors.', 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Define 6 relevant Prof. courses and offer 4 annually.',
        'Define 6 relevant Prof. courses and offer 4 annually Offer special rate certification courses for student from different majors.', 1, v_admin_id, v_ap_2425);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Arrange workshops/activities for employers during career fairs or in coordination with other departments', 10, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, '1 workshop every semester', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, '1 workshop every semester', '1 workshop every semester', 1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, '1 workshop every semester', '1 workshop every semester', 1, v_admin_id, v_ap_2425);

    -- =========================================================================
    -- MAS (Math and Sciences)
    -- =========================================================================
    INSERT INTO strategy (planning_cycle_id, department_id, strategy_type, state, title)
    VALUES (v_cycle_id, v_dept_mas, 'DEPARTMENT', 'DEPLOYED', 'Math and Sciences 2022-2027 Departmental Strategic Plan')
    RETURNING id INTO v_ds_mas;
    INSERT INTO role_assignment (user_id, strategy_id, role) VALUES (v_admin_id, v_ds_mas, 'OWNER');

    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_ds_mas, 'MAS Strategic Objectives 2022-2027', 1, v_admin_id) RETURNING id INTO v_dg;

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Direct collaboration with other departments for research based projects; include advisory board to support research ideas connected to industry', 1, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar2_4);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'A direct collaboration with other departments in terms of research based projects (capstone or Master thesis) to encourage the MaS faculty more into the research. To include an advisory board to support research ideas more connected to industry and outer market.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Alzheimer''s research with the Computing & EE department - Ongoing. One publication published in July 2024. Forming a Pathology lab along with the Computing & EE department - Ongoing.',
        'Alzheimer''s research with the Computing & EE department - Ongoing - One publication published in July 2024 - Forming a Pathology lab along with the Computing & EE department - Ongoing - Funding is being sourced - Communications are being made in order to form an advisory board for MaS department.',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Alzheimer''s research with the Computing & EE department - Completed, publication published in July 2024. Alzheimer''s research with GMU - Ongoing, publication being finalized for submission in June 2025.',
        'Alzheimer''s research with the Computing & EE department - Completed - publication published in July 2024. Alzheimer''s research with GMU - Ongoing - publication being finalized for submission in June 2025. Forming a Pathology lab along with the Computing & EE department - Paused - Funding NA.',
        1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Dr Vidya''s students were selected in the top 10 participants for Agricultural Hackathon organized by the Ministry of Climate Change. Dr. Rema and her team have initiated multiple research projects.',
        'Dr Vidya''s students Mohamed Nabil Ansari, Blen Nima & Merlin Igwe were selected in the top10 participants for Agricultural Hackathon organized by the Ministry of Climate Change and Environment. Dr. Rema, Shamat, Dali, Vidhya & Ms. Wardah, Ms. Rawan have initiated multiple research projects.',
        1, v_admin_id, v_ap_2526);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Direct or indirect collaboration with external institutions including healthcare sectors, RIT global and other universities', 2, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar2_4);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Direct or indirect collaboration with external institutions including healthcare sectors, RIT global and other universities. Promote more hands on sessions on the area of Math. Promote recent developments in the area of teaching.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'A new collaboration to be discussed with RIT NY and AUD related to a research based activity in the area of Physics. A fund of 15000 AED was granted from RIT research committee towards this project. Breast Cancer Seminar was held by a medical doctor from DSO.',
        'A new collaboration to be discussed with RIT NY and AUD related to a research based activity in the area of Physics. A fund of 15000 AED was granted from RIT research committee towards this project. To be conducted through 2024-25. Breast Cancer Seminar was held by a medical doctor from DSO Polyclinic.',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Joint Research Project with Gulf Medical University on Alzheimer''s Research. Joint Research Project with Gulf Medical University on Cancer Research. Joint Research Project with the Computing & EE department at RIT Dubai.',
        'Joint Research Project with Gulf Medical University on Alzheimer''s Research. Joint Research Project with Gulf Medical University on Cancer Research. Joint Research Project with the Computing & EE department at RIT Dubai, and College of Science at RIT Global. High school visits planned with Admissions.',
        1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Plans to collaborate with experts and invite guest speakers from different fields of biology and chemistry through webinars and workshops. Dr. Sathya Dev delivered an Online Talk on "Engineering for the Heart".',
        'We plan to collaborate with experts and invite guest speakers from different fields of biology and chemistry through webinars and workshops to enrich students'' learning experiences. Dr. Sathya Dev delivered an Online Talk on "Engineering for the Heart" for the students.',
        1, v_admin_id, v_ap_2526);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Collaboration with FDC through department representative; conduct at least 1 development activity per AY', 3, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Collaboration with FDC through department representative or those organized by RIT-D directly, i.e. to conduct at least 1 development activity per AY. Guidance and suggestions from advisory board once formed.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'A workshop/talk on how to access RIT global library resources for research was presented through FDC invitee (Spring 24). A seminar on the effect of AI in education (organized by RIT-D) - Feb 24. Course Folder training seminar offered to all newly hired faculty.',
        'A workshop/talk to be presented through FDC invitee on how to access RIT global library resources required for research based activities (Spring 24) - Done. A seminar on the effect of AI in education (organized by RIT-D) - Feb 24 - Done. Offered a Course Folder training seminar to all newly hired faculty.',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'In August, FDC organized an in-person workshop on enhancing teamwork and relationships among faculty. In September, FDC organized two training workshops on the usage of MyCourses.',
        'In August, FDC organized an in-person workshop on enhancing teamwork and relationships among faculty by inviting an external speaker. In September, FDC organized two training workshops on the usage of MyCourses by inviting a speaker from RIT NY (over Zoom). Faculty participation in FD events recorded.',
        1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Dr. Ali to hold meeting with the chair of FD committee to plan for workshops on the integration of AI in teaching and learning, with the main focus on the MATH/Sciences areas.',
        'Dr. Ali to hold meeting with the chair of FD committee in order to plan for some possible workshops on the integration of AI in teaching and learning, with the main focus on the MATH/Sciences areas. MaSc faculty to attend FDC workshops as planned.',
        1, v_admin_id, v_ap_2526);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Organizing competitions among schools; forming a student MATH club; holding internal competitions', 4, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Organizing and holding competitions among schools. Collaboration with RIT-D students union and ASC. Forming a student MATH club (could be extended to other subjects). Holding internal competition around different subjects taught in MaS.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'To hold Physics lab sessions for high school students - Ongoing. Chemistry Competition on 19th of Feb - Done. Biology Competition on 26th of Feb - Done. Meeting held with students governor on forming a MATH club led by faculty.',
        'To hold Physics (Science) lab sessions for high school students - Ongoing - Chemistry Competition scheduled on the 19th of Feb. - Done - Biology Competition scheduled on the 26th of Feb. - Done - A meeting has been held with students governor in order to take action on forming a MATH club led by faculty.',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Science Fair held in Oct-24. Biology Competition held in Feb-25. Chemistry Competition held in Feb-25. Organization of the seventh annual Math competition during Fall 2024.',
        'Science Fair held in Oct-24. Biology Competition held in Feb-25. Chemistry Competition held in Feb-25. Organization of the seventh annual Math competition during Fall 2024. Done - Invite High school students to for a physics lab demonstrations during Spring 2025.',
        1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Dr Vidya''s students were selected in the top 10 participants for Agricultural Hackathon organized by the Ministry of Climate Change. Ms. Wardah and Dr. Dali were selected as mentors for Mustadeem 2.',
        'Dr Vidya''s students Mohamed Nabil Ansari, Blen Nima & Merlin Igwe were selected in the top10 participants for Agricultural Hackathon organized by the Ministry of Climate Change and Environment. Ms. Wardah and Dr. Dali were selected as mentors for Mustadeem 2 organized by the Ministry of Climate Change.',
        1, v_admin_id, v_ap_2526);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Collaboration and communication through RIT-D representative in GLEC to promote achievements; develop MaS LinkedIn page', 5, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'A collaboration and communication is required through RIT-D representative in GLEC in order to promote achievements made throughout the year. Engage faculty in presenting their research activities during department meetings. Develop MaS LinkedIn page.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Science updates have always been an integral part of department meetings. Science updates have always been posted on RIT-Dubai social media. Activities and achievements continuously shared during department meetings.',
        'Science updates have always been an integral part of department meetings, shared by Dr. Rema Amawi and her team. Science updates have always been posted on RIT-Dubai social media, and sharing on MaS page, will be next year onwards. Activities and achievements continuously shared during department meetings.',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Science updates have always been an integral part of department meetings. Science updates posted on RIT-Dubai social media and sharing on MaS page this year. New chemistry content is being developed.',
        'Science updates have always been an integral part of department meetings, shared by Dr. Rema Amawi and her team. Science updates have always been posted on RIT-Dubai social media, and sharing on MaS page this year. New chemistry content is being developed to be posted on RIT-Dubai website.',
        1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Math club activities updates have always been posted on the Department''s LinkedIn page and the club''s page. yMath Club to organize Statistics seminar.',
        'yMath club activities updates have always been posted on the Department''s LinkedIn page and the club''s page. yMath Club to organize Statistics seminar to help students learn the application of Statistics in real life. Science faculty will be sharing the progress of their research activities.',
        1, v_admin_id, v_ap_2526);

    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Enhancement of course learning outcomes to comply better with the needs of other departments in cooperation with NY campus', 6, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_do, v_o_ar3_6);
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Enhancement of course learning outcomes in order to comply better with the needs of other departments in cooperation with NY campus. Hold frequent meetings with the chair of other departments to enhance the courses offered in MaS per AY.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'All MaS courses syllabi are being revised in terms of CLOs. All Bio., Chem. and Physics courses are finalized and approved by NY. Three Math courses are finalized and being approved by NY. 7 other MATH/STAT courses are under NY review.',
        'All MaS courses syllabi is being revised in terms of CLOs, in order to be enhanced as per required. NY is in the loop - Ongoing - All Bio. Chem. and Physics courses are finalized and approved by NY. Three Math courses are finalized and being approved by NY. 7 other MATH/STAT courses are under NY review.',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Prerequisites for all 100 level Math courses have been revised and shared with the main campus for approval. Some more courses have been approved by NY campus. Process is rather slow from NY.',
        'Prerequisites for all 100 level Math courses have been revised and the new enhanced drafts have been shared with the main campus for approval. Some more courses have been approved by NY campus. The process is rather slow from NY. Some more courses to be approved. Ongoing.',
        1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Incorporating some AI based math tools in Calculus courses (ex: Desmos, Symbollab, Integral Calculator). Planning to make a presentation to help students in preparing for major exams.',
        'Incorporating some AI based math tools in Calculus courses (ex: Desmos, Symbollab, Integral Calculator). Inviting the colleagues to participate in FDC activities. Planning to make a presentation to help students in preparing for major exams. Dr Abhilasha planning to implement AI tools like desmos/symbollab.',
        1, v_admin_id, v_ap_2526);

    RAISE NOTICE 'V6 seed completed successfully.';

END $$;
