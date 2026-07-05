-- V17: Seed Liberal Arts (LA) department strategy, goals, objectives,
-- initiatives, measurements, and achievements.
--
-- Source: "LA - Copy.xlsx" (sheets: 2022-2024, 2024-2025, 2025-2026), each
-- row pairing a Department Vision/Goal (-> objective) with a Strategic Plan
-- Initiative (-> initiative) and an Assessment and Measurements Result
-- (-> achievement, one per assessment period in which the row had content).
--
-- Rows 2-12 are common to all three yearly sheets (11 objectives); the
-- "Creative Writing" objective appears only in 2024-2025; the "K-12
-- engagement" objective (with two initiatives) and the "Innovation and
-- Entrepreneurship Journey" objective appear only in 2025-2026.
--
-- This mirrors the structural pattern used to seed MAS in V6 (strategy ->
-- goal -> objective -> initiative -> measurement -> achievement). University
-- strategy mapping (objective_mapping / initiative_mapping) is added
-- separately in V18, following the same two-step process used for MAS
-- (V6 seed + V9 mapping).

DO $$
DECLARE
    v_dept_la   BIGINT;
    v_admin_id  BIGINT := 1;
    v_cycle_id  BIGINT := 2;
    v_ap_2224   BIGINT := 4;
    v_ap_2425   BIGINT := 5;
    v_ap_2526   BIGINT := 6;

    v_ds_la BIGINT;
    v_dg    BIGINT;
    v_do    BIGINT;
    v_di    BIGINT;
    v_dm    BIGINT;
