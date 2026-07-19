-- V16: Add objective and initiative mappings for Co-op and Outreach department
--
-- Source: 22-27-Strategic plan mapping-AR Tracking.xlsx (A&R, OCE sheets)
-- Co-op and Outreach is labelled "Coop" / "OCE" in the university tracking
-- sheet's department rows.
--
-- A&R/OCE sheet analysis (matched by content, since dept_initiative_id and
-- objective_id are 1:1 aligned in this department: ini_(N+11) belongs to obj_N):
--   obj_87  (meet gov entities)        -> uni_obj_10 (already mapped via V6)
--   obj_88  (At-School G12 workshops)  -> uni_obj_16 (admission initiatives: highschool visits/workshops)
--   obj_89  (coop prep course)         -> uni_obj_12 (raise interns/coop quality)
--   obj_90  (research/consultancy coop)-> uni_obj_13 (engage industry/gov partners innovatively)
--   obj_91  (alumni data)              -> uni_obj_14 (already mapped via V6)
--   obj_92  (employer/career fair growth) -> uni_obj_15 (already mapped via V6)
--   obj_93  (open day rep)             -> uni_obj_16 (already mapped via V6)
--   obj_94  (event sponsors)           -> uni_obj_17 (already mapped via V6)
--   obj_95  (prof courses annually)    -> uni_obj_18 (already mapped via V6)
--   obj_96  (employer workshops at career fairs) -> uni_obj_15 (employer/career fair engagement)
--
-- Department covered: Co-op and Outreach (strategy 9, obj 87-96, ini 98-107)
-- V6 already mapped obj_87, obj_91, obj_92, obj_93, obj_94, obj_95; obj_88, obj_89, obj_90, obj_96 missing.
-- Zero initiative_mapping rows exist yet for strategy 9.

DO $$
DECLARE
    v_uni_obj_12 BIGINT;   -- Raise interns/coop quality
    v_uni_obj_13 BIGINT;   -- Engage with industry and government partners in more innovative ways
    v_uni_obj_15 BIGINT;   -- 10% annual increase in employers number/career fair attendance
    v_uni_obj_16 BIGINT;   -- Active annual participation in admission initiatives

    v_uni_ini_16 BIGINT;   -- Collaborate with admission on sponsoring an event with a k-12 school
    v_uni_ini_17 BIGINT;   -- Each department holds at least one k-12 event per year in the Innovation Center
    v_uni_ini_22 BIGINT;   -- Utilize RIT365 and extra coaching sessions to raise interns/coop quality
    v_uni_ini_23 BIGINT;   -- Create 6 applied research/consultancy projects-coop
    v_uni_ini_24 BIGINT;   -- Keep Alumni data completed and accurate for at least 75% of all Alumni
    v_uni_ini_26 BIGINT;   -- Each Academic department is engaged in at least one event per term
    v_uni_ini_27 BIGINT;   -- Meet 10 Gov. entities to promote courses
    v_uni_ini_28 BIGINT;   -- Target 2 event sponsors (Gov. or Corp.)
    v_uni_ini_30 BIGINT;   -- Academic programs engage with EE&C in promoting executive education offerings

    v_coop_obj_88 BIGINT;  v_coop_obj_89 BIGINT;  v_coop_obj_90 BIGINT;  v_coop_obj_96 BIGINT;

    v_coop_ini_98  BIGINT;  v_coop_ini_99  BIGINT;  v_coop_ini_100 BIGINT;  v_coop_ini_101 BIGINT;
    v_coop_ini_102 BIGINT;  v_coop_ini_103 BIGINT;  v_coop_ini_104 BIGINT;  v_coop_ini_105 BIGINT;
    v_coop_ini_106 BIGINT;  v_coop_ini_107 BIGINT;
