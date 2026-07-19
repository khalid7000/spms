-- V25: Re-seed ADV (Academic Advising, strategy 5) with correct structure.
--
-- Before: stale/incorrect seed data for strategy 5 (ADV, department 12).
-- After:  4 goals, 4 objectives (one per goal), 18 initiatives distributed
--         across objectives, 1 measurement per initiative, achievements for
--         periods 4 (2022-2024) and 5 (2024-2025) — completed items only.
--         objective_mapping and initiative_mapping to university strategy.
--
-- Source: ADV Academic Advising Excel (sheets: 2022-2024, 2024-2025, 2025-2026)
-- Period 6 (2025-2026) Col C is blank; no achievements for period 6.
-- Assessment periods: 4=2022-2024, 5=2024-2025, 6=2025-2026

DO $$
DECLARE
    v_strategy_id BIGINT := 5;
    v_goal_id     BIGINT;
    v_obj_id      BIGINT;
    v_init_id     BIGINT;
    v_meas_id     BIGINT;
BEGIN
    DELETE FROM goal WHERE strategy_id = v_strategy_id;

    -- ===== GOAL 1: Ensure students understand their requirements and progress towards graduation (2 initiatives) =====
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Ensure students understand their requirements and progress towards graduation', 1, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Ensure students understand their requirements and progress towards graduation', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_obj_id, 3);

    -- G1 I1: Offer greater support for graduate students to understand their program requirements
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Offer greater support for graduate students to understand their program requirements', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Offer greater support for graduate students to understand their program requirements', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 5);
    -- Period 4 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Information sessions held at least once a year for each graduate program', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Advisors to visit the first class of each graduate program cohort to introduce themselves', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Clarify thesis/capstone process and make guidelines available', 1, 1, 4, NOW(), NOW());
    -- Period 5 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Information sessions held for graduate programs on graduation requirements, thesis process, academic policies', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Worked with faculty and students to clean up confusion around Thesis enrollment and grading', 1, 1, 5, NOW(), NOW());

    -- G1 I2: Improve access to program information for students
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Improve access to program information for students', 2, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Improve access to program information for students', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 4);
    -- Period 5 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Held 1st Minors and Immersion Fair to introduce options to all undergraduate students', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Created BFA NMD flowchart, worksheet, and all orientation material for students', 1, 1, 5, NOW(), NOW());

    -- ===== GOAL 2: Support at-risk students with guidance and resources to lead to academic success (4 initiatives) =====
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Support at-risk students with guidance and resources to lead to academic success', 2, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Support at-risk students with guidance and resources to lead to academic success', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_obj_id, 3);

    -- G2 I1: Develop a program of support for students on Probation and Deferred Suspension
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Develop a program of support for students on Probation and Deferred Suspension', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Develop a program of support for students on Probation and Deferred Suspension', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 5);
    -- Period 4 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Coordinate with ASC to offer a Probation Workshop each semester', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Coordinate with ASC to offer a Study Skills course for students with Deferred Suspensions', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Increase education about the purpose of Suspensions and its roles in ultimately helping students reach their academic goals', 1, 1, 4, NOW(), NOW());
    -- Period 5 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Worked with ASC to offer Probation Workshops to all students on Probation earlier in the semester', 1, 1, 5, NOW(), NOW());

    -- G2 I2: Streamline the Suspension appeal process, developing policies that have the students'' best interest in mind
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Streamline the Suspension appeal process, developing policies that have the students'' best interest in mind', 2, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Streamline the Suspension appeal process, developing policies that have the students'' best interest in mind', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 5);
    -- Period 4 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Move Suspension Appeal Form to Google Forms', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Shorten timeline of appeals process to meet RO deadlines', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Tighten criteria for appeals that go to the President', 1, 1, 4, NOW(), NOW());
    -- Period 5 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Clarified and streamlined Suspension Appeals process more, unified and updated email templates for accepted appeals, rejected appeals, email to parents/guardians', 1, 1, 5, NOW(), NOW());

    -- G2 I3: Analyze the success of students who start in foundation courses (ELCA 062, MATH 90) to find trends and issues that can be addressed
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Analyze the success of students who start in foundation courses (ELCA 062, MATH 90) to find trends and issues that can be addressed', 3, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Analyze the success of students who start in foundation courses (ELCA 062, MATH 90) to find trends and issues that can be addressed', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 5);
    -- No completed achievements for this initiative in periods 4 or 5

    -- G2 I4: Continual use of Early Alerts to identify students who are struggling
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Continual use of Early Alerts to identify students who are struggling', 4, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Continual use of Early Alerts to identify students who are struggling', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 5);
    -- Period 5 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Worked towards more effective follow-up with students in response to Early Alerts', 1, 1, 5, NOW(), NOW());

    -- ===== GOAL 3: Optimize efficiency and clarity in our processes (3 initiatives) =====
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Optimize efficiency and clarity in our processes', 3, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Optimize efficiency and clarity in our processes', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_obj_id, 6);

    -- G3 I1: Develop an online repository of student advising resources (MyCourses, my.rit.edu, new website)
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Develop an online repository of student advising resources (MyCourses, my.rit.edu, new website)', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Develop an online repository of student advising resources (MyCourses, my.rit.edu, new website)', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 11);
    -- Period 4 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Share Advising calendar on Academic Dashboard', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Create dubai_advising listserv so faculty/staff can easily contact all advisors at once', 1, 1, 4, NOW(), NOW());
    -- Period 5 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Educated students on the Advising pages on the RIT Dubai website in more places to expand student awareness of resources available', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Website updates: enhanced Global Scholars page, added Transcript and Academic Verification sections, made Immersions and Minors more clear', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Added Student Enrollment numbers to Dashboard to include students enrolled in classes, studying abroad, on co-op so all faculty/staff have accurate numbers', 1, 1, 5, NOW(), NOW());

    -- G3 I2: Go paperless and move all internal forms to Google Forms
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Go paperless and move all internal forms to Google Forms', 2, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Go paperless and move all internal forms to Google Forms', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 11);
    -- Period 4 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Independent Course Teaching Requests, Online Course Exceptions, Suspension Appeals Forms moved to Google Sheets', 1, 1, 4, NOW(), NOW());
    -- Period 5 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Added features to Google Sheets for increased automation (Online pivot, New student sheet)', 1, 1, 5, NOW(), NOW());

    -- G3 I3: Create standardized documents to capture important processes for the departments
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Create standardized documents to capture important processes for the departments', 3, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Create standardized documents to capture important processes for the departments', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 4);
    -- Period 5 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Continued to develop our Advising Process and Procedures document to ensure all advisors are on the same page and aware of updates', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Developed New Advisor Training Plan to ensure all advisors have the information they need', 1, 1, 5, NOW(), NOW());

    -- ===== GOAL 4: Maximize positive satisfaction from all constituents - students, faculty, and staff (6 initiatives) =====
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Maximize positive satisfaction from all constituents - students, faculty, and staff', 4, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Maximize positive satisfaction from all constituents - students, faculty, and staff', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_obj_id, 3);

    -- G4 I1: Increase availability and responsiveness of advisors to students, faculty, and staff
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Increase availability and responsiveness of advisors to students, faculty, and staff', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Increase availability and responsiveness of advisors to students, faculty, and staff', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 5);
    -- Period 4 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Email instructors when adding new students in beyond add/drop deadline', 1, 1, 4, NOW(), NOW());
    -- Period 5 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Advisors of graduate programs to attend graduate orientation', 1, 1, 5, NOW(), NOW());

    -- G4 I2: Hiring plan to reduce advisor-advisee ratio and work towards having Department-specific advisors
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Hiring plan to reduce advisor-advisee ratio and work towards having Department-specific advisors', 2, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Hiring plan to reduce advisor-advisee ratio and work towards having Department-specific advisors', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 4);
    -- Period 5 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Continued to request the hiring of more advisors to reduce student/advisor ratio', 1, 1, 5, NOW(), NOW());

    -- G4 I3: Develop working relationship with other offices and Department Chairs
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Develop working relationship with other offices and Department Chairs', 3, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Develop working relationship with other offices and Department Chairs', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 4);
    -- Period 4 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Meet with Admissions each semester to review how Orientation went and suggest improvements', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'New hires to meet with Chairs during training', 1, 1, 4, NOW(), NOW());
    -- Period 5 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Provided training session for new faculty and new Faculty Advisors on advising processes', 1, 1, 5, NOW(), NOW());

    -- G4 I4: Professional development in interpersonal skills - counseling, listening, communication, emotional intelligence
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Professional development in interpersonal skills - counseling, listening, communication, emotional intelligence', 4, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Professional development in interpersonal skills - counseling, listening, communication, emotional intelligence', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 2);
    -- No completed achievements for this initiative in periods 4 or 5

    -- G4 I5: Regularly share ideas for best practices in developmental advising at team meetings
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Regularly share ideas for best practices in developmental advising at team meetings', 5, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Regularly share ideas for best practices in developmental advising at team meetings', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 4);
    -- Period 4 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Share Advising calendar on Academic Dashboard', 1, 1, 4, NOW(), NOW());

    -- G4 I6: Review Satisfaction Survey questions to capture more areas that align with our vision
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Review Satisfaction Survey questions to capture more areas that align with our vision', 6, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Review Satisfaction Survey questions to capture more areas that align with our vision', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 4);
    -- Period 5 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Created and shared Learning Outcomes of Advising and Advisor/Advisee responsibilities with new students', 1, 1, 5, NOW(), NOW());

    RAISE NOTICE 'V25: ADV strategy 5 re-seeded with 4 goals, 4 objectives, 15 initiatives, achievements for periods 4 and 5.';
END $$;
