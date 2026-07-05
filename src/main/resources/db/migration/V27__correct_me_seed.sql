-- V27: Re-seed ME (Mechanical Engineering, strategy 6) with correct structure.
DO $$
DECLARE
    v_strategy_id BIGINT := 6;
    v_goal_id     BIGINT;
    v_obj_id      BIGINT;
    v_init_id     BIGINT;
    v_meas_id     BIGINT;

    -- Goal 1 initiative ids
    v_g1i1_id     BIGINT;
    v_g1i2_id     BIGINT;
    v_g1i3_id     BIGINT;
    v_g1i4_id     BIGINT;
    v_g1i5_id     BIGINT;
    v_g1i6_id     BIGINT;
    v_g1i7_id     BIGINT;

    -- Goal 1 measurement ids
    v_g1m1_id     BIGINT;
    v_g1m2_id     BIGINT;
    v_g1m3_id     BIGINT;
    v_g1m4_id     BIGINT;
    v_g1m5_id     BIGINT;
    v_g1m6_id     BIGINT;
    v_g1m7_id     BIGINT;

    -- Goal 2 initiative ids
    v_g2i1_id     BIGINT;
    v_g2i2_id     BIGINT;
    v_g2i3_id     BIGINT;
    v_g2i4_id     BIGINT;
    v_g2i5_id     BIGINT;
    v_g2i6_id     BIGINT;

    -- Goal 2 measurement ids
    v_g2m1_id     BIGINT;
    v_g2m2_id     BIGINT;
    v_g2m3_id     BIGINT;
    v_g2m4_id     BIGINT;
    v_g2m5_id     BIGINT;
    v_g2m6_id     BIGINT;

    -- Goal 3 initiative ids
    v_g3i1_id     BIGINT;
    v_g3i2_id     BIGINT;
    v_g3i3_id     BIGINT;
    v_g3i4_id     BIGINT;

    -- Goal 3 measurement ids
    v_g3m1_id     BIGINT;
    v_g3m2_id     BIGINT;
    v_g3m3_id     BIGINT;
    v_g3m4_id     BIGINT;

    -- Goal 4 initiative ids
    v_g4i1_id     BIGINT;
    v_g4i2_id     BIGINT;
    v_g4i3_id     BIGINT;
    v_g4i4_id     BIGINT;

    -- Goal 4 measurement ids
    v_g4m1_id     BIGINT;
    v_g4m2_id     BIGINT;
    v_g4m3_id     BIGINT;
    v_g4m4_id     BIGINT;

    -- Goal 5 initiative ids
    v_g5i1_id     BIGINT;
    v_g5i2_id     BIGINT;
    v_g5i3_id     BIGINT;
    v_g5i4_id     BIGINT;
    v_g5i5_id     BIGINT;
    v_g5i6_id     BIGINT;

    -- Goal 5 measurement ids
    v_g5m1_id     BIGINT;
    v_g5m2_id     BIGINT;
    v_g5m3_id     BIGINT;
    v_g5m4_id     BIGINT;
    v_g5m5_id     BIGINT;
    v_g5m6_id     BIGINT;