BEGIN
    SELECT id INTO v_uni_obj_12 FROM objective WHERE title LIKE 'Raise interns/coop quality'                  LIMIT 1;
    SELECT id INTO v_uni_obj_13 FROM objective WHERE title LIKE 'Engage with industry and government partners%' LIMIT 1;
    SELECT id INTO v_uni_obj_15 FROM objective WHERE title LIKE '10%% annual increase in employers number%'    LIMIT 1;
    SELECT id INTO v_uni_obj_16 FROM objective WHERE title LIKE 'Active annual participation in admission%'    LIMIT 1;

    SELECT id INTO v_uni_ini_16 FROM initiative WHERE title LIKE 'Collaborate with admission on sponsoring an event%'       LIMIT 1;
    SELECT id INTO v_uni_ini_17 FROM initiative WHERE title LIKE 'Each department holds at least one k-12 event%'           LIMIT 1;
    SELECT id INTO v_uni_ini_22 FROM initiative WHERE title LIKE 'Utilize RIT365 and extra coaching sessions%'              LIMIT 1;
    SELECT id INTO v_uni_ini_23 FROM initiative WHERE title LIKE 'Create 6 applied research/consultancy projects-coop'      LIMIT 1;
    SELECT id INTO v_uni_ini_24 FROM initiative WHERE title LIKE 'Keep Alumni data completed and accurate%'                 LIMIT 1;
    SELECT id INTO v_uni_ini_26 FROM initiative WHERE title LIKE 'Each Academic department is engaged in at least one event%' LIMIT 1;
    SELECT id INTO v_uni_ini_27 FROM initiative WHERE title LIKE 'Meet 10 Gov. entities to promote courses.'                LIMIT 1;
    SELECT id INTO v_uni_ini_28 FROM initiative WHERE title LIKE 'Target 2 event sponsors%'                                 LIMIT 1;
    SELECT id INTO v_uni_ini_30 FROM initiative WHERE title LIKE 'Academic programs engage with all programs, especially EE&C%' LIMIT 1;

    SELECT id INTO v_coop_obj_88 FROM objective WHERE title LIKE '"At-School" workshops for G12 students%'                 LIMIT 1;
    SELECT id INTO v_coop_obj_89 FROM objective WHERE title LIKE 'Improve the Coop prep course syllabus%'                  LIMIT 1;
    SELECT id INTO v_coop_obj_90 FROM objective WHERE title LIKE 'Invite all faculty to recommend "Research/Consultancy"%' LIMIT 1;
    SELECT id INTO v_coop_obj_96 FROM objective WHERE title LIKE 'Arrange workshops/activities for employers%'             LIMIT 1;

    SELECT id INTO v_coop_ini_98  FROM initiative WHERE title LIKE 'Invite 5 Gov. entities and ensure 2 attend every career fair%'  LIMIT 1;
    SELECT id INTO v_coop_ini_99  FROM initiative WHERE title LIKE 'Conduct 2 "At-School" workshops and 1 Open day per year'        LIMIT 1;
    SELECT id INTO v_coop_ini_100 FROM initiative WHERE title LIKE 'Add "What makes you, You" experience%'                         LIMIT 1;
    SELECT id INTO v_coop_ini_101 FROM initiative WHERE title LIKE '1 consultancy per year. 5 research per year.'                  LIMIT 1;
    SELECT id INTO v_coop_ini_102 FROM initiative WHERE title LIKE 'Grant access to SIS for accurate alumni data%'                  LIMIT 1;
    SELECT id INTO v_coop_ini_103 FROM initiative WHERE title LIKE 'Use the multi-purpose hall for the Spring career fair%'          LIMIT 1;
    SELECT id INTO v_coop_ini_104 FROM initiative WHERE title LIKE 'Coop Office to attend all open days'                            LIMIT 1;
    SELECT id INTO v_coop_ini_105 FROM initiative WHERE title LIKE 'Approach Dubai Police to sponsor some students%'                LIMIT 1;
    SELECT id INTO v_coop_ini_106 FROM initiative WHERE title LIKE 'Define 6 relevant Prof. courses and offer 2 annually%'          LIMIT 1;
    SELECT id INTO v_coop_ini_107 FROM initiative WHERE title LIKE '1 workshop every semester'                                     LIMIT 1;

    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_coop_obj_88, v_uni_obj_16); -- G12 workshops → active admission participation
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_coop_obj_89, v_uni_obj_12); -- coop prep course → raise interns/coop quality
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_coop_obj_90, v_uni_obj_13); -- research/consultancy coop → engage industry/gov partners
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_coop_obj_96, v_uni_obj_15); -- employer workshops at career fairs → employer/career fair growth

    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_coop_ini_98,  v_uni_ini_27); -- invite gov entities to career fair → meet 10 gov entities
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_coop_ini_99,  v_uni_ini_17); -- At-School workshops/open day → dept k-12 event
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_coop_ini_100, v_uni_ini_22); -- coaching sessions/skills → RIT365 raise coop quality
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_coop_ini_101, v_uni_ini_23); -- consultancy/research → 6 applied research/consultancy projects-coop
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_coop_ini_102, v_uni_ini_24); -- SIS access/alumni data → keep alumni data accurate
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_coop_ini_103, v_uni_ini_26); -- career fair/exhibitions → dept engaged in event per term
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_coop_ini_104, v_uni_ini_16); -- coop office attends open days → collaborate with admission on event
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_coop_ini_105, v_uni_ini_28); -- Dubai Police sponsorship → target 2 event sponsors
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_coop_ini_106, v_uni_ini_30); -- prof/certification courses → academic programs engage EE&C exec ed
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_coop_ini_107, v_uni_ini_26); -- semester workshop → dept engaged in event per term

    RAISE NOTICE 'V16 Co-op and Outreach: objective mappings (4 new) and initiative mappings (10) inserted.';
END $$;
