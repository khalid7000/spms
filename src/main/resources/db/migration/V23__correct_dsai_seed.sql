-- V23: Re-seed DSAI (strategy 3) with correct structure.
--
-- Before: 34 objectives, 34 initiatives, 29 achievements (collapsed 1:1 structure).
-- After:  7 goals (one per Col-A row), 7 objectives (same title as goal),
--         34 initiatives distributed across objectives, 1 measurement per initiative,
--         achievements only for period 4 (2022-2024) where Col C was non-blank.
--         objective_mapping and initiative_mapping to university strategy.
--
-- Source: DSAI Dept Vision Actions.xlsx

DO $$
DECLARE
    v_strategy_id BIGINT := 3;
    v_goal_id     BIGINT;
    v_obj_id      BIGINT;
    v_init_id     BIGINT;
    v_meas_id     BIGINT;
BEGIN
    DELETE FROM goal WHERE strategy_id = v_strategy_id;

    -- ===== GOAL 1: Maintain Academic Excellence in all Programs (9 initiatives) =====
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Maintain Academic Excellence in all Programs', 1, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Maintain Academic Excellence in all Programs', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_obj_id, 7);

    -- I1
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Keep curriculum current to incorporate cutting-edge skills', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Keep curriculum current to incorporate cutting-edge skills', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 13);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Regular checks with faculty during retreats about new trends and topics. Incorporation of agreed topics in courses. Announcement of updates on the website catalog, in the news, and with stakeholders.', 1, 1, 4, NOW(), NOW());

    -- I2
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Ensure competitiveness of programs', 2, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Ensure competitiveness of programs', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 13);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Yearly benchmark with competing programs from other universities.', 1, 1, 4, NOW(), NOW());

    -- I3
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Maintain accreditation status', 3, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Maintain accreditation status', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 8);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Follow the official calendars. Minimize requirements.', 1, 1, 4, NOW(), NOW());

    -- I4
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Explore innovative teaching methods to keep up with educational trends', 4, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Explore innovative teaching methods to keep up with educational trends', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 12);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'IE to communicate a yearly executive summary on new trends and organize training (self-paced online sessions) for faculty.', 1, 1, 4, NOW(), NOW());

    -- I5
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Applying innovative teaching methods or tools', 5, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Applying innovative teaching methods or tools', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 12);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Faculty to report during retreats on the experience in applying those methods and their impact on the learning process. Surveys to be run with students on the impact of the new methods or tools.', 1, 1, 4, NOW(), NOW());

    -- I6
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Maintain high thesis completion rates', 6, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Maintain high thesis completion rates', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 7);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, '80% completed on time.', 1, 1, 4, NOW(), NOW());

    -- I7 (achievement trimmed to stay under 500 chars)
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Maintain high quality of theses', 7, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Maintain high quality of theses', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 7);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Provide benchmark during thesis defense: target 10% Very Good, 30% Good. Publish quality statistics at the end of each program. Report high-quality theses for further capitalization through alumni outreach, testimonials, and presentations to customers.', 1, 1, 4, NOW(), NOW());

    -- I8
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Ensure students'' proficiency in "writing" and "presentation"', 8, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Ensure students'' proficiency in "writing" and "presentation"', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 4);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Assess students'' writing and presentation skills before joining the program. Offer students preparatory courses on Writing Skills and Presentation Skills or Executive Education before graduation. Faculty to report during the first courses about students who need to join these trainings before graduation.', 1, 1, 4, NOW(), NOW());

    -- I9
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Maintain integrity standards by designing assessments that are more resistant to cheating, such as projects, presentations, and case studies that require critical thinking and application of knowledge.', 9, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Maintain integrity standards by designing assessments that are more resistant to cheating, such as projects, presentations, and case studies that require critical thinking and application of knowledge.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 12);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'At least one oral exercise per course (ex: oral defense of major projects).', 1, 1, 4, NOW(), NOW());

    -- ===== GOAL 2: Increase Student Enrollment in SC and FFP (4 initiatives) =====
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Increase Student Enrollment in SC and FFP', 2, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Increase Student Enrollment in SC and FFP', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_obj_id, 16);

    -- I10
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Hold symposia or Innovation Days in Spring 2024 and beyond to publicize programs', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Hold symposia or Innovation Days in Spring 2024 and beyond to publicize programs', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 26);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Measure the number of respondents to the program.', 1, 1, 4, NOW(), NOW());

    -- I11
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Create separate web pages for GPR dept programs', 2, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Create separate web pages for GPR dept programs', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 16);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'End of Summer 2024. Review and update every Summer.', 1, 1, 4, NOW(), NOW());

    -- I12
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Use Alumni as marketing tool', 3, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Use Alumni as marketing tool', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 24);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Identify at least 4 alumni per cohort from both genders.', 1, 1, 4, NOW(), NOW());

    -- I13
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Create a database of bright and impactful students to form the core alumni. Faculty to report bright students at the end of each course.', 4, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Create a database of bright and impactful students to form the core alumni. Faculty to report bright students at the end of each course.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 24);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Faculty to report bright students at the end of each course. Outreach to follow-up with the students on their career progress. Collect testimonials and add them to the news.', 1, 1, 4, NOW(), NOW());

    -- ===== GOAL 3: Optimize Operations (5 initiatives) =====
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Optimize Operations', 3, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Optimize Operations', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_obj_id, 6);

    -- I14
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Seek support from Advisory Board on student recruitment.', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Seek support from Advisory Board on student recruitment.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 6);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Organize with Advisory Board members visits to prospect organizations, consulates, and NGOs to promote our programs. At least one visit per Board Member during the SPRING and FALL semesters.', 1, 1, 4, NOW(), NOW());

    -- I15 (no achievement — blank in Excel)
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Streamline scheduling for all 3 programs aligning them with RO', 2, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Streamline scheduling for all 3 programs aligning them with RO', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 11);

    -- I16 (no achievement — blank in Excel)
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Empower program coordinators', 3, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Empower program coordinators', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 11);

    -- I17 (no achievement — blank in Excel)
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Identify and address gaps in existing policies and procedures', 4, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Identify and address gaps in existing policies and procedures', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 11);

    -- I18
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Through automation, improve course logistics'' efficiency; course creation on SIS and mycourses, students'' email creation, proper scheduling of SRATE timings.', 5, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Through automation, improve course logistics'' efficiency; course creation on SIS and mycourses, students'' email creation, proper scheduling of SRATE timings.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 11);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'SIS, mycourses, and students'' emails must be ready before course start.', 1, 1, 4, NOW(), NOW());

    -- ===== GOAL 4: Establish Excellence in Research (5 initiatives) =====
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Establish Excellence in Research', 4, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Establish Excellence in Research', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_obj_id, 5);

    -- I19 (no achievement — blank in Excel)
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Full-time faculty to publish at least one paper per year', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Full-time faculty to publish at least one paper per year', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 7);

    -- I20
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Encourage faculty-student cooperation in converting theses to joint publication', 2, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Encourage faculty-student cooperation in converting theses to joint publication', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 7);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'At least 1 thesis per cohort converted to a publication.', 1, 1, 4, NOW(), NOW());

    -- I21
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Maintain high quality in theses and encourage student publication following thesis', 3, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Maintain high quality in theses and encourage student publication following thesis', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 7);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, '1-2 publications per cohort.', 1, 1, 4, NOW(), NOW());

    -- I22 (no achievement — blank in Excel)
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Faculty to attend at least one conference per year to present research and/or publicize programs', 4, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Faculty to attend at least one conference per year to present research and/or publicize programs', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 7);

    -- I23
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Faculty to provide a list of research topics for theses in the strategic areas of RIT', 5, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Faculty to provide a list of research topics for theses in the strategic areas of RIT', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 9);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Topics to be published before 2nd semester.', 1, 1, 4, NOW(), NOW());

    -- ===== GOAL 5: Build and Grow Relationships with Key Government, Industry and NGO Sectors (2 initiatives) =====
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Build and Grow Relationships with Key Government, Industry and NGO Sectors', 5, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Build and Grow Relationships with Key Government, Industry and NGO Sectors', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_obj_id, 13);

    -- I24
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Build relationships with govt departments, industry, and NGOs, and involve them via Advisory Board seats, curriculum involvement, executive education', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Build relationships with govt departments, industry, and NGOs, and involve them via Advisory Board seats, curriculum involvement, executive education', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 20);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Revise advisory board on a yearly basis. Try to attract advisors from government, industry, and NGO sectors.', 1, 1, 4, NOW(), NOW());

    -- I25
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Collect challenges from the advisory board members to be converted to thesis topics. Ask contacts to provide possible datasets and/or mentors around those challenges.', 2, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Collect challenges from the advisory board members to be converted to thesis topics. Ask contacts to provide possible datasets and/or mentors around those challenges.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 3);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'List of challenges from every board member.', 1, 1, 4, NOW(), NOW());

    -- ===== GOAL 6: Grow Community Engagement (5 initiatives) =====
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Grow Community Engagement', 6, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Grow Community Engagement', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_obj_id, 11);

    -- I26
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Establish community outreach programs that involve students and faculty members providing data analytics services to local businesses, nonprofits, or government agencies', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Establish community outreach programs that involve students and faculty members providing data analytics services to local businesses, nonprofits, or government agencies', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 26);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Track the number of outreach programs initiated and completed. Assess the impact of DA, SC and FFP services on the community partners. Collect feedback from community partners on the value of the program.', 1, 1, 4, NOW(), NOW());

    -- I27
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Integrate projects focused on social impact, such as solving community challenges or addressing societal issues, into the curriculum.', 2, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Integrate projects focused on social impact, such as solving community challenges or addressing societal issues, into the curriculum.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 10);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Evaluate the success of projects in achieving social impact goals. Measure the engagement and satisfaction of students involved in these projects. Collect testimonials or case studies showcasing the positive outcomes.', 1, 1, 4, NOW(), NOW());

    -- I28
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Collaborate with local educational institutions to offer workshops, seminars, or mentorship programs aimed at promoting data analytics in schools and colleges.', 3, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Collaborate with local educational institutions to offer workshops, seminars, or mentorship programs aimed at promoting data analytics in schools and colleges.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 17);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Track the number of collaborative initiatives with local schools. Assess the impact of workshops and programs on student awareness and interest in data analytics and smart cities topics. Collect feedback from educators and students involved.', 1, 1, 4, NOW(), NOW());

    -- I29
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Engage students and faculty in research projects that address specific challenges faced by the local community, demonstrating the practical applications of data analytics.', 4, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Engage students and faculty in research projects that address specific challenges faced by the local community, demonstrating the practical applications of data analytics.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 10);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Evaluate the relevance and impact of research projects on community issues. Measure the extent to which research findings are adopted or implemented by the community. Assess student and faculty involvement in community-focused research.', 1, 1, 4, NOW(), NOW());

    -- I30
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Seek recognition and awards from local organizations or authorities for the program''s positive impact on the community.', 5, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Seek recognition and awards from local organizations or authorities for the program''s positive impact on the community.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 26);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Monitor awards and recognitions received by the program. Assess the visibility and credibility of the program in the community. Track community sentiment and support following awards.', 1, 1, 4, NOW(), NOW());

    -- ===== GOAL 7: Promote Innovation and Entrepreneurship (4 initiatives) =====
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Promote Innovation and Entrepreneurship', 7, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Promote Innovation and Entrepreneurship', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_obj_id, 8);

    -- I31
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Invite students to innovation and entrepreneurship trainings during the year.', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Invite students to innovation and entrepreneurship trainings during the year.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 19);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Develop statistical reports on number of students joining these activities. Collect surveys about students'' feedback on the value of these events.', 1, 1, 4, NOW(), NOW());

    -- I32
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Organize hackathons, data competitions, and coding challenges to stimulate problem-solving skills within the RIT strategic research centers and foster a competitive yet collaborative environment.', 2, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Organize hackathons, data competitions, and coding challenges to stimulate problem-solving skills within the RIT strategic research centers and foster a competitive yet collaborative environment.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 19);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, '1 challenge per semester in a selected theme related to one of the RIT strategic research centers from DA or SC perspectives. Report on number of contributions per semester.', 1, 1, 4, NOW(), NOW());

    -- I33
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Invite entrepreneurs and innovators to deliver guest lectures and share insights into emerging trends and technologies in data analytics.', 3, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Invite entrepreneurs and innovators to deliver guest lectures and share insights into emerging trends and technologies in data analytics.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 26);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Try to get one guest lecture per delivered course.', 1, 1, 4, NOW(), NOW());

    -- I34
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Establish mentorship programs connecting students with industry professionals to provide guidance and networking opportunities.', 4, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Establish mentorship programs connecting students with industry professionals to provide guidance and networking opportunities.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 22);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Start developing a mentors'' database through our network. Evaluate the mentors'' contribution to students'' theses.', 1, 1, 4, NOW(), NOW());

    RAISE NOTICE 'V23: DSAI strategy 3 re-seeded with 7 goals, 7 objectives, 34 initiatives, 30 achievements.';
END $$;