BEGIN
    -- =========================================================
    -- CLEANUP: remove existing ME data for strategy 6
    -- =========================================================
    DELETE FROM goal WHERE strategy_id = v_strategy_id;

    -- =========================================================
    -- GOAL 1: Attain Academic Excellence and Innovation
    -- =========================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Attain Academic Excellence and Innovation', 1, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Attain Academic Excellence and Innovation', 1, false, 1)
    RETURNING id INTO v_obj_id;

    -- Objective mapping: Goal 1 → University Objective 7
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id)
    VALUES (v_obj_id, 7);

    -- Initiative 1
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Enhance the curriculum to incorporate new industry-relevant focus areas and certificates', 1, 1)
    RETURNING id INTO v_g1i1_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_g1i1_id, 13);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_g1i1_id, 'Enhance the curriculum to incorporate new industry-relevant focus areas and certificates', 1)
    RETURNING id INTO v_g1m1_id;

    -- Initiative 2
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Foster a culture of innovation via interdisciplinary projects and partnerships with local industries', 2, 1)
    RETURNING id INTO v_g1i2_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_g1i2_id, 12);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_g1i2_id, 'Foster a culture of innovation via interdisciplinary projects and partnerships with local industries', 1)
    RETURNING id INTO v_g1m2_id;

    -- Initiative 3
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Maintain local and international accreditation status', 3, 1)
    RETURNING id INTO v_g1i3_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_g1i3_id, 8);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_g1i3_id, 'Maintain local and international accreditation status', 1)
    RETURNING id INTO v_g1m3_id;

    -- Initiative 4
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Explore innovative teaching methods to adapt to changing educational trends', 4, 1)
    RETURNING id INTO v_g1i4_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_g1i4_id, 12);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_g1i4_id, 'Explore innovative teaching methods to adapt to changing educational trends', 1)
    RETURNING id INTO v_g1m4_id;

    -- Initiative 5
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Expand laboratory facilities', 5, 1)
    RETURNING id INTO v_g1i5_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_g1i5_id, 9);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_g1i5_id, 'Expand laboratory facilities', 1)
    RETURNING id INTO v_g1m5_id;

    -- Initiative 6
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Promote entrepreneurial activities', 6, 1)
    RETURNING id INTO v_g1i6_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_g1i6_id, 10);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_g1i6_id, 'Promote entrepreneurial activities', 1)
    RETURNING id INTO v_g1m6_id;

    -- Initiative 7
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Enforce a strict code of academic integrity among students and faculty', 7, 1)
    RETURNING id INTO v_g1i7_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_g1i7_id, 13);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_g1i7_id, 'Enforce a strict code of academic integrity among students and faculty', 1)
    RETURNING id INTO v_g1m7_id;

    -- Goal 1 Period 5 Achievements
    -- I1 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_g1m1_id, 'The BSIE curriculum now includes a new course titled Data Analytics and Predictive Modeling to align with recent industry trends and equip students with essential skills in this evolving field.', 1, 1, 5, NOW(), NOW());

    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_g1m1_id, 'The four programs collaboratively developed a set of AI-related outcomes to help track and measure the integration of AI across the curriculum. These outcomes were presented to the department''s Industry Advisory Board for feedback and alignment with industry expectations.', 1, 1, 5, NOW(), NOW());

    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_g1m1_id, 'Revived the offering of MECE 404: Robotics, reintroducing it into the curriculum after a long period of inactivity.', 1, 1, 5, NOW(), NOW());

    -- I3 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_g1m3_id, 'BSME, BSIE & MSME CAA reaccredited till 2028.', 1, 1, 5, NOW(), NOW());

    -- I5 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_g1m5_id, 'A desktop filament maker, plastic shredder, and polymer dryer have been added to our labs, enhancing the department''s capabilities in solid mechanics and polymer recycling.', 1, 1, 5, NOW(), NOW());

    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_g1m5_id, 'New additive manufacturing technologies were added to our labs: pallet-based printer, large-scale FDM printers, and continuous fiber-reinforced composite printer, expanding our capabilities in both high-volume prototyping and advanced material applications.', 1, 1, 5, NOW(), NOW());

    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_g1m5_id, 'Successful installation of a new compressed air pipeline system in our workshop, supplying a reliable air supply to CNC milling and turning centers, research and student projects requiring air pressure testing, pneumatic devices and actuators, as well as for cleaning workbenches and tools.', 1, 1, 5, NOW(), NOW());

    -- I6 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_g1m6_id, 'Multiple faculty members are collaborating with an MIT DesignX team on their startup idea, which is funded by DSO.', 1, 1, 5, NOW(), NOW());

    -- =========================================================
    -- GOAL 2: Increase Student Enrollment and Retention
    -- =========================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Increase Student Enrollment and Retention', 2, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Increase Student Enrollment and Retention', 1, false, 1)
    RETURNING id INTO v_obj_id;

    -- Objective mapping: Goal 2 → University Objective 16
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id)
    VALUES (v_obj_id, 16);

    -- Initiative 1
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Enhance the current Student for a Day workshops to include more advanced and contemporary topics', 1, 1)
    RETURNING id INTO v_g2i1_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_g2i1_id, 6);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_g2i1_id, 'Enhance the current Student for a Day workshops to include more advanced and contemporary topics', 1)
    RETURNING id INTO v_g2m1_id;

    -- Initiative 2
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Increase faculty and staff involvement in open days and introduce summer boot camps', 2, 1)
    RETURNING id INTO v_g2i2_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_g2i2_id, 6);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_g2i2_id, 'Increase faculty and staff involvement in open days and introduce summer boot camps', 1)
    RETURNING id INTO v_g2m2_id;

    -- Initiative 3
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Centralize all programs under one comprehensive webpage', 3, 1)
    RETURNING id INTO v_g2i3_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_g2i3_id, 11);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_g2i3_id, 'Centralize all programs under one comprehensive webpage', 1)
    RETURNING id INTO v_g2m3_id;

    -- Initiative 4
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Create opportunities for alumni involvement, mentorship, and networking', 4, 1)
    RETURNING id INTO v_g2i4_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_g2i4_id, 1);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_g2i4_id, 'Create opportunities for alumni involvement, mentorship, and networking', 1)
    RETURNING id INTO v_g2m4_id;

    -- Initiative 5
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Enhance the department''s social media presence, particularly on platforms like LinkedIn', 5, 1)
    RETURNING id INTO v_g2i5_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_g2i5_id, 11);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_g2i5_id, 'Enhance the department''s social media presence, particularly on platforms like LinkedIn', 1)
    RETURNING id INTO v_g2m5_id;

    -- Initiative 6
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Increase community engagement via workshops and seminars that are open to the local community', 6, 1)
    RETURNING id INTO v_g2i6_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_g2i6_id, 17);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_g2i6_id, 'Increase community engagement via workshops and seminars that are open to the local community', 1)
    RETURNING id INTO v_g2m6_id;

    -- Goal 2 Period 5 Achievements
    -- I1 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_g2m1_id, 'BSME enrollment increased from 56 to 61 (Fall 2023 vs. Fall 2024).', 1, 1, 5, NOW(), NOW());

    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_g2m1_id, 'BSIE enrollment increased from 23 to 38 (Fall 2023 vs. Fall 2024).', 1, 1, 5, NOW(), NOW());

    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_g2m1_id, 'MSME enrollment increased from 2 to 8 (Fall 2023 vs. Fall 2024).', 1, 1, 5, NOW(), NOW());

    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_g2m1_id, 'MSEM enrollment increased from 3 to 8 (Fall 2023 vs. Fall 2024).', 1, 1, 5, NOW(), NOW());

    -- I2 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_g2m2_id, 'The department participated in every Open Day and Student-for-a-Day event, actively engaging prospective students and their families. In collaboration with the Admissions team, faculty also conducted a series of hands-on workshops to showcase the department''s program offerings.', 1, 1, 5, NOW(), NOW());

    -- I3 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_g2m3_id, 'The department submitted self-studies to introduce a new dual BS-MS program in Mechanical Engineering, aiming to boost enrollment in the MS program by offering an accelerated pathway for high-performing undergraduate students.', 1, 1, 5, NOW(), NOW());

    -- I4 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_g2m4_id, 'IISE student chapter organized a networking event during Ramadan that brought together current ISE students and alumni, fostering connections and mentorship within the community.', 1, 1, 5, NOW(), NOW());

    -- I5 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_g2m5_id, 'The department''s LinkedIn page remains active, with over 1,200 genuine followers. All posts and engagement on the page are organic and reflect authentic interest from our community.', 1, 1, 5, NOW(), NOW());

    -- I6 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_g2m6_id, 'The ISEE 795 graduate seminar, offered for the first time and similar to the MECE one, is being conducted via Zoom to enhance accessibility. Invitations are also extended to the main campus to participate.', 1, 1, 5, NOW(), NOW());

    -- =========================================================
    -- GOAL 3: Empower, Attract and Retain Top-Tier Faculty
    -- =========================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Empower, Attract and Retain Top-Tier Faculty', 3, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Empower, Attract and Retain Top-Tier Faculty', 1, false, 1)
    RETURNING id INTO v_obj_id;

    -- Objective mapping: Goal 3 → University Objective 17
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id)
    VALUES (v_obj_id, 17);

    -- Initiative 1
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Develop faculty mentorship program', 1, 1)
    RETURNING id INTO v_g3i1_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_g3i1_id, 2);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_g3i1_id, 'Develop faculty mentorship program', 1)
    RETURNING id INTO v_g3m1_id;

    -- Initiative 2
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Explore various funding sources to support faculty development', 2, 1)
    RETURNING id INTO v_g3i2_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_g3i2_id, 2);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_g3i2_id, 'Explore various funding sources to support faculty development', 1)
    RETURNING id INTO v_g3m2_id;

    -- Initiative 3
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Promote a culture of mentorship and collaboration among faculty', 3, 1)
    RETURNING id INTO v_g3i3_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_g3i3_id, 2);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_g3i3_id, 'Promote a culture of mentorship and collaboration among faculty', 1)
    RETURNING id INTO v_g3m3_id;

    -- Initiative 4
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Increase support staff members such as Lab Engineers and Lab Technicians', 4, 1)
    RETURNING id INTO v_g3i4_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_g3i4_id, 4);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_g3i4_id, 'Increase support staff members such as Lab Engineers and Lab Technicians', 1)
    RETURNING id INTO v_g3m4_id;

    -- Goal 3 Period 5 Achievements
    -- I1 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_g3m1_id, 'Newly joined faculty member has been thoroughly briefed on the faculty evaluation rubrics.', 1, 1, 5, NOW(), NOW());

    -- I2 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_g3m2_id, 'A budget of AED 20k has been utilized for faculty development activities for the 2024-25 academic year.', 1, 1, 5, NOW(), NOW());

    -- I4 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_g3m4_id, 'Successfully hired an ISE adjunct at the senior executive level from GE Aerospace, who is currently teaching systems and project management.', 1, 1, 5, NOW(), NOW());

    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_g3m4_id, 'The search committee successfully hired a new ISE faculty member from a top-ranked Industrial Engineering program in the U.S. The new faculty member will join in Fall 2025.', 1, 1, 5, NOW(), NOW());

    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_g3m4_id, 'The department has successfully secured the budget to add one lab engineer position for the 2025-26 academic year.', 1, 1, 5, NOW(), NOW());

    -- =========================================================
    -- GOAL 4: Optimize Operational Efficiency
    -- =========================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Optimize Operational Efficiency', 4, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Optimize Operational Efficiency', 1, false, 1)
    RETURNING id INTO v_obj_id;

    -- Objective mapping: Goal 4 → University Objective 6
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id)
    VALUES (v_obj_id, 6);

    -- Initiative 1
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Streamline admin processes to reduce workload and enhance focus on teaching and research', 1, 1)
    RETURNING id INTO v_g4i1_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_g4i1_id, 11);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_g4i1_id, 'Streamline admin processes to reduce workload and enhance focus on teaching and research', 1)
    RETURNING id INTO v_g4m1_id;

    -- Initiative 2
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Assign and recognize faculty administrative roles such as Program Coordinators', 2, 1)
    RETURNING id INTO v_g4i2_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_g4i2_id, 4);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_g4i2_id, 'Assign and recognize faculty administrative roles such as Program Coordinators', 1)
    RETURNING id INTO v_g4m2_id;

    -- Initiative 3
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Identify and address gaps in existing policies and procedures', 3, 1)
    RETURNING id INTO v_g4i3_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_g4i3_id, 11);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_g4i3_id, 'Identify and address gaps in existing policies and procedures', 1)
    RETURNING id INTO v_g4m3_id;

    -- Initiative 4
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Automate PLO assessment and benchmarking', 4, 1)
    RETURNING id INTO v_g4i4_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_g4i4_id, 11);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_g4i4_id, 'Automate PLO assessment and benchmarking', 1)
    RETURNING id INTO v_g4m4_id;

    -- Goal 4 Period 5 Achievements
    -- I1 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_g4m1_id, 'The department''s research output is now systematically quantified and categorized to show the number and percentage of Q1 and Q2 journal publications, conference proceedings, and book chapters out of the total research output. In AY 2023-24, the department''s research output includes 19 journal papers alongside 14 conference presentations, 3 book chapters, and 1 book.', 1, 1, 5, NOW(), NOW());

    -- I2 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_g4m2_id, 'Three of the four programs have designated program coordinators. All four programs received an Advanced rating in RIT''s annual program assessment review.', 1, 1, 5, NOW(), NOW());

    -- I4 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_g4m4_id, 'For the BSME program-level assessment, all CLO data are now directly extracted from CARs to streamline the process and avoid redundancy, eliminating the need for Excel sheets and back-and-forth exchanges.', 1, 1, 5, NOW(), NOW());

    -- =========================================================
    -- GOAL 5: Attain Excellence in Research
    -- =========================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Attain Excellence in Research', 5, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Attain Excellence in Research', 1, false, 1)
    RETURNING id INTO v_obj_id;

    -- Objective mapping: Goal 5 → University Objective 5
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id)
    VALUES (v_obj_id, 5);

    -- Initiative 1
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Develop research groups and labs with state-of-the-art equipment', 1, 1)
    RETURNING id INTO v_g5i1_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_g5i1_id, 9);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_g5i1_id, 'Develop research groups and labs with state-of-the-art equipment', 1)
    RETURNING id INTO v_g5m1_id;

    -- Initiative 2
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Network with industry leaders to attract research funding', 2, 1)
    RETURNING id INTO v_g5i2_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_g5i2_id, 7);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_g5i2_id, 'Network with industry leaders to attract research funding', 1)
    RETURNING id INTO v_g5m2_id;

    -- Initiative 3
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Collaborate with local and international institutions, industries, and government agencies', 3, 1)
    RETURNING id INTO v_g5i3_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_g5i3_id, 7);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_g5i3_id, 'Collaborate with local and international institutions, industries, and government agencies', 1)
    RETURNING id INTO v_g5m3_id;

    -- Initiative 4
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Encourage student involvement in research through undergraduate research programs', 4, 1)
    RETURNING id INTO v_g5i4_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_g5i4_id, 10);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_g5i4_id, 'Encourage student involvement in research through undergraduate research programs', 1)
    RETURNING id INTO v_g5m4_id;

    -- Initiative 5
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Foster collaboration by facilitating cross-departmental partnerships within the university', 5, 1)
    RETURNING id INTO v_g5i5_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_g5i5_id, 9);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_g5i5_id, 'Foster collaboration by facilitating cross-departmental partnerships within the university', 1)
    RETURNING id INTO v_g5m5_id;

    -- Initiative 6
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Diversify funding sources and allocation', 6, 1)
    RETURNING id INTO v_g5i6_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_g5i6_id, 15);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_g5i6_id, 'Diversify funding sources and allocation', 1)
    RETURNING id INTO v_g5m6_id;

    -- Goal 5 Period 5 Achievements
    -- I1 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_g5m1_id, 'Two department-level research groups have been established, each with a distinct vision and set of objectives. Their work is actively showcased on RIT''s official webpage.', 1, 1, 5, NOW(), NOW());

    -- I3 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_g5m3_id, 'Multiple faculty publications, including journal co-authorships, in collaboration with industry partners such as DEWA.', 1, 1, 5, NOW(), NOW());

    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_g5m3_id, 'Hosted the International Conference on Optimization and Learning (OLA2025) at RIT Dubai from April 23 to 25.', 1, 1, 5, NOW(), NOW());

    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_g5m3_id, 'Several department faculty members maintain active international collaborations with institutions such as RIT Croatia, RIT (main), Carleton University, Qatar University, University of Regina, Universitat Politecnica de Valencia, Nanjing University of Aeronautics and Astronautics, and the University of Guelph.', 1, 1, 5, NOW(), NOW());

    -- I4 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_g5m4_id, 'The department hosted its first-ever research COOP student from the main campus.', 1, 1, 5, NOW(), NOW());

    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_g5m4_id, 'Several students have participated in research COOP programs under faculty supervision, resulting in multiple journal and conference publications.', 1, 1, 5, NOW(), NOW());

    -- I6 achievements
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_g5m6_id, 'Secured AED 84,000 in funding from one of the MIT DesignX startup participants, through DSO, to support research focused on innovative cooling technologies for GPUs.', 1, 1, 5, NOW(), NOW());

    RAISE NOTICE 'V27: ME strategy 6 re-seeded.';
END $$;
