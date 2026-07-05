-- V29: Re-seed EE (Electrical Engineering, strategy 4) with correct structure.
DO $$
DECLARE
    v_strategy_id BIGINT := 4;
    v_goal_id     BIGINT;
    v_obj_id      BIGINT;
    v_init_id     BIGINT;
    -- Goal 1 measurements
    v_meas_g1i1   BIGINT; -- Advocate teaching rigor / zero tolerance
    v_meas_g1i2   BIGINT; -- Maintain alignment with main campus
    v_meas_g1i3   BIGINT; -- Align curricula with emerging technologies
    v_meas_g1i4   BIGINT; -- Foster culture of innovation
    v_meas_g1i5   BIGINT; -- Leverage advanced labs
    v_meas_g1i6   BIGINT; -- Maintain accreditation status
    v_meas_g1i7   BIGINT; -- Increase success in faculty hire picks
    v_meas_g1i8   BIGINT; -- Improve delivery of capstone projects
    v_meas_g1i9   BIGINT; -- Meeting KPIs associated with DT lab
    -- Goal 2 measurements
    v_meas_g2i1   BIGINT; -- Create research groups
    v_meas_g2i2   BIGINT; -- Assign faculty mentor per course/area
    v_meas_g2i3   BIGINT; -- Ensure diversity in faculty/staff hirings
    v_meas_g2i4   BIGINT; -- Use course file peer review process
    v_meas_g2i5   BIGINT; -- Enhance collaboration through interdisciplinary research
    v_meas_g2i6   BIGINT; -- Use research co-op and capstones
    -- Goal 3 measurements
    v_meas_g3i1   BIGINT; -- Create new framework for capstone operation
    v_meas_g3i2   BIGINT; -- Add standing committees
    v_meas_g3i3   BIGINT; -- Establish Gen AI working group
    v_meas_g3i4   BIGINT; -- Create rotation plan for program leads
    -- Goal 4 measurements
    v_meas_g4i1   BIGINT; -- Promote better research production
    v_meas_g4i2   BIGINT; -- Organize symposium/conference
    v_meas_g4i3   BIGINT; -- Secure new internal/external research funds
    v_meas_g4i4   BIGINT; -- Leverage collaboration across RIT campuses
    v_meas_g4i5   BIGINT; -- Support engineering/computing PhD programs
    v_meas_g4i6   BIGINT; -- Form AI Research focus group
    v_meas_g4i7   BIGINT; -- Provide guidance to EECS research committee
    v_meas_g4i8   BIGINT; -- Promote inter-campus collaboration
    v_meas_g4i9   BIGINT; -- Attract qualified GRAs and post-docs
    -- Goal 5 measurements
    v_meas_g5i1   BIGINT; -- Engagements in feasibility studies / new programs
    v_meas_g5i2   BIGINT; -- Engagements in accreditation processes
    v_meas_g5i3   BIGINT; -- Engagements with main campus
    v_meas_g5i4   BIGINT; -- New competitions and challenges with partners
    v_meas_g5i5   BIGINT; -- Capstone project themes / sponsors
    v_meas_g5i6   BIGINT; -- Use IAB charter and bylaws
    -- Goal 6 measurements
    v_meas_g6i1   BIGINT; -- Expand RIT Dubai degree offerings
    v_meas_g6i2   BIGINT; -- Reach out to key partners/government for scholarships
    v_meas_g6i3   BIGINT; -- Offer GRAs to qualified students
    v_meas_g6i4   BIGINT; -- List MSCEC with TRA/ICTFund
    v_meas_g6i5   BIGINT; -- Leverage annual competitions for outreach
    v_meas_g6i6   BIGINT; -- Support recruitment/marketing teams
    v_meas_g6i7   BIGINT; -- Work with student recruitment agents
    v_meas_g6i8   BIGINT; -- Create system of support for undergraduate recruitment
    v_meas_g6i9   BIGINT; -- Leverage research/training in advanced labs
    v_meas_g6i10  BIGINT; -- Improve content presentation on RIT website
    v_meas_g6i11  BIGINT; -- Publish bi-yearly newsletter
    -- Goal 7 measurements
    v_meas_g7i1   BIGINT; -- Maintain attrition below 10%
    v_meas_g7i2   BIGINT; -- Promote efficient student advising
    v_meas_g7i3   BIGINT; -- Diversify elective offerings
    v_meas_g7i4   BIGINT; -- Help create new co-op opportunities
    v_meas_g7i5   BIGINT; -- Offer students more co-curricular activities
    v_meas_g7i6   BIGINT; -- Sustain TA support system
    v_meas_g7i7   BIGINT; -- Recognize student achievements
    v_meas_g7i8   BIGINT; -- Add more qualified faculty
    v_meas_g7i9   BIGINT; -- Reduce DFW rates in key courses
    v_meas_g7i10  BIGINT; -- Advocate teaching rigor and effective learning
    v_meas_g7i11  BIGINT; -- Make sure students have necessary labs/computing resources
    -- Goal 8 measurements
    v_meas_g8i1   BIGINT; -- Secure BSEE accreditation with ABET
    v_meas_g8i2   BIGINT; -- Secure BSEE accreditation with ministry
    v_meas_g8i3   BIGINT; -- Secure MSEE accreditation with ministry
    v_meas_g8i4   BIGINT; -- Secure CIT accreditation with ministry
    v_meas_g8i5   BIGINT; -- Secure approval of BS CSEC substantive changes
    v_meas_g8i6   BIGINT; -- Secure approval of MS CSEC substantive change
    v_meas_g8i7   BIGINT; -- Secure BS CSEC accreditation with ministry
    v_meas_g8i8   BIGINT; -- Secure MS CSEC accreditation with ministry
    -- Goal 9 measurements
    v_meas_g9i1   BIGINT; -- Add one degree in ICT domain
    v_meas_g9i2   BIGINT; -- Add micro master programs in energy/cybersecurity
    v_meas_g9i3   BIGINT; -- Add BS/MS dual degree in EE
    v_meas_g9i4   BIGINT; -- Add BS/MS dual degree in cybersecurity
