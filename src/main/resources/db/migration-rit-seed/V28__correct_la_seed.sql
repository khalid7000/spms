-- V28: Re-seed LA (Liberal Arts, strategy 13) with correct structure.
DO $$
DECLARE
    v_strategy_id BIGINT := 13;
    v_goal_id     BIGINT;
    v_obj_id      BIGINT;
    v_init_id     BIGINT;
    v_meas_id     BIGINT;
BEGIN
    DELETE FROM goal WHERE strategy_id = v_strategy_id;

    -- =========================================================
    -- GOAL 1: Introduce successful programs within the LA department
    -- =========================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id,
            'Introduce successful programs within the LA department that reflect the market needs in the LA space (incoming students and potential employer needs).',
            1, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id,
            'Introduce successful programs within the LA department that reflect the market needs in the LA space (incoming students and potential employer needs).',
            1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id)
    VALUES (v_obj_id, 7);

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id,
            'Successfully introduce the BS in Advertising and PR by Fall 2026 as the 2nd program within the department and 2nd LA program at RIT Dubai (pending Ministry approval) and achieve consistent student enrollment.',
            1, 1)
    RETURNING id INTO v_init_id;

    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_init_id, 14);

    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id,
            'Successfully introduce the BS in Advertising and PR by Fall 2026 as the 2nd program within the department and 2nd LA program at RIT Dubai (pending Ministry approval) and achieve consistent student enrollment.',
            1)
    RETURNING id INTO v_meas_id;

    -- Achievements: period 4 (2022-2024) COMPLETED
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Adapt the curriculum to include case studies and projects relevant to the Middle Eastern market.',
            1, 1, 4, NOW(), NOW());

    -- Achievements: period 5 (2024-2025) COMPLETED
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Successfully received approval from NY to run the APR Immersion, this will serve as a proof of concept for the program.',
            1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Rolled out four program courses for the BS in APR in 2024-2025 as part of the APR Immersion and as a proof of concept for the program.',
            1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Collaborated with NY to make curricular modifications to the BS in APR to include more production elements.',
            1, 1, 5, NOW(), NOW());

    -- Achievements: period 6 (2025-2026) COMPLETED
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Documentation submitted to Ministry in Fall 2025 for IPA of BS in APR.',
            1, 1, 6, NOW(), NOW());

    -- =========================================================
    -- GOAL 2: Introduce more GenEd LA course offerings
    -- =========================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id,
            'Introduce more GenEd LA course offerings while providing a wide variety of perspectives.',
            2, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id,
            'Introduce more GenEd LA course offerings while providing a wide variety of perspectives.',
            1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id)
    VALUES (v_obj_id, 7);

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id,
            'Continue to diversify the LA GenEd electives by offering more courses in Modern Languages as well as more offerings in Anthropology, Economics, Philosophy, and Sociology. Explore possibilities of courses in hybrid format.',
            1, 1)
    RETURNING id INTO v_init_id;

    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_init_id, 13);

    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id,
            'Continue to diversify the LA GenEd electives by offering more courses in Modern Languages as well as more offerings in Anthropology, Economics, Philosophy, and Sociology. Explore possibilities of courses in hybrid format.',
            1)
    RETURNING id INTO v_meas_id;

    -- Achievements: period 4 (2022-2024) COMPLETED
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Received approval by RIT NY to teach the last required course for Ethics Minor (PHIL 415 Ethical Theory).',
            1, 1, 4, NOW(), NOW());

    -- Achievements: period 5 (2024-2025) COMPLETED
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Opened a new course offering in Linguistic Anthropology (Fall 2024).',
            1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Opened 4 new courses in Advertising and PR in the 2024-2025 AY.',
            1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Opened a new course in Ethics (PHIL 415 Ethical Theory).',
            1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Opened Spanish II in Spring 2025 along with Great Authors (ENGL418) to complete the Spanish Immersion.',
            1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Plans in place for many new offerings in the 2025-2026 AY: Sociology, Linguistics, 2 upper-level Economics courses, Intermediate French I (each semester), ANTH301 (Social and Cultural Theory), Positive Psychology.',
            1, 1, 5, NOW(), NOW());

    -- Achievements: period 6 (2025-2026) COMPLETED
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'New GenEd course offerings: Positive Psychology (Jihane).',
            1, 1, 6, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'New GenEd course offerings: Sociology of Work SOCI 230, and Social/Cultural Theory SOCI/ANTH 301 in Fall (Rayya); Social Inequality SOCI 225 in Spring (Rayya).',
            1, 1, 6, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'New GenEd course offerings: Economics: Game Theory in Fall 2025 (Shama).',
            1, 1, 6, NOW(), NOW());

    -- =========================================================
    -- GOAL 3: Acquire more minors/immersions
    -- =========================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id,
            'Acquire more minors/immersions to allow more GenEd LA variety.',
            3, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id,
            'Acquire more minors/immersions to allow more GenEd LA variety.',
            1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id)
    VALUES (v_obj_id, 7);

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id,
            'Such offerings should be in support of acquiring and or fulfilling relevant immersions and minors, including Ethics Minor, Philosophy Minor, APR Immersion, Spanish Immersion, Sociology Immersion, Language Science Immersion, and Economics Minor.',
            1, 1)
    RETURNING id INTO v_init_id;

    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_init_id, 13);

    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id,
            'Such offerings should be in support of acquiring and or fulfilling relevant immersions and minors, including Ethics Minor, Philosophy Minor, APR Immersion, Spanish Immersion, Sociology Immersion, Language Science Immersion, and Economics Minor.',
            1)
    RETURNING id INTO v_meas_id;

    -- Achievements: period 4 (2022-2024) COMPLETED
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Received official approval from the NY Campus to offer the Ethics Minor and the Philosophy Minor at RIT Dubai starting in Fall 2025.',
            1, 1, 4, NOW(), NOW());

    -- Achievements: period 5 (2024-2025) COMPLETED
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Opened all courses required for the Immersion in Advertising and PR in the 2024-2025 AY.',
            1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Offering the Spanish Immersion in Spring 2025 given the new course offering of Spanish II and Great Authors.',
            1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Ethics & Philosophy Minor approved starting in Fall 2024 semester. First graduates with Ethics minor anticipated for SP 25.',
            1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Introducing new Immersions and Minors in the 25-26 AY: Sociology Immersion, Language Science Immersion, and Economics Minor.',
            1, 1, 5, NOW(), NOW());

    -- Achievements: period 6 (2025-2026) COMPLETED
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'A new Sociology Immersion is created. Three new Sociology courses will be introduced in 2025-2026 (Rayya).',
            1, 1, 6, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Economics minor to be offered with the introduction of two new courses.',
            1, 1, 6, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Language Science Immersion to be delivered in 25-26.',
            1, 1, 6, NOW(), NOW());

    -- =========================================================
    -- GOAL 4: Produce graduates in LA that are highly employable
    -- =========================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id,
            'Produce graduates in LA that are highly employable.',
            4, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id,
            'Produce graduates in LA that are highly employable.',
            1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id)
    VALUES (v_obj_id, 2);

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id,
            'Establish stronger industry relationships in key areas of Psychology and Advertising/PR through connections such as our LA Advisory Board and faculty contacts/professional societies in order to increase co-op opportunities for students.',
            1, 1)
    RETURNING id INTO v_init_id;

    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_init_id, 3);

    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id,
            'Establish stronger industry relationships in key areas of Psychology and Advertising/PR through connections such as our LA Advisory Board and faculty contacts/professional societies in order to increase co-op opportunities for students.',
            1)
    RETURNING id INTO v_meas_id;

    -- Achievements: period 4 (2022-2024) COMPLETED
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'RIT Dubai received approval for extending membership in Psi Chi (Psych Professional Organization) from the NY Chapter to Dubai students.',
            1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Invite industry experts for guest lectures.',
            1, 1, 4, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Encourage faculty members to attend conferences and seminars to build and expand their networks.',
            1, 1, 4, NOW(), NOW());

    -- Achievements: period 5 (2024-2025) COMPLETED
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Invited two Public Relations Professionals (from Dubai region) to speak to all 3 sections of the COMM 212 PR course.',
            1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Invited guest speaker Dr Christine Abi Assi who is now in contact with the coop department as well as 2 other clinics to establish agreements for student opportunities. Collaborated with the Graphic Design Club to organize a Media Kit competition for "Blood Donation Day".',
            1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Invited guest speaker who specialises in Middle Eastern Literature and Publishing; invited guest speakers and industry experts for health psychology and junior seminar — overall, 10 speakers were invited from organizations such as Emirates Speciality Hospital, Fakeeh University Hospital, Maharat Learning Centre, OpenMinds Centre Dubai, etc.',
            1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Working with Dubai-based client "The Giving Movement" for COMM 322 (Campaign Management & Planning), allowing students a project-based approach producing advertising & PR materials through the Spring Semester.',
            1, 1, 5, NOW(), NOW());

    -- Achievements: period 6 (2025-2026) COMPLETED
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Created an opportunity for Psychology related social media content creation co-op. First student enrolled in Summer 2025, will be rolling more in Summer 2026 (Jihane).',
            1, 1, 6, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Created 2 Research Co-op opportunities, engaged 3 students over the summer 2025, will be continuing in 2026 (Rupa).',
            1, 1, 6, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Created 3 Research Co-op opportunities, and worked with 2 other students on research papers over summer 2025; both papers are currently under review (Niki).',
            1, 1, 6, NOW(), NOW());

    -- =========================================================
    -- GOAL 5: Increase student retention through targeted initiatives
    -- =========================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id,
            'Increase student retention through targeted initiatives for at-risk students.',
            5, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id,
            'Increase student retention through targeted initiatives for at-risk students.',
            1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id)
    VALUES (v_obj_id, 3);

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id,
            'Continue to work with the ASC to offer workshops and series in support of student success through targeting specific need areas within LA — particularly those that relate to writing, research and critical thinking, as a part of ASC or as an extension of our Writing Center.',
            1, 1)
    RETURNING id INTO v_init_id;

    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_init_id, 5);

    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id,
            'Continue to work with the ASC to offer workshops and series in support of student success through targeting specific need areas within LA — particularly those that relate to writing, research and critical thinking, as a part of ASC or as an extension of our Writing Center.',
            1)
    RETURNING id INTO v_meas_id;

    -- Achievements: period 4 (2022-2024) COMPLETED
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Conducted specific workshops by means of the Writing Center to help students in typical challenge areas of writing and research.',
            1, 1, 4, NOW(), NOW());

    -- Achievements: period 5 (2024-2025) COMPLETED
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Writing Center Workshop: Synthesizing sources (Leen). Invited Mr. Dulmin as a guest speaker to ELCA classes on improving basic language skills through the use of monolingual dictionaries.',
            1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'ELCA Writing Circle established (Jessica, Mirosh).',
            1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Introduction of the self-reflective assessment form across foreign language classes designed for students who come to office hours and have trouble specifying their knowledge gaps (Maria).',
            1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Worked with ASC tutors in Fall 2024 to provide additional support to students in Economics. Tutors collaborated on topics and areas of revision prior to study sessions (Shama).',
            1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Invited Faryal from the ASC to the Spring Retreat to join faculty groups (per discipline) in discussing positive pedagogical changes to enhance student success.',
            1, 1, 5, NOW(), NOW());

    -- Achievements: period 6 (2025-2026) COMPLETED
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Developing at-risk registers for writing students and collaborating with their instructors to optimize student outcomes (Jessica).',
            1, 1, 6, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Developing a Writing Center website to facilitate appointment scheduling and provide access to key resources (Jessica).',
            1, 1, 6, NOW(), NOW());

    -- =========================================================
    -- GOAL 6: Increase opportunities for faculty research
    -- =========================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id,
            'Increase opportunities for faculty research within departmental means.',
            6, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id,
            'Increase opportunities for faculty research within departmental means.',
            1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id)
    VALUES (v_obj_id, 5);

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id,
            'Work with LA faculty to propose unique opportunities for increased research output and collaboration.',
            1, 1)
    RETURNING id INTO v_init_id;

    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_init_id, 7);

    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id,
            'Work with LA faculty to propose unique opportunities for increased research output and collaboration.',
            1)
    RETURNING id INTO v_meas_id;

    -- No COMPLETED achievements in period 4 for this goal
    -- Achievements: period 5 (2024-2025) COMPLETED
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'OSF Pre-registration for VR systematic review completed (Niki).',
            1, 1, 5, NOW(), NOW());

    -- Achievements: period 6 (2025-2026) COMPLETED
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Submitted and received 2 ARC grants for research (Rupa).',
            1, 1, 6, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Submitted 2 ARC grants for research and submitted a grant application to the Sheikh Hamdan Bin Rashid Al Maktoum Health Research Grant (Niki).',
            1, 1, 6, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Two papers currently under review done in collaboration with Psyc Faculty (Niki) and two papers currently under review done with Psyc students (Niki). Two papers published (Soumya).',
            1, 1, 6, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Submitted ARC and Seed grant by Dec 2025-Feb 2026 (Jihane).',
            1, 1, 6, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'One systematic review published (Jihane, Rupa and Niki).',
            1, 1, 6, NOW(), NOW());

    -- =========================================================
    -- GOAL 7: Engage with potential incoming HS students
    -- =========================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id,
            'Engage with potential incoming HS students in promotion of the offered LA programs and as a service to the community.',
            7, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id,
            'Engage with potential incoming HS students in promotion of the offered LA programs and as a service to the community.',
            1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id)
    VALUES (v_obj_id, 16);

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id,
            'Start an annual event in Psychology and Advertising and PR (if applicable) to promote such programs while engaging all LA faculty as key players within the course offerings of the degree program(s).',
            1, 1)
    RETURNING id INTO v_init_id;

    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_init_id, 17);

    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id,
            'Start an annual event in Psychology and Advertising and PR (if applicable) to promote such programs while engaging all LA faculty as key players within the course offerings of the degree program(s).',
            1)
    RETURNING id INTO v_meas_id;

    -- No COMPLETED achievements in period 4 for this goal
    -- Achievements: period 5 (2024-2025) COMPLETED
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Behavioral Health Symposium (Rupa).',
            1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Psychology Day to include taster in various workshops (e.g. mindfulness practice, "working together" poster, and EdTalk where students share their research on stage in "TedTalk" format), leading up to an expert presentation open to the community (Maaham).',
            1, 1, 5, NOW(), NOW());

    -- No COMPLETED achievements in period 6 for this goal (R8 Col C is empty)

    -- =========================================================
    -- GOAL 8: Engage with local high schools to bridge K-12 gap
    -- =========================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id,
            'Engage with local high schools to help bridge the gap between K-12 and university.',
            8, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id,
            'Engage with local high schools to help bridge the gap between K-12 and university.',
            1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id)
    VALUES (v_obj_id, 16);

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id,
            'Involve all LA faculty in the mission of bridging the gap between high school and university by creating a series of talks to be delivered to high school students and/or teachers as they relate to our various disciplines.',
            1, 1)
    RETURNING id INTO v_init_id;

    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_init_id, 17);

    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id,
            'Involve all LA faculty in the mission of bridging the gap between high school and university by creating a series of talks to be delivered to high school students and/or teachers as they relate to our various disciplines.',
            1)
    RETURNING id INTO v_meas_id;

    -- No COMPLETED achievements in period 4 for this goal
    -- Achievements: period 5 (2024-2025) COMPLETED
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Participated in a school career fair in Sharjah. RIT participated in their career fair for high school students with a footfall of 1000 students.',
            1, 1, 5, NOW(), NOW());

    -- No COMPLETED achievements in period 6 for this goal (Col C is empty for R9 in 2025-2026 COMPLETED section)

    -- =========================================================
    -- GOAL 9: Provide diverse opportunities for professional development
    -- =========================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id,
            'Provide diverse opportunities for professional development (in teaching and in research) within the department.',
            9, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id,
            'Provide diverse opportunities for professional development (in teaching and in research) within the department.',
            1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id)
    VALUES (v_obj_id, 17);

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id,
            'Leverage the existing talent within the LA Department to carry out an internal professional development/best practices series where faculty showcase their unique areas of strength in a way that all department members can benefit.',
            1, 1)
    RETURNING id INTO v_init_id;

    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_init_id, 4);

    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id,
            'Leverage the existing talent within the LA Department to carry out an internal professional development/best practices series where faculty showcase their unique areas of strength in a way that all department members can benefit.',
            1)
    RETURNING id INTO v_meas_id;

    -- Achievements: period 4 (2022-2024) COMPLETED
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Best practices in teaching: integrating technology in teaching; effective assessment strategies; constructive feedback; student centered learning approach; student engagement and motivation (Mirosh).',
            1, 1, 4, NOW(), NOW());

    -- Achievements: period 5 (2024-2025) COMPLETED
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            '"Managing stress at work" workshop delivered by psychology faculty to HR group (Jihane, Niki, Rupa). Workshop for student athletes on the mental well-being of athletes (Rupa). Highlighting CITI training for faculty members (Rupa).',
            1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Workshop on AI Integration in the classroom in Spring 2025 (Maria).',
            1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Internal professional development series during UWRT meetings: two sessions completed — one on APA referencing using Microsoft Word by Panteha, and another on using the interactive technology Padlet by Dulmin (Mirosh and UWRT faculty).',
            1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'FDC Seminar series 2024-25: Applications of GenAI in Education (Maria).',
            1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Integration of AI across PSYC mapping completed in June 2025 (Jihane and Sonakshi).',
            1, 1, 5, NOW(), NOW());

    -- Achievements: period 6 (2025-2026) COMPLETED
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Creation of an AI PDP for the LA Department anchored in behavioral theories on technological adoption, along with an implementation plan for 2025-2026 (Jillian and Maria).',
            1, 1, 6, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Publication of a comprehensive AI Integration Teaching and Assessment guide that combines theoretical foundations with practical applications tailored to various LA disciplines and courses (Khalid and Maria).',
            1, 1, 6, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Conducting 33 individual meetings with faculty to provide additional support and opportunities for AI integration, during which data was collected, analyzed, and used to customize the AI PDP for the LA department (Jillian and Maria).',
            1, 1, 6, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Session on AI anxiety (Rupa).',
            1, 1, 6, NOW(), NOW());

    -- =========================================================
    -- GOAL 10: Peer-to-peer PD within the LA Department
    -- =========================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id,
            'Create an opportunity for peer-to-peer professional development/support & mentorship/exchange of ideas within the LA Department at RIT Dubai.',
            10, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id,
            'Create an opportunity for peer-to-peer professional development/support & mentorship/exchange of ideas within the LA Department at RIT Dubai.',
            1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id)
    VALUES (v_obj_id, 17);

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id,
            'Continue to promote Peer Review of Educational Practice (PREP) so as to create a community of practice within the department. This will become mandatory for all new LA faculty and highly encouraged among existing LA faculty.',
            1, 1)
    RETURNING id INTO v_init_id;

    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_init_id, 4);

    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id,
            'Continue to promote Peer Review of Educational Practice (PREP) so as to create a community of practice within the department. This will become mandatory for all new LA faculty and highly encouraged among existing LA faculty.',
            1)
    RETURNING id INTO v_meas_id;

    -- Achievements: period 4 (2022-2024) COMPLETED
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Launched a Liberal Arts Department newsletter for the entire student and faculty body by means of the LA Club (Graphic Design Club) to highlight faculty and student achievements (Completed in Feb 2024).',
            1, 1, 4, NOW(), NOW());

    -- Achievements: period 5 (2024-2025) COMPLETED
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'PREP sessions completed in Fall 2024 for instructor feedback; many were reciprocated with the reviewee then observing the reviewer.',
            1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Created two WhatsApp groups for faculty teaching UWRT 100 and UWRT 150 to regularly discuss current lessons, assignment plans, and necessary updates as part of updating pedagogy and assessments.',
            1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            '8 PREPs completed across PSYC department (Psyc department).',
            1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'RIT Global Behavioral Health Symposium scheduled for April 9 (Rupa).',
            1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Creation of a space connecting Foreign Language instructors to exchange pedagogical practices and enhance modern language teaching (Jillian, Nasih, Maria).',
            1, 1, 5, NOW(), NOW());

    -- Achievements: period 6 (2025-2026) COMPLETED
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Create ANTH PREP Group that includes ANTH instructors: Yahya, Rayya, Reem, Juwaeriah (Initiated by Razem - created by Yahya).',
            1, 1, 6, NOW(), NOW());

    -- =========================================================
    -- GOAL 11: Peer-to-peer PD across campuses
    -- =========================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id,
            'Create an opportunity for peer-to-peer professional development/support & mentorship/exchange of ideas across campuses.',
            11, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id,
            'Create an opportunity for peer-to-peer professional development/support & mentorship/exchange of ideas across campuses.',
            1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id)
    VALUES (v_obj_id, 17);

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id,
            'Establish at least one active community of practice in conjunction with RIT NY whereby faculty of a common area meet on a regular basis to discuss best practices and "compare notes" on course delivery and common issues — each meeting driven by a particular theme.',
            1, 1)
    RETURNING id INTO v_init_id;

    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_init_id, 4);

    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id,
            'Establish at least one active community of practice in conjunction with RIT NY whereby faculty of a common area meet on a regular basis to discuss best practices and "compare notes" on course delivery and common issues — each meeting driven by a particular theme.',
            1)
    RETURNING id INTO v_meas_id;

    -- Achievements: period 4 (2022-2024) COMPLETED
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Community of practice (Teaching Circle) established with NY led by David Yockel and attended by RIT Dubai Writing Faculty as well as the NY Writing Director (Completed by Jillian in Feb 2024).',
            1, 1, 4, NOW(), NOW());

    -- Achievements: period 5 (2024-2025) COMPLETED
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Training to RIT NY Doctoral interns on Psychodynamic theory and therapy (Rupa); supervising doctoral interns in the NY campus.',
            1, 1, 5, NOW(), NOW());
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'ARC grants approved for research collaboration with Dr. Berbary, Crane and Sangiorgio for behavioral health; research collaboration with Dr. John Oliphant for student mental health; collaborating with Ana Havelka at RIT Croatia for research on student mental health.',
            1, 1, 5, NOW(), NOW());

    -- Achievements: period 6 (2025-2026) COMPLETED
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'ANTH Group had one meeting in the Spring semester and shared research ideas and possibilities for collaborations (Razem).',
            1, 1, 6, NOW(), NOW());

    -- =========================================================
    -- GOAL 12: Creative Writing as a tool in multiple professions
    -- (appears only in 2024-2025 sheet, R13)
    -- =========================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id,
            'Create opportunities for the uses of Creative Writing as a tool in multiple professions, from advertising to psychology.',
            12, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id,
            'Create opportunities for the uses of Creative Writing as a tool in multiple professions, from advertising to psychology.',
            1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id)
    VALUES (v_obj_id, 7);

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id,
            'Introduce instructors from other areas and disciplines to speak to how Creative Writing might help reinforce aspects of the discipline.',
            1, 1)
    RETURNING id INTO v_init_id;

    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_init_id, 12);

    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id,
            'Introduce instructors from other areas and disciplines to speak to how Creative Writing might help reinforce aspects of the discipline.',
            1)
    RETURNING id INTO v_meas_id;

    -- Achievements: period 5 (2024-2025)
    -- The Col C text is in present tense describing ongoing work/outcomes — treat as a COMPLETED achievement
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Work has been showcased from the various creative writing courses, and students representing many different majors acknowledge how CW has helped them to better express ambitions and points of view, from issues related to the environment to better articulating aims and ambitions.',
            1, 1, 5, NOW(), NOW());

    -- =========================================================
    -- GOAL 13: Engage program faculty with K-12 (new in 2025-2026)
    -- =========================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id,
            'Create more opportunities for program faculty to engage with K-12 students both on campus and off-site.',
            13, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id,
            'Create more opportunities for program faculty to engage with K-12 students both on campus and off-site.',
            1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id)
    VALUES (v_obj_id, 16);

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id,
            'Establish at least one event per year in collaboration with Admissions and in support of the BS in Psychology whereby faculty deliver talks to K-12 students.',
            1, 1)
    RETURNING id INTO v_init_id;

    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_init_id, 17);

    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id,
            'Establish at least one event per year in collaboration with Admissions and in support of the BS in Psychology whereby faculty deliver talks to K-12 students.',
            1)
    RETURNING id INTO v_meas_id;

    -- Achievements: period 6 (2025-2026) COMPLETED
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Student for a day and Open days organized by Admissions to interact with high school students and provide information about the program. Jihane participated on 18 Dec 2025 and 29 Jan 2026.',
            1, 1, 6, NOW(), NOW());

    -- =========================================================
    -- GOAL 14: Innovation and Entrepreneurship Journey accountability
    -- (new in 2025-2026, R15)
    -- =========================================================
    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_strategy_id,
            'Increase program accountability towards the Innovation and Entrepreneurship Journey.',
            14, 1)
    RETURNING id INTO v_goal_id;

    INSERT INTO objective (goal_id, title, sort_order, frozen, created_by)
    VALUES (v_goal_id,
            'Increase program accountability towards the Innovation and Entrepreneurship Journey.',
            1, false, 1)
    RETURNING id INTO v_obj_id;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id)
    VALUES (v_obj_id, 8);

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_obj_id,
            'Conduct an annual systematic review of the Innovation and Entrepreneurship Journey in the BS in Psychology to continually make enhancements and close gaps in student achievement.',
            1, 1)
    RETURNING id INTO v_init_id;

    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id)
    VALUES (v_init_id, 4);

    INSERT INTO measurement (initiative_id, description, sort_order)
    VALUES (v_init_id,
            'Conduct an annual systematic review of the Innovation and Entrepreneurship Journey in the BS in Psychology to continually make enhancements and close gaps in student achievement.',
            1)
    RETURNING id INTO v_meas_id;

    -- Achievements: period 6 (2025-2026) COMPLETED
    INSERT INTO achievement (measurement_id, title, achievement_type_id, author_id, assessment_period_id, recorded_at, updated_at)
    VALUES (v_meas_id,
            'Improvements to Innovation and Entrepreneurship journey through systematic re-mapping of certain assessments within the journey based on program feedback (Spring 2026 - Rupa, Maaham).',
            1, 1, 6, NOW(), NOW());

    RAISE NOTICE 'V28: LA strategy 13 re-seeded.';
END $$;
