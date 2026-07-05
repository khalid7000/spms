-- V24: Re-seed INSE (strategy 8) with correct structure.
--
-- Before: stale/incorrect seed data for strategy 8 (INSE, department 13).
-- After:  5 goals (one per Col-A row), 5 objectives (same title as goal),
--         11 initiatives distributed across objectives, 1 measurement per initiative,
--         achievements only for period 4 (2022-2024) where Col C was non-blank.
--         objective_mapping and initiative_mapping to university strategy.
--
-- Source: INSE Dept Vision Actions.xlsx (sheets: 2022-2024, 2024-2025, 2025-2026)
-- Only the 2022-2024 sheet (period 4) contains achievement data.
-- Assessment periods: 4=2022-2024, 5=2024-2025, 6=2025-2026

DO $$
DECLARE
    v_strategy_id BIGINT := 8;
    v_goal_id     BIGINT;
    v_obj_id      BIGINT;
    v_init_id     BIGINT;
    v_meas_id     BIGINT;
BEGIN
    DELETE FROM goal WHERE strategy_id = v_strategy_id;

    -- ===== GOAL 1: RIT Dubai is recognized for high-quality rigorous education proved by high ranking in the UAE and demanded students in the job market. (3 initiatives) =====
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'RIT Dubai is recognized for high-quality rigorous education proven by high ranking in the UAE and demanded students in the job market.', 1, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'RIT Dubai is recognized for high-quality rigorous education proven by high ranking in the UAE and demanded students in the job market.', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_obj_id, 4);

    -- I1: Collect, analyse, interpret, and disseminate institutional data
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Collect, analyse, interpret, and disseminate institutional data in order to facilitate better decision-making and improve overall effectiveness.', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Collect, analyse, interpret, and disseminate institutional data in order to facilitate better decision-making and improve overall effectiveness.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 4);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Conduct surveys amongst the internal stakeholders and analyze and distribute the results in each semester to the respective departments in a timely manner.', 1, 1, 4, NOW(), NOW());

    -- I2: Engage in benchmarking activities
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Engage in benchmarking activities to compare the institution''s performance against peer institutions, identifying areas for improvement and adopting best practices.', 2, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Engage in benchmarking activities to compare the institution''s performance against peer institutions, identifying areas for improvement and adopting best practices.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 13);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Conducting institutional and program-related benchmarking with peer institutions.', 1, 1, 4, NOW(), NOW());

    -- I3: Provide data to external stakeholders and regulatory bodies
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Efficiently provide data to external stakeholders and regulatory bodies to enhance University Ranking and Classification.', 3, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Efficiently provide data to external stakeholders and regulatory bodies to enhance University Ranking and Classification.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 27);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Submission of data to regulatory bodies for University Classification. Submission of semester-wise CHEDS and annual KHDA Census data in a timely manner.', 1, 1, 4, NOW(), NOW());

    -- ===== GOAL 2: 100% achievement of all accreditation applications (1 initiative) =====
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, '100% achievement of all accreditation applications', 2, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, '100% achievement of all accreditation applications', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_obj_id, 4);

    -- I4: Ensure compliance and alignment with accreditation requirements
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Ensure compliance and alignment with accreditation requirements in a timely manner by establishing processes that consistently meet or exceed standards set by the local and international accreditation bodies.', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Ensure compliance and alignment with accreditation requirements in a timely manner by establishing processes that consistently meet or exceed standards set by the local and international accreditation bodies.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 8);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Submit self-studies, ERT responses for renewal of institutional and program accreditation in compliance with CAA and international standards as per the IE calendar.', 1, 1, 4, NOW(), NOW());

    -- ===== GOAL 3: Increase RIT Dubai effectiveness, reach and academic success using technology (1 initiative) =====
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Increase RIT Dubai effectiveness, reach and academic success using technology', 3, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Increase RIT Dubai effectiveness, reach and academic success using technology', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_obj_id, 6);

    -- I5: Automate course folder review process
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Automate repetitive processes such as the course folder review process for efficient monitoring and maintenance of the course folders to achieve compliance with the CAA standards and to ensure effective usage of available human resources.', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Automate repetitive processes such as the course folder review process for efficient monitoring and maintenance of the course folders to achieve compliance with the CAA standards and to ensure effective usage of available human resources.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 11);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Acquire and implement course folder management software and train the faculty and IE staff on its efficient and effective usage.', 1, 1, 4, NOW(), NOW());

    -- ===== GOAL 4: Enhance Program Effectiveness through improved quality assurance and efficient processes (5 initiatives) =====
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Enhance Program Effectiveness through improved quality assurance and efficient processes', 4, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Enhance Program Effectiveness through improved quality assurance and efficient processes', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_obj_id, 7);

    -- I6: Enhance the evaluation framework
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Enhance the evaluation framework for ongoing assessment and improvement, focusing on academic programs, administrative services, and supporting units.', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Enhance the evaluation framework for ongoing assessment and improvement, focusing on academic programs, administrative services, and supporting units.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 4);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Implement the course folder management software with the right integration with SIS and mycourses and create a provision for receiving automated reports for program assessment and institutional effectiveness.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Improve the quality of annual self-evaluation reports and action plans and utilize them for continuous improvement.', 1, 1, 4, NOW(), NOW());

    -- I7: Support for developing and refining institutional plans
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Provide necessary support for developing and refining institutional plans, utilizing data insights to drive RIT Dubai towards its goals.', 2, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Provide necessary support for developing and refining institutional plans, utilizing data insights to drive RIT Dubai towards its goals.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 4);

    -- I8: Resources, training, and support to faculty
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Provide resources, training, and support to faculty and staff in utilizing the course assessment data for program evaluation and improvement.', 3, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Provide resources, training, and support to faculty and staff in utilizing the course assessment data for program evaluation and improvement.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 2);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Provide new faculty orientation, course folders workshops, constantly support new faculty with course folder related queries. Provide innovation outcomes assessment data.', 1, 1, 4, NOW(), NOW());

    -- I9: Professional development opportunities for IE staff
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Provide professional development opportunities for staff within the Institutional Effectiveness Unit to stay updated on best practices and emerging trends in assessment and data analysis.', 4, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Provide professional development opportunities for staff within the Institutional Effectiveness Unit to stay updated on best practices and emerging trends in assessment and data analysis.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 2);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Prepare a PD plan for all IE staff for the academic year, attend the planned training, and apply the outcomes for the applicable tasks within the IE office.', 1, 1, 4, NOW(), NOW());

    -- I10: Communicate assessment findings to stakeholders
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Regularly communicate assessment findings and institutional effectiveness reports to all departments and relevant stakeholders, fostering transparency and accountability.', 5, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Regularly communicate assessment findings and institutional effectiveness reports to all departments and relevant stakeholders, fostering transparency and accountability.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 4);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Prepare and submit the annual reports to department chairs and senior management before the prep week. Ensure all the course folders, CAR documents are complete in all aspects.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Disseminate student survey results, and updated handbooks including Policies and Procedures Manual to internal stakeholders by the end of AY 2023-24 and external stakeholders as required.', 1, 1, 4, NOW(), NOW());

    -- ===== GOAL 5: Introduce new programs and course offerings (1 initiative) =====
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Introduce new programs and course offerings', 5, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Introduce new programs and course offerings', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_obj_id, 7);

    -- I11: Prepare, submit, and obtain accreditation for new programs
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Prepare, submit, and obtain accreditation documents for new programs (BFA in New Media Design and BS in Advertising and Public Relations) as per the required standards and in a timely manner.', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Prepare, submit, and obtain accreditation documents for new programs (BFA in New Media Design and BS in Advertising and Public Relations) as per the required standards and in a timely manner.', 1)
    RETURNING id INTO v_meas_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 14);
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id, 'Support MOE visits, and submit responses for ERT reviews for new programs and achieve initial program accreditation within the set timeline.', 1, 1, 4, NOW(), NOW());

    RAISE NOTICE 'V24: INSE strategy 8 re-seeded with 5 goals, 5 objectives, 11 initiatives, 11 achievements.';
END $$;