BEGIN
    SELECT id INTO v_dept_la FROM department WHERE code = 'LA';

    INSERT INTO strategy (planning_cycle_id, department_id, strategy_type, state, title)
    VALUES (v_cycle_id, v_dept_la, 'DEPARTMENT', 'DEPLOYED', 'Liberal Arts 2022-2027 Departmental Strategic Plan')
    RETURNING id INTO v_ds_la;
    INSERT INTO role_assignment (user_id, strategy_id, role) VALUES (v_admin_id, v_ds_la, 'OWNER');

    INSERT INTO goal (strategy_id, title, sort_order, created_by)
    VALUES (v_ds_la, 'LA Strategic Objectives 2022-2027', 1, v_admin_id) RETURNING id INTO v_dg;

    -- =========================================================================
    -- Objective 1
    -- =========================================================================
    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Introduce successful programs within the LA department that reflect the market needs in the LA space (incoming students and potential employer needs).', 1, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Successfully introduce the BS in Advertising and PR as the 2nd program within the department and 2nd LA program at RIT Dubai (pending Ministry approval) and achieve consistent student enrollment across Fall/Spring intake periods to maximize program enrollment cap. Continue to identify new program offerings that serve the wider community within Liberal Arts.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Adapting curriculum and responding to Ministry requirements for the BS in Advertising and PR',
        'Completed: Adapted the curriculum to include case studies and projects relevant to the Middle Eastern market (Mirosh). In Progress: Responding to Ministry requirements from the IPA for the BS in APR; promoting PR/Comm career options and connecting with professionals in the field, including contacts in Oman for rebranding and the UAE for Crisis in PR (Roula).',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'APR Immersion approved as proof of concept; four program courses rolled out',
        'Completed: Received approval from NY to run the APR Immersion as proof of concept; rolled out four program courses for the BS in APR in 2024-2025; collaborated with NY on curricular modifications to include more production elements (under review with the NY curriculum committee). In Progress: Running an in-depth feasibility study to ensure program viability in the region before responding to Ministry requirements; exploring MS in Experimental Psychology or PhD Cog Sci feasibility (PSYC).',
        1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Documentation submitted to Ministry for IPA of BS in APR',
        'Completed: Documentation submitted to Ministry in Fall 2025 for IPA of the BS in APR. In Progress: PG in Psychology Feasibility Study (Maaham).',
        1, v_admin_id, v_ap_2526);

    -- =========================================================================
    -- Objective 2
    -- =========================================================================
    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Introduce more GenEd LA course offerings while providing a wide variety of perspectives.', 2, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Continue to diversify the LA GenEd electives by offering more courses in Modern Languages as well as more offerings in Anthropology, Economics, Philosophy, and Sociology; explore possibilities of courses delivered in hybrid format while maintaining pedagogical benefit to students.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Ethics Minor course approved; new elective offerings explored',
        'Completed: Received approval by RIT NY to teach the last required course for the Ethics Minor (PHIL 415 Ethical Theory), completed January 2024 (Jamaal). In Progress: Creating a special topics course in Women in Islamic Society (Reem); adding an Anthropology course in Language/Linguistics (Reem). Pending: Opening courses in Sustainability, Nature Writing and Eco Literature to pair with Sociology (Adrianne); offering an Environmental Sociology course (Rayya).',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Four new courses opened; 2025-2026 offerings planned',
        'Completed: Opened a new course in Linguistic Anthropology (Fall 2024); opened 4 new courses in Advertising and PR in 2024-2025; opened a new course in Ethics (PHIL 415 Ethical Theory, Jamaal); opened Spanish II in Spring 2025 along with Great Authors (ENGL418) to complete the Spanish Immersion. Plans in place for 2025-2026: Sociology, Linguistics, two upper-level Economics courses, Intermediate French I, ANTH301 (Social and Cultural Theory), Positive Psychology. Pending: Introduce Environmental Sociology.',
        1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'New GenEd courses launched; further course design in progress',
        'Completed: New GenEd course offerings including Positive Psychology (Jihane); Sociology of Work (SOCI 230) and Social/Cultural Theory (SOCI/ANTH 301) in Fall, Social Inequality (SOCI 225) in Spring (Rayya); Economics Game Theory in Fall 2025 (Shama). In Progress: Designing two new Linguistics courses leading toward a potential immersion/minor (Juwaeriah). Pending: Discussion of a qualitative research course and a Developmental Psychology breadth course (Jihane); delivering COMM 272 Reporting and Writing for News Media (Mehreen, Spring 2026) and ECON 448 Development Economics (Soumya, Spring 2026).',
        1, v_admin_id, v_ap_2526);

    -- =========================================================================
    -- Objective 3
    -- =========================================================================
    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Acquire more minors/immersions to allow more GenEd LA variety.', 3, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Pursue offerings in support of acquiring and/or fulfilling relevant immersions and minors, including the Ethics Minor, Philosophy Immersion/Minor, and Economics Minor.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Ethics and Philosophy Minor approved for Fall 2025',
        'Completed: Received official approval from the NY Campus to offer the Ethics Minor and the Philosophy Minor at RIT Dubai starting Fall 2025 (Jamaal). Pending: For foreign languages, exploring a model where students watch recorded instructor explanations and use contact hours for interactive engagement and practice (Maria, Jessica, Panteha).',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Advertising and PR Immersion and Spanish Immersion completed; new immersions planned for 2025-2026',
        'Completed: Opened all courses required for the Immersion in Advertising and PR; offering the Spanish Immersion in Spring 2025 with the new Spanish II and Great Authors courses; Ethics & Philosophy Minor approved starting Fall 2024, first graduates anticipated Spring 2025; introducing new Immersions/Minors in 2025-2026 (Sociology Immersion, Language Science Immersion, Economics Minor). In Progress: Awaiting official NY approval on streamlining the Minor authorization process. Pending: One course away from the APR Minor.',
        1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'New Sociology Immersion and Economics Minor introduced; Language Science Immersion delivered',
        'Completed: A new Sociology Immersion created with three new Sociology courses in 2025-2026 (Rayya); Economics minor to be offered with two new courses; Language Science Immersion delivered in 2025-2026. In Progress: Awaiting confirmation from NY on a streamlined minor approval process (Jillian).',
        1, v_admin_id, v_ap_2526);

    -- =========================================================================
    -- Objective 4
    -- =========================================================================
    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Produce graduates in LA that are highly employable.', 4, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Establish stronger industry relationships in key areas of Psychology and Advertising/PR through connections such as the LA Advisory Board and faculty contacts/professional societies to increase co-op opportunities for students within LA majors.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Psi Chi membership extended to Dubai; industry engagement initiated',
        'Completed: RIT Dubai received approval extending Psi Chi (Psychology Professional Organization) membership from the NY Chapter to Dubai students (John McCarthy, Feb 2024); invited industry experts for guest lectures (Mirosh); encouraged faculty to attend conferences and seminars to build networks (Mirosh). In Progress: Incorporating student projects, consultancies and educational visits into major-specific courses (Anitha). Pending: Organizing 10 Advertising/PR lectures with attendance certificates (Jasminka); a mentorship program pairing business volunteers with 3rd/4th year students (Jasminka); collaborating with the Co-Op office on a once-a-semester industry connections email (Maria, Jessica, Panteha); cross-collaboration with courses on presentation/communication skills for engineers.',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Guest speakers, client-based projects, and co-op position development',
        'Completed: Invited two Public Relations professionals to speak to all three sections of COMM 212 PR; invited guest speaker Dr Christine Abi Assi, now in contact with the co-op department and two clinics for student opportunities (Jihane); collaborated with the Graphic Design Club on a Media Kit competition for Blood Donation Day; invited 10 guest speakers from organizations including Emirates Speciality Hospital, Fakeeh University Hospital, Maharat Learning Centre and OpenMinds Centre Dubai (Sonakshi); worked with Dubai-based client The Giving Movement for COMM 322 Campaign Management and Planning. In Progress: Collaborating with external partners such as Autism Centre and Behaviour Enrichment to develop projects and courses (Maaham); developing TA and RA co-op position descriptions (Maaham); creating a PR and Advertising contacts list for future collaboration (Roula, Mike).',
        1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'New co-op opportunities created in Psychology and Research',
        'Completed: Created a Psychology-related social media content creation co-op, first student enrolled Summer 2025 with more planned for Summer 2026 (Jihane); created 2 Research Co-op opportunities engaging 3 students over Summer 2025, continuing in 2026 (Rupa); created 3 Research Co-op opportunities and worked with 2 students on research papers under review (Niki). In Progress: Liaising with Keyani Wellness and NeuroKinds for psychology student co-op opportunities (Rupa). Pending: Collaborating with Dubai Culture/Dubai Museums for a Language Club project tied to ANTH220 (Juwaeriah); collaborating with Dubai Digital Authority on research into addiction and digital use in teenagers and young adults (Jihane, May 2026).',
        1, v_admin_id, v_ap_2526);

    -- =========================================================================
    -- Objective 5
    -- =========================================================================
    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Increase student retention through targeted initiatives for at-risk students.', 5, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Continue to work with the ASC to offer workshops and series supporting student success, targeting LA-specific need areas related to writing, research and critical thinking, as part of ASC or an extension of the Writing Center.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Writing Center workshops and ASC tutoring support launched',
        'Completed: Conducted Writing Center workshops to help students with common writing and research challenges (Leen, Feb 2024). In Progress: Working with ASC tutors to provide additional support to students in Economics (Shama). Pending: Requiring faculty to have students attend ASC workshops as part of syllabi (Mirosh); inviting motivational speakers/TED Talks to campus (Maria, Jessica, Panteha).',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Writing Center workshops expanded; at-risk student tracking developed',
        'Completed: Writing Center Workshop on synthesizing sources (Leen); guest speaker Mr. Dulmin on improving language skills via monolingual dictionaries for ELCA classes; ELCA Writing Circle (Jessica, Mirosh); introduced a self-reflective assessment form across foreign language classes; worked with ASC tutors in Fall 2024 to support Economics students, continuing in Spring 2025 (Shama); invited Faryal from the ASC to the Spring Retreat to discuss pedagogical changes. In Progress: Creating a log of at-risk students with personalized action plans (Mirosh, Panteha, Jessica, Ferzana, Leen, Hana, Dulmin); Writing Center report tracking at-risk student engagement (Jessica, Ferzana); ASC workshops on Academic Integrity (Mirosh), common errors (Ferzana), managing references in Microsoft Word (Panteha); Writing Center handouts and website (Mirosh, Jessica, Leen). Pending: Managing references in Microsoft Word workshop to be completed in Spring (Panteha).',
        1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'At-risk registers and Writing Center website developed',
        'Completed: Developed at-risk registers for writing students, collaborating with instructors to optimize outcomes (Jessica); developed a Writing Center website for appointment scheduling and resource access (Jessica). In Progress: Creating department-specific resources for diverse academic needs (Jessica); creating a scientific writing workshop (Maaham, Niki); improving research writing preparation via targeted UWRT assignments (UWRT and Psych Team); guidelines for citing/referencing/acknowledging AI use (Maria); seminar on academic integrity and ethical AI use (Reem Rabea). Pending: Workshop on social capital, peer networks and engagement in student retention, in collaboration with Student Affairs, planned for Spring 2026 (Rayya).',
        1, v_admin_id, v_ap_2526);

    -- =========================================================================
    -- Objective 6
    -- =========================================================================
    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Increase opportunities for faculty research within departmental means.', 6, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Work with LA faculty to propose unique opportunities for increased research output and collaboration, including course release support to lighten teaching load for deserving faculty with minimal financial and human resource impact.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Initial groundwork for securing research grants',
        'Pending: Working collaboratively to secure grants for research projects/publications to enable course buy-out (Anitha, Jasminka); pursuing greater attendance at relevant conferences with intent to publish afterwards (Nasih).',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'VR systematic review pre-registered; collaborations with MBRU and NIH explored',
        'Completed: OSF pre-registration for VR systematic review (Niki). In Progress: A multistage VR and psychiatric disorder research project with Dr Carlos (Design), Jihane, Niki and Rupa (Psychology) underway, systematic review submission targeted for August 2025 (Jihane); introduction, methodology and results being drafted (Jihane); Niki and Rupa pursuing collaboration with MBRU for MRI imaging research; Rupa pursuing NIH grants for underserved populations research, member of the Research Committee. Pending: Submission to selected journals and review process to start September 2025 (Jihane).',
        1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Multiple ARC grants secured; papers under review and published',
        'Completed: Submitted and received 2 ARC grants for research (Rupa); submitted 2 ARC grants and a Sheikh Hamdan Bin Rashid Al Maktoum Health Research Grant application (Niki); two papers under review in collaboration with Psyc faculty and students (Niki); two papers published (Soumya); submitted ARC and Seed grant applications Dec 2025 to Feb 2026 (Jihane); one systematic review published (Jihane, Rupa, Niki). In Progress: Research Session at the LA department retreat to establish research collaborations (Niki); signing an MOU with USEK for collaborative research, online conferences, library access and guest lectures (Maria); seminar on AI in research (Rupa, Niki, Maria). Pending: Submitted Sheikh Hamdan Grant for Biomedical research (Rupa, Niki and other Psyc faculty).',
        1, v_admin_id, v_ap_2526);

    -- =========================================================================
    -- Objective 7
    -- =========================================================================
    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Engage with potential incoming HS students in promotion of the offered LA programs and as a service to the community.', 7, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Start an annual event in Psychology and Advertising and PR (if applicable) to promote such programs while engaging all LA faculty as key players within the course offerings of the degree program(s).', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Outreach event ideas identified',
        'Pending: Establishing an event via relevant student clubs/associations to promote service learning (Anitha); hosting a seminar bringing together experts, faculty and students from Psychology and Advertising/PR (Mirosh); conducting a mental health awareness campaign (Mirosh); a PR and Psychology youth camp with 10 days of lectures (Jasminka).',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Behavioral Health Symposium and Psychology Day held; MUN conference planned',
        'Completed: Behavioral Health Symposium (Rupa); Psychology Day including workshop tasters (mindfulness practice, working together poster, EdTalk) leading to an expert presentation open to the community (Maaham). Pending: RIT Dubai hosting a Model United Nations conference for high school students, planned for late January 2025 (Jamaal); PSYC course taster videos (Jillian, Maaham).',
        1, v_admin_id, v_ap_2425);

    -- =========================================================================
    -- Objective 8
    -- =========================================================================
    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Engage with local high schools to help bridge the gap between K-12 and university.', 8, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Involve all LA faculty in the mission of bridging the gap between high school and university by creating a series of talks delivered to high school students and/or teachers as they relate to LA disciplines.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Outreach to admissions and K-12 workshop ideas explored',
        'Pending: Welcoming high school students into class during the semester, pending follow-up with Admissions (Roula, Jasminka); organizing online K-12 career orientation workshops (Maria, Jessica, Panteha).',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Participated in Sharjah career fair; school partnerships strengthened',
        'Completed: Participated in a school career fair in Sharjah with a footfall of 1000 high school students. In Progress: Strengthening relations with GEMs Wellington DSO for LA faculty guest speaking (Jillian); facilitated an MOU with GEMS Al-Khaleej International School on recruiting students and offering scholarships (Reem Razem).',
        1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'GEMs Wellington DSO relationship continued',
        'In Progress: Strengthening relations with GEMs Wellington DSO and proposing disciplines for LA faculty guest speaking (Jillian). Pending: Interactive sessions introducing sociological concepts (inequality, globalization) during school visits in Spring 2026 (Rayya).',
        1, v_admin_id, v_ap_2526);

    -- =========================================================================
    -- Objective 9
    -- =========================================================================
    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Provide diverse opportunities for professional development (in teaching and in research) within the department.', 9, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Leverage existing talent within the LA Department to carry out an internal professional development/best practices series where faculty showcase their unique areas of strength, covering best practices in teaching, technology integration, and research/publishing insights.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Best practices in teaching session delivered; CPD workshops planned',
        'Completed: Best practices in teaching session covering technology integration, effective assessment strategies, constructive feedback, student-centered learning, and engagement/motivation (Mirosh). Pending: Faculty-led LA CPD workshops on publishing and grant writing (Anitha, Maria); teaching and research seminars with a shared best-practice document (Jasminka); presentation on Perusall, a social annotation platform (Jamaal).',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'AI integration workshops and FDC seminar series delivered',
        'Completed: Managing Stress at Work workshop for HR (Jihane, Niki, Rupa); workshop on mental well-being for student athletes (Rupa); CITI training highlight for faculty (Rupa); Workshop on AI Integration in the classroom, Spring 2025 (Maria); internal UWRT professional development sessions on APA referencing (Panteha) and Padlet (Dulmin); FDC Seminar series 2024-25 on GenAI applications in education (Maria); integration of AI across PSYC mapping completed June 2025 (Jihane, Sonakshi). In Progress: Assessment standardization workshop for PSYC (Maaham). Pending: A database for instructors to share AI course integration insights (Maria); workshop for the Ureka Global Innovation Hub (Jihane, Niki, Rupa); workshop series for non-Arabic speaking faculty on Arabic language and culture (Reem Razem); Brown Bag Session series on language research (Reem Razem).',
        1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'AI PDP and Teaching Guide published; faculty AI integration support ongoing',
        'Completed: Creation of an AI PDP for the LA Department anchored in behavioral theories of technological adoption, with a 2025-2026 implementation plan (Jillian, Maria); publication of a comprehensive AI Integration Teaching and Assessment guide (Khalid, Maria); 33 individual faculty meetings conducted to support AI integration, data collected and used to customize the AI PDP (Jillian, Maria); session on AI anxiety (Rupa). In Progress: Seminar on AI integration for faculty (Maria); seminar on curriculum/instructional design and academic integrity pending policy approval (Maria); communities of practice for AI integration insights (Maria); AI tools 3-minute video series (Maria); collaborative space for AI integration insights (Maria); building an AI repository for faculty (Maria). Pending: Talks on sleep, motivation and stress management with the sport centre, pushed to Fall 2027 (Jihane).',
        1, v_admin_id, v_ap_2526);

    -- =========================================================================
    -- Objective 10
    -- =========================================================================
    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Create an opportunity for peer-to-peer professional development/support and mentorship/exchange of ideas within the LA Department at RIT Dubai.', 10, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Continue to promote Peer Review of Educational Practice (PREP) to create a community of practice within the department, mandatory for all new LA faculty and highly encouraged among existing faculty.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'LA newsletter launched to highlight achievements',
        'Completed: Launched a Liberal Arts Department newsletter for the entire student and faculty body via the LA Club (Graphic Design Club) to highlight faculty and student achievements (Feb 2024). In Progress: Sharing the annual list of peer reviewers and perspectives on what worked well in the PREP process (Jasminka, Jillian).',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'PREP sessions completed across disciplines',
        'Completed: PREP sessions completed in Fall 2024 with reciprocal observation between reviewers and reviewees; created WhatsApp groups for UWRT 100 and UWRT 150 faculty to discuss lessons and assignment plans; 8 PREPs completed across PSYC; RIT Global Behavioral Health Symposium scheduled for April 9 (Rupa); created a space connecting Foreign Language instructors to exchange pedagogical practices (Jillian, Nasih, Maria). In Progress: Initiative on AI integration into all LA courses to enhance pedagogical innovation and AI literacy (Maria). Pending: Creating an ANTH PREP Group including ANTH instructors Yahya, Rayya, Reem, Juwaeriah (Reem Razem).',
        1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'ANTH PREP Group created',
        'Completed: Created the ANTH PREP Group including instructors Yahya, Rayya, Reem and Juwaeriah (initiated by Razem, created by Yahya).',
        1, v_admin_id, v_ap_2526);

    -- =========================================================================
    -- Objective 11
    -- =========================================================================
    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Create an opportunity for peer-to-peer professional development/support and mentorship/exchange of ideas across campuses.', 11, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Establish at least one active community of practice in conjunction with RIT NY whereby faculty of a common area meet regularly to discuss best practices and compare notes on course delivery and common issues such as plagiarism, each meeting driven by a particular theme.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Teaching Circle community of practice established with NY',
        'Completed: Community of practice (Teaching Circle) established with NY, led by David Yockel and attended by RIT Dubai Writing Faculty and the NY Writing Director (Jillian, Feb 2024). In Progress: Using RIT NY global faculty grants to meet colleagues in person, connect with department chairs, and use a hybrid model for jointly conducted courses (Jasminka).',
        1, v_admin_id, v_ap_2224);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Cross-campus research collaborations and ELCA pedagogy meeting',
        'Completed: Training to RIT NY doctoral interns on psychodynamic theory and therapy and supervising doctoral interns at the NY campus (Rupa); ARC grants approved for behavioral health research collaboration with Dr. Berbary, Crane and Sangiorgio; research collaboration with Dr. John Oliphant on student mental health; collaborating with Ana Havelka at RIT Croatia on student mental health research. In Progress: Meeting arranged with ELCA instructors from Croatia and RIT Dubai to discuss pedagogy and review materials, with a planned spring follow-up; coordinating with RIT NY SRS and PHT180 to seek grant writing support for RIT Dubai (Rupa). Pending: Establishing a platform for sharing current projects and research across campuses in PSYC-related areas (Maaham).',
        1, v_admin_id, v_ap_2425);
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'ANTH and Linguistics cross-campus research groups active',
        'Completed: ANTH Group held one meeting in the Spring semester sharing research ideas and collaboration possibilities (Razem). In Progress: Brown Bag Linguistics Research Group with ongoing biweekly meetings since February 2026/Spring Term (Razem, Juwaeriah). Pending: Collaborating with Computer Science faculty and students on joint NLP (Natural Language Processing) projects/workshops for Linguistics students (Juwaeriah).',
        1, v_admin_id, v_ap_2526);

    -- =========================================================================
    -- Objective 12 (2024-2025 sheet only)
    -- =========================================================================
    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Create opportunities for the uses of Creative Writing as a tool in multiple professions, from advertising to psychology.', 12, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Introduce instructors from other areas and disciplines to speak to how Creative Writing might help reinforce aspects of their discipline.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Creative Writing work showcased across disciplines',
        'Work has been showcased from various creative writing courses, with students representing many different majors. Students acknowledge how Creative Writing has helped them better express ambitions and points of view, from issues related to the environment to better articulating aims and ambitions.',
        1, v_admin_id, v_ap_2425);

    -- =========================================================================
    -- Objective 13 (2025-2026 sheet only; two initiatives)
    -- =========================================================================
    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Create more opportunities for program faculty to engage with K-12 students both on campus and off-site.', 13, v_admin_id) RETURNING id INTO v_do;

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Establish at least one event per year in collaboration with Admissions and in support of the BS in Psychology whereby faculty deliver talks to K-12 students.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Student for a Day and Open Day support delivered for HS admissions',
        'Completed: Student for a Day and Open Days organized by Admissions to interact with high school students and provide program information (Jihane, 18 Dec 2025 and 29 Jan 2026 open day and student-for-a-day support for HS admission). In Progress: Interactive sports open day, Athletics Focused, planned for June 2026 (Jihane).',
        1, v_admin_id, v_ap_2526);

    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Establish at least one event per year in collaboration with Admissions hosted on campus (in the Innovation Center) for K-12 students.', 2, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Psych Day moved to Fall 2026',
        'In Progress: Psych Day 2026 moved to Fall 2026.',
        1, v_admin_id, v_ap_2526);

    -- =========================================================================
    -- Objective 14 (2025-2026 sheet only)
    -- =========================================================================
    INSERT INTO objective (goal_id, title, sort_order, created_by)
    VALUES (v_dg, 'Increase program accountability towards the Innovation and Entrepreneurship Journey.', 14, v_admin_id) RETURNING id INTO v_do;
    INSERT INTO initiative (objective_id, title, sort_order, created_by)
    VALUES (v_do, 'Conduct an annual systematic review of the Innovation and Entrepreneurship Journey in the BS in Psychology to continually make enhancements and close gaps in student achievement.', 1, v_admin_id) RETURNING id INTO v_di;
    INSERT INTO measurement (initiative_id, description, sort_order) VALUES (v_di, 'Progress and achievements', 1) RETURNING id INTO v_dm;
    INSERT INTO achievement (measurement_id, title, details, achievement_type_id, author_id, assessment_period_id)
    VALUES (v_dm, 'Systematic re-mapping of Innovation and Entrepreneurship Journey assessments',
        'Completed: Improvements to the Innovation and Entrepreneurship journey through systematic re-mapping of certain assessments within the journey based on program feedback (Spring 2026, Rupa, Maaham).',
        1, v_admin_id, v_ap_2526);

    RAISE NOTICE 'V17: LA department strategy seeded (14 objectives, 15 initiatives).';
END $$;