BEGIN
    DELETE FROM goal WHERE strategy_id = v_strategy_id;

    -- =========================================================
    -- GOAL 1: Academic Excellence and Innovation
    -- =========================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Academic Excellence and Innovation', 1, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Academic Excellence and Innovation', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id)
    VALUES (v_obj_id, 7);

    -- Initiative 1
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Advocate by example teaching rigor and effective learning environment. Seek academic excellence and foster an environment of zero tolerance to cheating and plagiarism.', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 12);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Advocate by example teaching rigor and effective learning environment. Seek academic excellence and foster an environment of zero tolerance to cheating and plagiarism.', 1)
    RETURNING id INTO v_meas_g1i1;

    -- Initiative 2
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Maintain alignment with the main campus on instruction, curricula, and faculty qualifications through mutual visits, GLEC platform, and assigning a point of contact from NY for each program', 2, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 2);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Maintain alignment with the main campus on instruction, curricula, and faculty qualifications through mutual visits, GLEC platform, and assigning a point of contact from NY for each program', 1)
    RETURNING id INTO v_meas_g1i2;

    -- Initiative 3
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Align curricula with emerging technologies and desired competences', 3, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 13);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Align curricula with emerging technologies and desired competences', 1)
    RETURNING id INTO v_meas_g1i3;

    -- Initiative 4
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Foster a culture of innovation via interdisciplinary projects and partnerships with local industries', 4, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 10);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Foster a culture of innovation via interdisciplinary projects and partnerships with local industries', 1)
    RETURNING id INTO v_meas_g1i4;

    -- Initiative 5
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Leverage the advanced labs (DT, AI/Robotics, Energy, Cybersecurity) to learn advanced technologies, realize innovations and to support research', 5, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 7);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Leverage the advanced labs (DT, AI/Robotics, Energy, Cybersecurity) to learn advanced technologies, realize innovations and to support research', 1)
    RETURNING id INTO v_meas_g1i5;

    -- Initiative 6
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Maintain local and international accreditation status', 6, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 8);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Maintain local and international accreditation status', 1)
    RETURNING id INTO v_meas_g1i6;

    -- Initiative 7
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Increase success in faculty hire picks as well as securing high quality faculty member hires', 7, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 4);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Increase success in faculty hire picks as well as securing high quality faculty member hires', 1)
    RETURNING id INTO v_meas_g1i7;

    -- Initiative 8
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Improve the delivery of the capstone projects', 8, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 4);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Improve the delivery of the capstone projects', 1)
    RETURNING id INTO v_meas_g1i8;

    -- Initiative 9
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Meeting the KPIs associated with the DT lab', 9, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 11);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Meeting the KPIs associated with the DT lab', 1)
    RETURNING id INTO v_meas_g1i9;

    -- Period 4 achievements for Goal 1
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i2, 'NY assigned point of contacts for CIT and CSEC. GLEC committee is engaging to incorporate the input of RIT Dubai into the updates to the curriculum.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i2, 'iSchool director and assistant director are planning a visit to RIT Dubai on Feb. 26-28, 2024.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i3, 'AI option has been added to the BSEE degree.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i3, 'MSEE curriculum has been redesigned with focus areas to support AI and smart solutions in energy and robotics.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i3, 'A zero-credit graduate seminar (EEEE-795) has been added to the MSEE curriculum.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i1, 'EECS department organized 8 research-based seminars/webinars in 2021-22, 9 in 2022-23, and 8 in 2023-24.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i3, 'Proposed critical changes to the CIT program (the GCIS-123/124 sequence and the program introductory course).', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i3, 'Partnered with the main campus on introducing Code Zero course to help CIT students prepare for GCIS-123 (Fall 2023).', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i3, 'A new elective in energy has been introduced by the EE department and have secured approval by the program and university CC as well as the Academic council (Spring 2024).', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i1, 'Identified a learning barrier due to inadequate background in English and worked with advisors and the English department on solutions (Fall 2023).', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i4, 'The Innovation journey has been enhanced with collaborations with industry partners like Intel, Software AG, Schneider Electric, and Intarel leveraging the AI/Robotics and Digital Systems labs.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i3, 'Gen-AI usage in the EE and computing courses is being studied (Spring 2024).', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i5, 'AI/Robotics lab has supported key robotics competitions and produced many research engagements that led to several publications.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i9, 'Digital Transformation lab has satisfactorily met all KPIs associated with the ICTFund contract, including research.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i7, 'Budget for new FT hires has been secured for 2024-25. Ads are being advertised with outlets with good reach to qualified faculty. The main campus is also on board to support.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i6, 'All EE and computing programs have secured ABET and the ministry accreditations.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i8, 'A new capstone framework has been established since Fall 2023 along with a standing committee and is leading to several improvements in the delivery of the projects.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i9, 'All benchmarks associated with the DT lab (publications & patent, training, utilization, open source) have been met or exceeded.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i3, 'Substantive changes for changing the degree title for CSEC and MS CSEC have been submitted on Jan. 29, 2024.', 1, 1, 4, NOW(), NOW());

    -- Period 5 achievements for Goal 1
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i3, 'The department has submitted two of the dual degree applications to NY and received approval. The degree options offer high-achieving students an accelerated path to complete the BS and MS degrees in EE and Cybersecurity in 5 years.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i6, 'The ministry requirements for the three computing programs have been addressed and finalised.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i3, 'The new Health Informatics Advanced Certificate has been submitted to the Ministry of Education for accreditation. The same certificate has already been approved on the main campus.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i3, 'The new Digital Twins course has been finalized with a plan to offer it in Fall 2025 to engineering and computing students. Two instructors from computing and industrial engineering will share the teaching.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i4, 'Engagement has been ongoing with the School of Interactive Games and Media in relation to the Game Development Program. Additional engagements are also happening with Dubai government entities and some industry partners (e.g., SynPlant/CoTwin). We have also offered a game development workshop to be repeated in Fall 2025 along with the formation of a student club.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i1, 'The peer review process for the course files has been revisited by adding criteria to incorporate DFW rates, courses offered for the first time and other unique courses as well.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i3, 'The department has discussed e-learning and its suitability to engineering and computing offering. It was agreed that e-learning is not generally recommended for undergraduate courses except for few courses like the digital twin. It would be beneficial in micro credential courses and co-listed courses between graduate and undergraduate students.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i3, 'A certification co-op pathway was created, with the aim of enabling students to pursue industry recognized certifications as part of their co-op learning experience.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i3, 'The department led discussions with the heads of the iSchool and the Computer Engineering department at NY to introduce new academic programs in Artificial Intelligence (at the BS or MS level) and Computer Engineering (BS). The BS in Computer Engineering has entered the implementation phase.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i7, 'The department has successfully hired a full-time faculty in network systems and a lab instructor for the computing labs.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i5, 'EC-Council solution has been approved. The solution will augment students'' learning in practical use cases in cybersecurity and prepares them for competitions such as CTF and CPTC. It also offers a co-op path for students.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i8, 'Capstone framework has been fine-tuned to include themes where faculty and industry sponsors will be invited to submit project ideas to the proposed themes.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i4, 'The department has led the Innovation Day, an event that included Imagine RIT and Capstone project showcases. The event featured innovative work from classes, theses, capstones, and research co-ops, joined by the industry and community at large.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i9, 'The Smart Energy Lab has received two boxes of sensors from SmartKable, designed for data acquisition from the power grid. These sensors will enable the application of analytics and AI to support informed decision-making for more efficient grid operations.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i1, 'The department has organised its annual series of webinars (6 webinars) for graduate and senior students. The webinars covered topics in AI, robotics, energy, and communications. In addition, the department has contributed to the college series of webinars with two webinars related to computing and electrical engineering.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i5, 'The EEEE-547 and EEEE-220 courses sustained their strong industry engagement through final projects, where students collaborated with leading organizations, including Schneider, Emerson, TDRA, Ministry of Energy and Infrastructure, Mastercard, and Odoo, to ideate and implement innovative solutions.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g1i1, 'The lockdown browser has been finalized and successfully tested for use in online assessments, aiming to minimize incidents of academic integrity violations.', 1, 1, 5, NOW(), NOW());

    -- =========================================================
    -- GOAL 2: Culture of leadership, teamwork, collaboration
    -- =========================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Maintain a culture of leadership, teamwork, collaboration in the department conducive to productivity and high achievements in teaching, research, and services leveraging the complex but rich cultural and educational diversity of the EECS faculty.', 2, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Maintain a culture of leadership, teamwork, collaboration in the department conducive to productivity and high achievements in teaching, research, and services leveraging the diversity of the EECS faculty.', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id)
    VALUES (v_obj_id, 7);

    -- Initiative 1
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Create research groups with focus on themes such as health care and digital transformation to support joint research and collaboration', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 7);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Create research groups with focus on themes such as health care and digital transformation to support joint research and collaboration', 1)
    RETURNING id INTO v_meas_g2i1;

    -- Initiative 2
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Assign a faculty mentor for each course or area who will provide guidance and support to other faculty especially the new faculty and adjuncts', 2, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 2);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Assign a faculty mentor for each course or area who will provide guidance and support to other faculty especially the new faculty and adjuncts', 1)
    RETURNING id INTO v_meas_g2i2;

    -- Initiative 3
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Ensure diversity in faculty and staff hirings', 3, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 4);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Ensure diversity in faculty and staff hirings', 1)
    RETURNING id INTO v_meas_g2i3;

    -- Initiative 4
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Use course file peer review process to support quality and collaboration amongst faculty', 4, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 12);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Use course file peer review process to support quality and collaboration amongst faculty', 1)
    RETURNING id INTO v_meas_g2i4;

    -- Initiative 5
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Enhance collaboration through interdisciplinary research', 5, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 10);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Enhance collaboration through interdisciplinary research', 1)
    RETURNING id INTO v_meas_g2i5;

    -- Initiative 6
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Use research co-op and capstones to promote collaboration and teamwork', 6, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 23);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Use research co-op and capstones to promote collaboration and teamwork', 1)
    RETURNING id INTO v_meas_g2i6;

    -- Period 4 achievements for Goal 2
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g2i1, 'A research group in AI has been established in Fall 2023 with focus on healthcare.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g2i6, 'Capstone theme in digital transformation will be introduced in Fall 2024.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g2i2, 'Faculty have been supporting each other through mentorship: Faculty mentorship per area: Dr. Khalil for database & Information Requirements Modeling; Dr. Omar for programming, Networking and software engineering; Dr. Huda for Routing & Switching and Networking; Dr. Ali for programming and database; Dr. Kevser and Dr. Wesam for CSEC courses; Dr. Jinane for AI courses; Dr. Abdullah for Energy.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g2i3, 'Faculty & staff come from 13 nationalities with 47% females.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g2i4, '13 courses were peer reviewed in Fall 2023.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g2i5, 'All capstones are interdisciplinary; Dr. Khalil led two interdisciplinary research projects; Dr. Muhieddin and Dr. Jinane led an interdisciplinary inter-campus research project; Dr. Muhieddin and Dr. Omar are leading an inter-campus research project in 5G.', 1, 1, 4, NOW(), NOW());

    -- Period 5 achievements for Goal 2
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g2i5, 'The Digital Twins course has been designed to serve both engineering and computing students. This offering reflects a strong interdisciplinary collaboration between the EECS and MEIE departments, with faculty from computing and industrial engineering jointly delivering the course content.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g2i5, 'The department discussed with the i-school the idea of collaboration between the two campuses on a capstone project where two student teams from Dubai and NY work jointly on designing and implementing the project under the supervision of mentors from both campuses.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g2i1, 'Two more research groups have been established for a total of 3 at the department level. Recent groups are Cybersecurity and Wireless Communications. The groups will promote research in the fields of cybersecurity and communications. Their scope and research contributions are now featured on the main RIT website.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g2i2, 'Coordinators have been appointed for core courses to assist faculty and adjuncts in aligning course materials and coordinating the delivery of assessments.', 1, 1, 5, NOW(), NOW());

    -- =========================================================
    -- GOAL 3: Add more organizational structure
    -- =========================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Add more organizational structure to the department with a wider governance including creating more standing committees.', 3, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Add more organizational structure to the department with a wider governance including creating more standing committees.', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id)
    VALUES (v_obj_id, 6);

    -- Initiative 1
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Create a new framework for the capstone operation to improve faculty and student engagement', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 4);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Create a new framework for the capstone operation to improve faculty and student engagement', 1)
    RETURNING id INTO v_meas_g3i1;

    -- Initiative 2
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Add another standing committee in 2023-24 and one more in 2024-25', 2, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 4);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Add another standing committee in 2023-24 and one more in 2024-25', 1)
    RETURNING id INTO v_meas_g3i2;

    -- Initiative 3
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Establish a working group for the Gen AI roll out plan, which could lead to a standing committee', 3, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 6);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Establish a working group for the Gen AI roll out plan, which could lead to a standing committee', 1)
    RETURNING id INTO v_meas_g3i3;

    -- Initiative 4
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Create a rotation plan for the program leads to help develop faculty for the admin roles', 4, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 4);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Create a rotation plan for the program leads to help develop faculty for the admin roles', 1)
    RETURNING id INTO v_meas_g3i4;

    -- Period 4 achievements for Goal 3
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g3i1, 'The new capstone framework has been established since Fall 2023 along with a standing committee and is leading to several improvements in the delivery of the projects. The model of the capstone and research committees is promising and will be enhanced and replicated in other areas.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g3i2, 'Research Committee has been established since the start of 2021-22 and an AI research-focused group was established in Fall 2023.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g3i2, 'Capstone Committee was established starting AY 2022-23.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g3i3, 'Gen-AI Working Group was established in Spring 2024.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g3i4, 'Rotation on program leadership has been in place since the introduction of the EECS programs: BSEE program assessment rotation included Dr. Muhieddin, Dr. Boutheina, Dr. Jinane; MSEE rotation included Dr. Muhieddin, Dr. Abdullah; CIT rotation included Dr. Omar, Dr. Khalil, Dr. Omar; CSEC rotation included Dr. Wesam, Dr. Kevser; MSCSEC rotation included Dr. Ali, Dr. Huda.', 1, 1, 4, NOW(), NOW());

    -- Period 5 achievements for Goal 3
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g3i4, 'A new lead Dr. Ali Assi has been assigned for the CIT program.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g3i2, 'A new committee has been created to review and enhance the process for Master''s theses and graduate papers/capstones within the EE and Cybersecurity programs. The committee has developed a preliminary draft outlining comprehensive guidelines for thesis completion, including advising protocols, timelines, publication opportunities, and final submission requirements.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g3i3, 'The program leads have been in charge of addressing the Gen AI inclusion in the courses and following up with faculty to ensure proper incorporation of the required content.', 1, 1, 5, NOW(), NOW());

    -- =========================================================
    -- GOAL 4: Support university achieve better ranking on research
    -- =========================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Support the university achieve better ranking on research. The EECS department has established a positive subscription to this theme and some faculty have already established research collaboration with the industry and top 200 universities.', 4, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Support the university achieve better ranking on research. The EECS department has established a positive subscription to this theme and some faculty have already established research collaboration with the industry and top 200 universities.', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id)
    VALUES (v_obj_id, 5);

    -- Initiative 1
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Promote better research production evidenced by quality publications and possibly patents and collaboration with top 200 universities', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 7);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Promote better research production evidenced by quality publications and possibly patents and collaboration with top 200 universities', 1)
    RETURNING id INTO v_meas_g4i1;

    -- Initiative 2
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Organize one symposium and one conference by 2024', 2, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 9);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Organize one symposium and one conference by 2024', 1)
    RETURNING id INTO v_meas_g4i2;

    -- Initiative 3
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Secure new internal and external research funds', 3, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 15);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Secure new internal and external research funds', 1)
    RETURNING id INTO v_meas_g4i3;

    -- Initiative 4
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Leverage collaboration across RIT campuses and apply for the RIT Global Faculty Research Grants', 4, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 7);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Leverage collaboration across RIT campuses and apply for the RIT Global Faculty Research Grants', 1)
    RETURNING id INTO v_meas_g4i4;

    -- Initiative 5
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Support the engineering and computing PhD programs through joint research and co-supervision with the main campus', 5, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 9);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Support the engineering and computing PhD programs through joint research and co-supervision with the main campus', 1)
    RETURNING id INTO v_meas_g4i5;

    -- Initiative 6
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Form an AI Research focus group, in addition to the general research committee', 6, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 9);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Form an AI Research focus group, in addition to the general research committee', 1)
    RETURNING id INTO v_meas_g4i6;

    -- Initiative 7
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Provide guidance & support to the EECS research committee to prompt research across different disciplines and to find new ways to attract research funds', 7, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 7);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Provide guidance & support to the EECS research committee to prompt research across different disciplines and to find new ways to attract research funds', 1)
    RETURNING id INTO v_meas_g4i7;

    -- Initiative 8
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Promote inter-campus collaboration and facilitate faculty exchange and visits to support research', 8, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 17);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Promote inter-campus collaboration and facilitate faculty exchange and visits to support research', 1)
    RETURNING id INTO v_meas_g4i8;

    -- Initiative 9
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Attract qualified GRAs and post-docs to support research', 9, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 7);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Attract qualified GRAs and post-docs to support research', 1)
    RETURNING id INTO v_meas_g4i9;

    -- Period 4 achievements for Goal 4
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g4i1, 'All EECS full-time faculty members have produced publications in 2021-22, and 2022-23 and many have received research grants.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g4i7, 'Research Committee has been established since the start of 2021-22.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g4i6, 'AI research group has been established starting in Fall 2023 with 7 members and counting.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g4i4, 'One research project has been going on in collaboration with researchers from the Croatia campus since Fall 2022 and secured a funding of $10,000 which has already supported the visit of the Croatia researchers to RIT Dubai.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g4i9, 'EE faculty added two post-docs and one sGTA and one GTA to their research groups starting in Fall 2023.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g4i9, 'Computing faculty added two GTAs to their research groups starting in Fall 2022.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g4i3, 'Collaboration with NewBridge Pharmaceutical, TechMed, MBRU, WIA, and Fakeeh Hospital on various AI based projects utilizing the capability of the DT lab.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g4i2, 'EECS department co-organized and participated in the "AI, Cybersecurity & Metaverse in Healthcare" symposium, Oct. 14, 2023.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g4i2, 'EECS department participated as speakers and moderators in the "Generative AI - Opportunities and Challenges" symposium, Jan. 11, 2024.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g4i2, 'EECS department is planning an IEEE-sponsored conference "AI, Cybersecurity & Metaverse" in Oct. 2024.', 1, 1, 4, NOW(), NOW());

    -- Period 5 achievements for Goal 4
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g4i1, 'EECS full-time faculty members have produced publications including journal and conference papers.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g4i3, 'Three RDI grants have been secured with a total of AED 2 millions. The RDI grants include collaborations with universities, such as MBRU, UoD, AUD, and McGill, in addition to government entities, such as Dubai Health, Dubai Municipality, and Dubai Police Academy.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g4i3, 'Two other external research grants have been secured with SmartKable (AED 224,000) and with Women in Research (AED 10,000).', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g4i6, 'A total of three research groups include faculty, GTAs, and postdoc researchers to promote research in the fields of AI, cybersecurity, and communications.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g4i9, 'Three GTAs have been enrolled to be added to the advanced labs and research groups with the aim of supporting the department with research in robotics, cybersecurity, and IoT.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g4i3, 'The department has contributed to the drafting of a proposal submitted by the college to establish an AI lab with the collaboration of Dubai Digital Authority. The main role of the lab is to provide research opportunities related to the applications of AI in several fields, including healthcare, mobility, energy, tourism, space, education, and more.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g4i8, 'An MoU was signed with the Lebanese American University (LAU) for research collaboration and student exchange.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g4i3, 'An NDA has been signed with the Ministry of Energy and Infrastructure to facilitate collaborative research focused on predictive analytics for UAE energy consumption and production.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g4i2, 'The department participated in the joint bid to host the IEEE International Symposium on Biomedical Imaging (ISBI) in 2027. The bid was made in collaboration with Zayed University, University of Dubai, MBRU, and Khalifa University.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g4i2, 'The second edition of the ICAMAC conference is planned to be organised and hosted at RIT in October 2025.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g4i2, 'The department will bring the IoT Security (SaSeIoT) conference that will be hosted at RIT in November 2025.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g4i1, 'EECS faculty have participated as speakers and moderators at various conferences, panels, and symposiums, locally and internationally.', 1, 1, 5, NOW(), NOW());

    -- =========================================================
    -- GOAL 5: Community Engagement: Maintain critical relationships with IAB
    -- =========================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Community Engagement: Maintain critical relationships and engagements with the IAB, sponsors and strategic partners and establish new collaborations on projects, co-op, and capstone sponsorship.', 5, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Community Engagement: Maintain critical relationships and engagements with the IAB, sponsors and strategic partners and establish new collaborations on projects, co-op, and capstone sponsorship.', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id)
    VALUES (v_obj_id, 13);

    -- Initiative 1
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Engagements in the feasibility studies and in developing new minors, concentrations, and majors', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 13);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Engagements in the feasibility studies and in developing new minors, concentrations, and majors', 1)
    RETURNING id INTO v_meas_g5i1;

    -- Initiative 2
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Engagements in the accreditation processes', 2, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 8);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Engagements in the accreditation processes', 1)
    RETURNING id INTO v_meas_g5i2;

    -- Initiative 3
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Engagements and collaborations with the main campus', 3, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 29);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Engagements and collaborations with the main campus', 1)
    RETURNING id INTO v_meas_g5i3;

    -- Initiative 4
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'New competitions and challenges in collaboration with partners', 4, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 11);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'New competitions and challenges in collaboration with partners', 1)
    RETURNING id INTO v_meas_g5i4;

    -- Initiative 5
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Establish capstone project themes and invite faculty and partners to propose and sponsor project ideas', 5, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 3);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Establish capstone project themes and invite faculty and partners to propose and sponsor project ideas', 1)
    RETURNING id INTO v_meas_g5i5;

    -- Initiative 6
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Use the new IAB charter and bylaws to promote more engagements by the advisory board members', 6, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 13);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Use the new IAB charter and bylaws to promote more engagements by the advisory board members', 1)
    RETURNING id INTO v_meas_g5i6;

    -- Period 4 achievements for Goal 5
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g5i5, 'Since Fall 2022, X capstones and Y theses were sponsored by industry.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g5i1, 'Collaboration with NewBridge Pharmaceutical, TechMed, MBRU, WIA, and Fakeeh Hospital on various AI based projects utilizing the capability of the DT lab.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g5i4, 'Dr. Jinane and Dr. Omar developed a drone AI-enabled system for agricultural spectral imaging to determine plant health and to control irrigation.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g5i4, 'Dr. Omar mentored a high-school capstone using SVM classifier for the taslima project.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g5i3, 'EE department employed an intern to engage in the AI and robotics solutions.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g5i4, 'Dr. Omar offered a workshop on gaming design to high-school students in collaboration with HP.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g5i4, 'Dr. Khalil, Dr. Omar, Dr. Jinane participated in the Arabic Hackathon Forum to incorporate AI in the Arabic language teaching and lesson planning (Fall 2023).', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g5i4, 'Dr. Jinane offered a half-day workshop to 50 teachers on the use of AI in lesson planning and teaching methodology.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g5i4, 'Dr. Boutheina and Dr. Jinane - Intensive involvement in IEEE, WIE, and WIA activities.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g5i6, 'The new charter and bylaws have been presented to the IAB in the May 11, 2023 meeting. The IAB members are keen to support the expectations set forth in the charter.', 1, 1, 4, NOW(), NOW());

    -- Period 5 achievements for Goal 5
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g5i4, 'The EECS Department delivered training in Digital Design to 200 Year 12 students from DIA, with support from volunteer EE students and members of the AI/Robotics student group.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g5i3, 'Dr. Jinane received USD 10,000 from Rochester to form a team of students who will work in collaboration with the NY campus on designing and implementing a social robot that interacts with the students and collects their feedback to enhance their learning experience.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g5i4, 'A panel discussion with NASA delegates, conducted in collaboration with the US Mission to the UAE, included Dr. Jinane as one of the panelists.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g5i1, 'Collaborations with the Department of Economy and Tourism have been ongoing with Dr. Ahmed and Dr. Omar on creating games and animations for tourism sector.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g5i1, 'Collaborations with Amman and Gulf Center for Strategic Studies have been ongoing with respect to using AI in knowledge creation.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g5i6, 'An MoU has been signed with IEEE to formalize collaboration on academic initiatives, research activities, and professional development opportunities for students and faculty.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g5i1, 'A visit to DEWA by Dr. Abdulla and Dr. Jinane explored potential collaborations in research initiatives and training workshops.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g5i4, 'The department has renewed its partnership with the Machines Can See event. The event offers 15 free seats for faculty and students and a booth for RIT students to present their projects in computer vision.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g5i1, 'The department joined the AI bootcamp organised by Mastercard where faculty and students explored the future applications of AI in retail and banking. The visit also discussed the sponsorship of Mastercard for the department''s events, such as ICAMAC, the Engineering Competition, and the capstone projects.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g5i5, 'Industry stakeholders engaged in the capstones by funding and sponsoring three EECS projects. These included Neuromind, DFF, and LifeCircum.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g5i4, 'The department supported the "Student for One Day" event, which hosted a series of workshops in AI and robotics delivered to UAE high school students.', 1, 1, 5, NOW(), NOW());

    -- =========================================================
    -- GOAL 6: Develop sustained effort to increase student enrolment
    -- =========================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Develop a sustained effort and organization to increase student enrolment especially in the master programs.', 6, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Develop a sustained effort and organization to increase student enrolment especially in the master programs.', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id)
    VALUES (v_obj_id, 16);

    -- Initiative 1
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Expand RIT Dubai degree offerings in ICT by adding new degrees, minors/options, dual BS/MS degrees, and micromasters', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 14);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Expand RIT Dubai degree offerings in ICT by adding new degrees, minors/options, dual BS/MS degrees, and micromasters', 1)
    RETURNING id INTO v_meas_g6i1;

    -- Initiative 2
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Reach out to key partners and government entities to sponsor elite students', 2, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 20);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Reach out to key partners and government entities to sponsor elite students', 1)
    RETURNING id INTO v_meas_g6i2;

    -- Initiative 3
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Offer GRAs to qualified students', 3, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 7);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Offer GRAs to qualified students', 1)
    RETURNING id INTO v_meas_g6i3;

    -- Initiative 4
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'List the MSCEC program besides MSEE with TRA/ICTFund for scholarship sponsorship', 4, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 20);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'List the MSCEC program besides MSEE with TRA/ICTFund for scholarship sponsorship', 1)
    RETURNING id INTO v_meas_g6i4;

    -- Initiative 5
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Leverage the annual competitions to reach out to new applicants', 5, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 16);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Leverage the annual competitions to reach out to new applicants', 1)
    RETURNING id INTO v_meas_g6i5;

    -- Initiative 6
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Support the recruitment and marketing teams with their efforts and outreach', 6, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 16);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Support the recruitment and marketing teams with their efforts and outreach', 1)
    RETURNING id INTO v_meas_g6i6;

    -- Initiative 7
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Work with student recruitment agents on outreach activities and webinar/seminar series and training', 7, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 30);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Work with student recruitment agents on outreach activities and webinar/seminar series and training', 1)
    RETURNING id INTO v_meas_g6i7;

    -- Initiative 8
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Create a system of support for undergraduate recruitment events including school visits and open days through the department student clubs', 8, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 16);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Create a system of support for undergraduate recruitment events including school visits and open days through the department student clubs', 1)
    RETURNING id INTO v_meas_g6i8;

    -- Initiative 9
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Leverage the research and training activities in the advanced labs to market the EE and computing programs and engage more with the community', 9, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 26);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Leverage the research and training activities in the advanced labs to market the EE and computing programs and engage more with the community', 1)
    RETURNING id INTO v_meas_g6i9;

    -- Initiative 10
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Improve the content presentation of EECS programs on the RIT website by creating a single home page that integrates all contents and webpages for all advanced labs', 10, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 19);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Improve the content presentation of EECS programs on the RIT website by creating a single home page that integrates all contents and webpages for all advanced labs', 1)
    RETURNING id INTO v_meas_g6i10;

    -- Initiative 11
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Publish a bi-yearly newsletter to highlight the research activities of faculty and students, the activities in the advanced labs, updates to majors, minors, and courses', 11, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 19);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Publish a bi-yearly newsletter to highlight the research activities of faculty and students, the activities in the advanced labs, updates to majors, minors, and courses', 1)
    RETURNING id INTO v_meas_g6i11;

    -- Period 4 achievements for Goal 6
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g6i8, 'A support system has been put in place since Fall 2023 to allow EECS faculty participate in open days, school visits to RIT, and workshops where each activity is represented by EE and computing faculty.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g6i2, 'Worked out an agreement with Emirates Airlines to sponsor 5 students in CIT.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g6i2, 'Dubai Police sponsored 7 students in MS CSEC.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g6i3, 'MSEE and MSCSEC have each two GTAs. The plan to keep at least a total of four GTA positions.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g6i4, 'Both MSCEC and MSEE are listed with the TRA/ICTFund for scholarship sponsorship.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g6i5, 'Emirates robotics competition (for university students) was successfully launched in Feb. 2023; Will be repeated in March 2024 and should become an annual flagship event which is being done in collaboration with KU and DFL.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g6i5, 'The smart energy competition (for high school students) was launched in March 2023 (solar car racing); Will be repeated in March 2024 and should become an annual flagship event.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g6i5, 'The CPTC competition (for university students) was launched in October 2019 where RIT Dubai has served as the regional host and has since become an annual flagship event for both Dubai and NY campuses.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g6i5, 'The data challenge was organized in collaboration with ZainTech. First edition was done in Fall 2023. Second edition is planned in Fall 2024 including industry and government collaboration.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g6i5, 'The annual engineering and computing competition has been running for 13 years and has involved hundreds of schools and organized in collaboration with Emerson.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g6i5, 'EE and computing students have been participating in the annual IEEE xtreme (24 hours of programming) and have won the 1st place in some years.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g6i5, 'EE and computing students have been participating in the annual IEEE student day using their capstone projects. Also our faculty have served as judges.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g6i9, 'Imagine RIT (three projects have been submitted in 2024).', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g6i10, 'The webpages for all advanced labs have been created. Next is to do the integration.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g6i5, 'RIT team students won first and third place in Bybit CryptoAI literacy and security competition held in AUS 5th Nov, 2023.', 1, 1, 4, NOW(), NOW());

    -- Period 5 achievements for Goal 6
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g6i1, 'The dual BS/MS degree in EE and CSEC has received approval from New York, and a proposal has been submitted to the Ministry of Education (MOE) for accreditation.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g6i1, 'Plans are underway to introduce new academic programs to the department, including a BS in Computer Engineering, an MS in Artificial Intelligence, and either a BS or Minor in Game Design and Development.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g6i1, 'The proposal for an Advanced Certificate in Health Informatics has been submitted to the MOE to establish this microcredential, which will be delivered in collaboration with MBRU.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g6i3, 'A GTA for the computing programs has been selected from the MOE''s elite list, with full coverage for the Master''s in Cybersecurity.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g6i2, 'An MOU has been signed with the Lebanese American University (LAU), offering their students reduced tuition fees for Master''s programs.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g6i2, 'A campaign has been launched to attract strategic partners by offering discounted tuition for those enrolling in Master''s programs.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g6i7, 'Two online information sessions were conducted for the MSEE and MSCSEC programs, aimed at recruiting graduate students.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g6i7, 'Two tailored info sessions were organized to specifically engage Women in AI and the Society of Engineers members.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g6i9, 'EE graduate seminars/webinars and the College webinar series have been used to introduce the community to the themes and strengths of our graduate programs.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g6i8, 'The AI/Robotics student club hosted multiple workshops for high school students, focusing on AI and robotics to boost engagement and interest in our programs.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g6i8, 'A customized two-day digital design workshop was delivered to 200 Year 12 students from DIA, covering 3D modeling, 3D printing, robotics, and AI.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g6i10, 'Content for all advanced labs has been enhanced and consolidated into a single webpage to better showcase the department''s capabilities.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g6i10, 'Research group profiles have been standardized in structure and branding, and centralized on one homepage.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g6i10, 'Student club content has also been unified in format and branding, and placed on a dedicated homepage.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g6i11, 'Short promotional videos highlighting our unique course offerings have been produced and are now featured on department screens and social media platforms.', 1, 1, 5, NOW(), NOW());

    -- =========================================================
    -- GOAL 7: Student success, retention and satisfaction
    -- =========================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Student success, retention and satisfaction can be further improved through new initiatives and engagements.', 7, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Student success, retention and satisfaction can be further improved through new initiatives and engagements.', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id)
    VALUES (v_obj_id, 3);

    -- Initiative 1
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Maintain attrition to below 10%', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 5);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Maintain attrition to below 10%', 1)
    RETURNING id INTO v_meas_g7i1;

    -- Initiative 2
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Promote efficient student advising and better engagements', 2, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 5);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Promote efficient student advising and better engagements', 1)
    RETURNING id INTO v_meas_g7i2;

    -- Initiative 3
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Diversify elective offerings', 3, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 13);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Diversify elective offerings', 1)
    RETURNING id INTO v_meas_g7i3;

    -- Initiative 4
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Help create new co-op opportunities and support appropriate co-op alternatives', 4, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 3);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Help create new co-op opportunities and support appropriate co-op alternatives', 1)
    RETURNING id INTO v_meas_g7i4;

    -- Initiative 5
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Offer students more co-curricular activities through clubs and advanced labs', 5, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 6);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Offer students more co-curricular activities through clubs and advanced labs', 1)
    RETURNING id INTO v_meas_g7i5;

    -- Initiative 6
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Sustain the TA support system', 6, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 22);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Sustain the TA support system', 1)
    RETURNING id INTO v_meas_g7i6;

    -- Initiative 7
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Recognize student achievements', 7, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 6);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Recognize student achievements', 1)
    RETURNING id INTO v_meas_g7i7;

    -- Initiative 8
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Add more qualified faculty with distinct expertise', 8, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 2);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Add more qualified faculty with distinct expertise', 1)
    RETURNING id INTO v_meas_g7i8;

    -- Initiative 9
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Reduce DFW rates in key courses such as CSEC-140, GCIS-123, GCIS-124, EEEE-120 and EEEE-281 through improved teaching methodologies, coordination, TA support and recitations', 9, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 22);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Reduce DFW rates in key courses such as CSEC-140, GCIS-123, GCIS-124, EEEE-120 and EEEE-281 through improved teaching methodologies, coordination, TA support and recitations', 1)
    RETURNING id INTO v_meas_g7i9;

    -- Initiative 10
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Advocate by example teaching rigor and effective learning environment', 10, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 12);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Advocate by example teaching rigor and effective learning environment', 1)
    RETURNING id INTO v_meas_g7i10;

    -- Initiative 11
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Make sure students have the necessary labs and computing resources', 11, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 11);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Make sure students have the necessary labs and computing resources', 1)
    RETURNING id INTO v_meas_g7i11;

    -- Period 4 achievements for Goal 7
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g7i1, 'Attrition rate averaged over all EECS programs is at 9% in 2022-23.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g7i4, 'Created X new industry co-op placement through the Cyber-forward program in collaboration with DIFC and Mastercard.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g7i4, 'EECS supported Y research co-op placements since Fall 2022 including internal co-op where computing students developed automation for some of the RIT processes.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g7i11, 'An ad-hoc committee was established in Fall 2023 to relocate virtual computing lab access from the main campus to the DT lab to address the slow access of the virtual machines.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g7i3, 'Since Fall 2022, one elective has been added to both the BSEE & MSEE degrees in addition to a new course in advanced programming added to the core BSEE.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g7i3, 'Since Fall 2022, three new electives have been added to the computing programs.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g7i11, 'Requested a budget of 528,000 Dhs in 2024-25 to add labs and high-end computers to support the EE and computing programs, of which 355,500 was approved.', 1, 1, 4, NOW(), NOW());

    -- Period 5 achievements for Goal 7
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g7i7, 'Six student teams presented their projects at the AI Conference hosted by GDRFA during AI Week.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g7i7, 'A team of students in Electrical Engineering secured first place in the 12th Undergraduate Research and Innovation Competition 2025 organized by Abu Dhabi University (Spring 2025).', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g7i5, 'Two EE teams participated in the Dubai Health Bootcamp, collaborating with peers from universities across the UAE.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g7i5, 'Two EE teams progressed to the final round of the Emirates Robotics Competition.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g7i7, 'At the IROS Conference held at KU Abu Dhabi, two teams achieved podium finishes—one securing first place and the other third.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g7i7, 'At the IEEE student day, one team of students secured second position in software engineering category while another team secured the third position in the common design category.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g7i5, 'The IEEE RAS student branch was officially established under the AI/Robotics student club.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g7i5, 'A team of students from the AI/Robotics student club received a fund of $10,000 to build a social robot in collaboration with NY.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g7i11, 'Full maintenance has been completed for all laboratory equipment.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g7i9, 'Courses with high DFW rates have been thoroughly reviewed using peer evaluation forms to identify and implement improved teaching strategies.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g7i7, 'Undergraduate and graduate students have contributed publications to Q1/Q2 journals and presented at national and international conferences, as part of research coop, capstone, thesis/graduate paper.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g7i10, 'The SAIV system has been rigorously enforced to minimize instances of plagiarism.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g7i10, 'The lockdown browser has been tested in an effort to start implementing it with online assessments.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g7i10, 'AI usage policies have been embedded into course structures to ensure responsible integration of AI tools in assessments.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g7i8, 'A new faculty member with a PhD from Virginia Tech and a robust academic and industry background has joined to teach networking courses.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g7i2, 'Graduate students have been given expanded access to a broader range of courses through enrollment in NY online offerings.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g7i3, 'New elective courses are planned for launch, including Introduction to AI (EEEE-447) and Digital Twin as a special topic (EEEE-489).', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g7i4, 'An agreement has been formalised with the EC Council and received approval to administer certification exams to RIT CSEC students directly on campus starting Fall 2025.', 1, 1, 5, NOW(), NOW());

    -- =========================================================
    -- GOAL 8: Secure engineering accreditations with ABET
    -- =========================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Secure engineering accreditations with ABET and engineering and computing accreditations and substantive changes with the ministry.', 8, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Secure engineering accreditations with ABET and engineering and computing accreditations and substantive changes with the ministry.', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id)
    VALUES (v_obj_id, 4);

    -- Initiative 1
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Secure the BSEE accreditation with ABET', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 8);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Secure the BSEE accreditation with ABET', 1)
    RETURNING id INTO v_meas_g8i1;

    -- Initiative 2
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Secure the BSEE accreditation with the ministry', 2, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 8);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Secure the BSEE accreditation with the ministry', 1)
    RETURNING id INTO v_meas_g8i2;

    -- Initiative 3
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Secure the MSEE accreditation with the ministry', 3, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 8);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Secure the MSEE accreditation with the ministry', 1)
    RETURNING id INTO v_meas_g8i3;

    -- Initiative 4
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Secure the CIT accreditation with the ministry', 4, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 8);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Secure the CIT accreditation with the ministry', 1)
    RETURNING id INTO v_meas_g8i4;

    -- Initiative 5
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Secure the approval of substantive changes for the BS CSEC with the ministry', 5, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 8);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Secure the approval of substantive changes for the BS CSEC with the ministry', 1)
    RETURNING id INTO v_meas_g8i5;

    -- Initiative 6
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Secure the approval of substantive change for the MS CSEC with the ministry', 6, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 8);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Secure the approval of substantive change for the MS CSEC with the ministry', 1)
    RETURNING id INTO v_meas_g8i6;

    -- Initiative 7
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Secure the BS CSEC accreditation with the ministry', 7, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 8);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Secure the BS CSEC accreditation with the ministry', 1)
    RETURNING id INTO v_meas_g8i7;

    -- Initiative 8
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Secure the MS CSEC accreditation with the ministry', 8, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 8);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Secure the MS CSEC accreditation with the ministry', 1)
    RETURNING id INTO v_meas_g8i8;

    -- Period 4 achievements for Goal 8
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g8i1, 'The BSEE program accreditation has been renewed by ABET till Sept. 30, 2028.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g8i2, 'The BSEE program accreditation has been renewed by the ministry till Dec. 18, 2028.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g8i3, 'The MSEE program accreditation has been renewed by the ministry till Dec. 2028.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g8i4, 'The CIT program accreditation has been renewed by the ministry till Fall 2024.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g8i5, 'The BS CSEC substantive change has been submitted on Jan. 29, 2024.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g8i6, 'The MSCSEC substantive change has been submitted on Jan. 29, 2024.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g8i7, 'The BS CSEC program accreditation has been renewed by the ministry till Fall 2024.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g8i8, 'The MS CSEC program accreditation has been renewed by the ministry till Fall 2024.', 1, 1, 4, NOW(), NOW());

    -- Period 5 achievements for Goal 8
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g8i1, 'The BSEE program accreditation has been renewed by ABET till Sept. 30, 2028.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g8i2, 'The BSEE program accreditation has been renewed by the ministry till Dec. 18, 2028.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g8i3, 'The MSEE program accreditation has been renewed by the ministry till Dec. 2028.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g8i4, 'The CIT program accreditation has been renewed by the ministry till Fall 2029.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g8i5, 'The BS CSEC substantive change has been submitted on Jan. 29, 2024.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g8i6, 'The MSCSEC substantive change has been submitted on Jan. 29, 2024.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g8i7, 'The BS CSEC program accreditation has been renewed by the ministry till Fall 2029.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g8i8, 'The MS CSEC program accreditation has been renewed by the ministry till Fall 2029.', 1, 1, 5, NOW(), NOW());

    -- =========================================================
    -- GOAL 9: Expand RIT Dubai degree offerings in ICT
    -- =========================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id, 'Expand RIT Dubai degree offerings in ICT by adding relevant new degrees, minors/options, BS/MS dual degrees, and micromasters', 9, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id, 'Expand RIT Dubai degree offerings in ICT by adding relevant new degrees, minors/options, BS/MS dual degrees, and micromasters', 1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id)
    VALUES (v_obj_id, 7);

    -- Initiative 1
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Add one degree in the ICT domain within the next 2 years (Computer Engineering, Computer Science, or MS in Information Technology and Analytics)', 1, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 14);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Add one degree in the ICT domain within the next 2 years (Computer Engineering, Computer Science, or MS in Information Technology and Analytics)', 1)
    RETURNING id INTO v_meas_g9i1;

    -- Initiative 2
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Add one to two micro master programs in energy and cybersecurity', 2, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 18);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Add one to two micro master programs in energy and cybersecurity', 1)
    RETURNING id INTO v_meas_g9i2;

    -- Initiative 3
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Add the BS/MS dual degree in EE', 3, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 14);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Add the BS/MS dual degree in EE', 1)
    RETURNING id INTO v_meas_g9i3;

    -- Initiative 4
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id, 'Add the BS/MS dual degree in cybersecurity', 4, 1)
    RETURNING id INTO v_init_id;
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_init_id, 14);
    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id, 'Add the BS/MS dual degree in cybersecurity', 1)
    RETURNING id INTO v_meas_g9i4;

    -- Period 4 achievements for Goal 9
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g9i1, 'SE minor has been added since Fall 2022.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g9i1, 'The AI degree option has been added since Fall 2023.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g9i1, 'The MSEE degree was redesigned with focus areas in AI/Robotics and smart energy since Fall 2023 to support the demand of our students.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g9i2, 'At least one micromaster application will be prepared by the end of 2023-24 academic year.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g9i3, 'At least one BS/MS dual degree will be prepared by the end of 2023-24 academic year.', 1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g9i1, 'Planning a course in digital twins.', 1, 1, 4, NOW(), NOW());

    -- Period 5 achievements for Goal 9
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g9i1, 'The BS in Computer Engineering has been planned to be added to the department''s programs.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g9i1, 'The MS in AI degree in collaboration with NY (50% online; 50% in person) carries a lot of promise and has been planned to be added.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g9i1, 'A minor in Game Design and Development has been considered given the growth we have in the computing programs and the addition of the SE minor.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g9i2, 'The advanced certificate in Health Informatics has been submitted to the MOE for accreditation.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g9i3, 'The BS/MS dual degree in EE has been approved by NY and submitted to the MOE for accreditation.', 1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_g9i4, 'The BS/MS dual degree in Cybersecurity has been approved by NY and submitted to the MOE for accreditation.', 1, 1, 5, NOW(), NOW());

    RAISE NOTICE 'V29: EE strategy 4 re-seeded.';
END $$;
