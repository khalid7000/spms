-- V26: Re-seed COOP (Co-op and Outreach, strategy 9) with correct structure.
--
-- Department: COOP (Co-op and Outreach), strategy_id=9, department_id=7
-- Assessment periods: 4=2022-2024, 5=2024-2025, 6=2025-2026 (period 6 blank)
-- Structure: 10 goals, each goal has 1 objective (same title), 1+ initiatives,
--            1 measurement per initiative, achievements for periods 4 and 5.
--
-- Source: COOP Dept Vision Actions.xlsx

DO $$
DECLARE
    v_strategy_id BIGINT := 9;
    v_goal_id     BIGINT;
    v_obj_id      BIGINT;
    v_init_id     BIGINT;
    v_meas_id     BIGINT;
BEGIN
    DELETE FROM goal WHERE strategy_id = v_strategy_id;

    -- ===== GOAL 1: Meet Gov. entities to build relationship =====
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Meet Gov. entities to build relationship', 1, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Meet Gov. entities to build relationship', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_obj_id, 10);

    -- I1
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Meet 10 Gov. entities to promote courses, internships/coop, projects, research, and other collaboration', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Meet 10 Gov. entities to promote courses, internships/coop, projects, research, and other collaboration', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 27);
    -- Period 4
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Invite 5 Gov. entities and ensure 2 attend every career fair attracted by offering internships for Emirati students. Attended: EMIRATES, Digital Dubai, ENOC, Commercial Bank of Dubai.', 1, 1, 4, NOW(), NOW());
    -- Period 5
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, '10 Gov. entities attended our 3 career fairs in 2024-2025.', 1, 1, 5, NOW(), NOW());

    -- ===== GOAL 2: Each department holds at least one k-12 event per year in the Innovation Center =====
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Each department holds at least one k-12 event per year in the Innovation Center', 2, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Each department holds at least one k-12 event per year in the Innovation Center', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_obj_id, 11);

    -- I1
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Conduct At-School workshops for G12 students regarding experiential learning, or any professional workshop.', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Conduct At-School workshops for G12 students regarding experiential learning, or any professional workshop.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 17);
    -- Period 4
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, '2 At-School workshops conducted (DNS Al Barsha). 1 Open Day for COOP Program at RIT Dubai. Counselors workshop with Panel Discussion.', 1, 1, 4, NOW(), NOW());
    -- Period 5
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, '2 workshops conducted about experiential learning and Coop concept: 1 for school counselors in the innovation center, 1 for accepted students.', 1, 1, 5, NOW(), NOW());

    -- ===== GOAL 3: Raise interns quality through RIT 365 and extra coaching =====
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Raise interns quality through RIT 365 and extra coaching', 3, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Raise interns quality through RIT 365 and extra coaching', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_obj_id, 12);

    -- I1
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Improve the Coop prep course syllabus to be more hands-on and rigorous.', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Improve the Coop prep course syllabus to be more hands-on and rigorous.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 22);
    -- Period 4
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Added the What makes you, You experience in relation to the 2025 required skills. An experience was added to the course, facilitators were trained, and coaching sessions were conducted by all facilitators.', 1, 1, 4, NOW(), NOW());
    -- Period 5
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Attendance policy amended to reduce absence days from 4 to 2. Both the CV and DT assignments were 90% completed in class.', 1, 1, 5, NOW(), NOW());

    -- ===== GOAL 4: Create alternative applied co-op experiences like research/consultancy projects =====
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Create alternative applied co-op experiences like research/consultancy projects', 4, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Create alternative applied co-op experiences like research/consultancy projects', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_obj_id, 12);

    -- I1
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Invite all faculty members at the start of the coop period to recommend Research/Consultancy based coop to share with students. Consider professional certificates.', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Invite all faculty members at the start of the coop period to recommend Research/Consultancy based coop to share with students. Consider professional certificates.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 23);
    -- Period 4
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, '29 Research Coops conducted. Consultancy Coop was made by FARNEK with direct reporting to employer supervisor.', 1, 1, 4, NOW(), NOW());
    -- Period 5
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, '4 consultancy research-based Coops ongoing with GDRFA. 55 research Coops conducted in 2024-2025.', 1, 1, 5, NOW(), NOW());

    -- ===== GOAL 5: Establish Alumni Clubs with reliable data and relationship =====
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Establish Alumni Clubs with reliable data and relationship', 5, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Establish Alumni Clubs with reliable data and relationship', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_obj_id, 14);

    -- I1
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Collect data from SIS, IE, MoE for accurate alumni data.', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Collect data from SIS, IE, MoE for accurate alumni data.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 24);
    -- Period 4
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Alumni data collected through GDS and exit surveys, monitored through LinkedIn profiles. Data collected when invitations sent for events.', 1, 1, 4, NOW(), NOW());
    -- Period 5
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Names, majors, grades, and year of graduation are 100% accurate for all alumni. Contact details up to 3 years are 75% accurate based on GDS.', 1, 1, 5, NOW(), NOW());

    -- I2
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Create Alumni Board and communication channels for ongoing engagement.', 2, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Create Alumni Board and communication channels for ongoing engagement.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 25);
    -- Period 5 only
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Alumni Board formed and first meeting conducted successfully.', 1, 1, 5, NOW(), NOW());

    -- ===== GOAL 6: Achieve an annual increase in employers number and employers attending career fairs =====
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Achieve an annual increase in employers number and employers attending career fairs', 6, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Achieve an annual increase in employers number and employers attending career fairs', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_obj_id, 15);

    -- I1
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Use the multi-purpose hall for the Spring career fair.', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Use the multi-purpose hall for the Spring career fair.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 26);
    -- Period 4
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, '10-15% increase in employers. 10 new strategic partners added in 2023-24: Odoo, Mastercard, RED Engineering, HNW Consultancy, FAB, RAK Bank, Philip Morris International, Cozmo Travels, FARNEK, and AMMANA. 67 new collaborations, 40 new employers on Symplicity.', 1, 1, 4, NOW(), NOW());
    -- Period 5
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Exceeded an average of 3 additional new employers every week in 2024-2025. Exceeded 10% annual increase in employers attending career fairs.', 1, 1, 5, NOW(), NOW());

    -- I2
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Organize one big career fair in a hotel and 5 to 10 small career fairs annually.', 2, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Organize one big career fair in a hotel and 5 to 10 small career fairs annually.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 26);

    -- I3
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Attend 6 exhibitions/events annually (e.g., Big 5, GITEX).', 3, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Attend 6 exhibitions/events annually (e.g., Big 5, GITEX).', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 26);
    -- Period 5
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Maintained daily employer visits, meetings, and fruitful correspondence throughout 2024-2025.', 1, 1, 5, NOW(), NOW());

    -- ===== GOAL 7: Active participation in admission initiatives including Open House, High School visits and workshops =====
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Active participation in admission initiatives including Open House, High School visits and workshops', 7, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Active participation in admission initiatives including Open House, High School visits and workshops', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_obj_id, 16);

    -- I1
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'One representative from coop team for every open day conducted by Admissions.', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'One representative from coop team for every open day conducted by Admissions.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 16);
    -- Period 4
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Coop Office members attended major open days. Note: there are too many open days, which is too much for the Coop team to cover.', 1, 1, 4, NOW(), NOW());
    -- Period 5
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Each coop team member attended one or more open days with admissions.', 1, 1, 5, NOW(), NOW());

    -- ===== GOAL 8: Target event sponsorships =====
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Target event sponsorships', 8, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Target event sponsorships', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_obj_id, 11);

    -- I1
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Target 2 event sponsors (Gov. or Corp.)', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Target 2 event sponsors (Gov. or Corp.)', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 28);
    -- Period 4
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Approached Dubai police to sponsor student events or ensure free invitations. Most outcomes were training programs and sponsored students for MS degree.', 1, 1, 4, NOW(), NOW());
    -- Period 5
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Approached Dubai police and GDRFA to sponsor student events or ensure free invitations for events.', 1, 1, 5, NOW(), NOW());

    -- ===== GOAL 9: Offer Professional courses =====
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Offer Professional courses', 9, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Offer Professional courses', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_obj_id, 18);

    -- I1
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Define and offer relevant professional courses annually.', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Define and offer relevant professional courses annually.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 18);
    -- Period 4
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Defined 6 relevant professional courses. Offer 2 annually. Offered special rate certification courses for students from different majors.', 1, 1, 4, NOW(), NOW());
    -- Period 5
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Defined 6 relevant professional courses. Offering 4 annually. Offered special rate certification courses for students from different majors.', 1, 1, 5, NOW(), NOW());

    -- ===== GOAL 10: Increase awareness level by 25% in the coming 5 years =====
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Increase awareness level by 25% in the coming 5 years', 10, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Increase awareness level by 25% in the coming 5 years', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_obj_id, 19);

    -- I1
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Arrange workshops/activities for employers during career fairs or in coordination with other departments.', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Arrange workshops/activities for employers during career fairs or in coordination with other departments.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 30);
    -- Period 4
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, '1 workshop every semester.', 1, 1, 4, NOW(), NOW());
    -- Period 5
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, '1 workshop every semester. Maintained consistent employer engagement sessions throughout the academic year.', 1, 1, 5, NOW(), NOW());

    RAISE NOTICE 'V26: COOP strategy 9 re-seeded.';
END $$;
