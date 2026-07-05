-- V9: Math and Sciences (MAS) objective and initiative mappings
--
-- Source: 22-27-Strategic plan mapping-AR Tracking.xlsx (Academic & Research sheet)
--         and Math n Science Dept Vision Actions.xlsx (three yearly sheets).
--
-- Method: The university A&R sheet has 14 blocks of ~10 rows, one per university
-- initiative. Each block contains one MathnScience row whose initiative/assessment
-- text matches a row in the dept file, identifying which dept goal/objective maps
-- to which university initiative (and its parent university objective).
--
-- Block-to-department-goal mapping (university A&R sheet):
--   Block 1  (R9,   Uni Ini: Alumni engagement)              ← MAS GOAL 1 (research activities)
--   Block 2  (R21,  Uni Ini: Faculty PD opportunities)       ← MAS GOAL 3 (faculty training)
--   Block 3  (R31,  Uni Ini: Industry connections co-op)     ← MAS GOAL 2 (speakers/external collaboration)
--   Block 4  (R41,  Uni Ini: Academic Development unit)      ← MAS GOAL 6 (teaching quality)
--   Block 5  (R51,  Uni Ini: Early Alert)                    ← MAS: no entry (empty)
--   Block 6  (R61,  Uni Ini: Target freshman students)       ← MAS GOAL 4 (competitions & clubs)
--   Block 7  (R71,  Uni Ini: Faculty research support)       ← MAS GOAL 1 (research activities, again)
--   Block 8  (R81,  Uni Ini: Accreditations)                 ← MAS: no entry (empty)
--   Block 9  (R91,  Uni Ini: Research centers taskforce)     ← MAS: no entry (empty)
--   Block 10 (R101, Uni Ini: Engage students with faculty)   ← MAS GOAL 6 (teaching quality, again)
--   Block 11 (R111, Uni Ini: Increase use of IT)             ← MAS GOAL 6 (teaching quality — AI/tech)
--   Block 12 (R121, Uni Ini: Innovative teaching models)     ← MAS GOAL 5 (sharing activities)
--   Block 13 (R131, Uni Ini: New advances in courses)        ← MAS GOAL 6 (teaching quality — CLO/NY)
--   Block 14 (R141, Uni Ini: Introduce new programs)         ← MAS: no entry (empty)

DO $$
DECLARE
    -- University objective IDs
    v_uni_obj_2  BIGINT;   -- Achieve 90% employability
    v_uni_obj_3  BIGINT;   -- Maintain attrition below 10%
    v_uni_obj_4  BIGINT;   -- Increase RIT Dubai rank score in UAE MoE
    v_uni_obj_5  BIGINT;   -- Establish research and innovation centers of excellence
    v_uni_obj_6  BIGINT;   -- Automate repetitive internal processes
    v_uni_obj_7  BIGINT;   -- Enhance curriculum with innovative teaching

    -- University initiative IDs (A&R sheet only)
    v_uni_ini_1  BIGINT;   -- More engagement with Alumni
    v_uni_ini_2  BIGINT;   -- Provide faculty with diverse PD opportunities
    v_uni_ini_3  BIGINT;   -- Each program helps co-op with industry connection
    v_uni_ini_4  BIGINT;   -- Establish vibrant Academic Development unit
    v_uni_ini_5  BIGINT;   -- More effective use of Early Alert + ASC
    v_uni_ini_6  BIGINT;   -- Target freshman students with program events
    v_uni_ini_7  BIGINT;   -- Increase support for faculty research
    v_uni_ini_10 BIGINT;   -- Engage students with faculty for teaching/research
    v_uni_ini_11 BIGINT;   -- Increase use of IT to streamline tasks
    v_uni_ini_12 BIGINT;   -- Formalize innovative teaching models
    v_uni_ini_13 BIGINT;   -- Establish strategy for new advances in courses
    v_uni_ini_14 BIGINT;   -- Introduce new programs and course offerings

    -- MAS department objective IDs
    v_mas_obj_109 BIGINT;  -- research-based activities
    v_mas_obj_110 BIGINT;  -- speakers/webinars/external collaboration
    v_mas_obj_111 BIGINT;  -- faculty training/development
    v_mas_obj_112 BIGINT;  -- internal competitions and clubs
    v_mas_obj_113 BIGINT;  -- sharing activities/communications
    v_mas_obj_114 BIGINT;  -- quality of teaching and learning

    -- MAS initiative IDs
    v_ini_131 BIGINT; v_ini_132 BIGINT; v_ini_133 BIGINT; v_ini_134 BIGINT; v_ini_135 BIGINT;
    v_ini_136 BIGINT; v_ini_137 BIGINT; v_ini_138 BIGINT; v_ini_139 BIGINT; v_ini_140 BIGINT;
    v_ini_141 BIGINT; v_ini_142 BIGINT;
    v_ini_143 BIGINT; v_ini_144 BIGINT;
    v_ini_145 BIGINT; v_ini_146 BIGINT; v_ini_147 BIGINT; v_ini_148 BIGINT;
    v_ini_149 BIGINT; v_ini_150 BIGINT; v_ini_151 BIGINT; v_ini_152 BIGINT; v_ini_153 BIGINT;
    v_ini_154 BIGINT; v_ini_155 BIGINT; v_ini_156 BIGINT; v_ini_157 BIGINT; v_ini_158 BIGINT;
    v_ini_159 BIGINT; v_ini_160 BIGINT; v_ini_161 BIGINT;

BEGIN
    -- -------------------------------------------------------------------------
    -- Resolve university objective IDs
    -- -------------------------------------------------------------------------
    SELECT id INTO v_uni_obj_2 FROM objective WHERE title LIKE 'Achieve 90% employability%' LIMIT 1;
    SELECT id INTO v_uni_obj_3 FROM objective WHERE title LIKE 'Maintain attrition%' LIMIT 1;
    SELECT id INTO v_uni_obj_4 FROM objective WHERE title LIKE 'Increase RIT Dubai rank score%' LIMIT 1;
    SELECT id INTO v_uni_obj_5 FROM objective WHERE title LIKE 'Establish research and innovation centers%' LIMIT 1;
    SELECT id INTO v_uni_obj_6 FROM objective WHERE title LIKE 'Automate repetitive internal%' LIMIT 1;
    SELECT id INTO v_uni_obj_7 FROM objective WHERE title LIKE 'Enhance curriculum with innovative%' LIMIT 1;

    -- -------------------------------------------------------------------------
    -- Resolve university initiative IDs
    -- -------------------------------------------------------------------------
    SELECT id INTO v_uni_ini_1  FROM initiative WHERE title LIKE 'More engagement with Alumni%' LIMIT 1;
    SELECT id INTO v_uni_ini_2  FROM initiative WHERE title LIKE 'Provide faculty members with diverse professional%' LIMIT 1;
    SELECT id INTO v_uni_ini_3  FROM initiative WHERE title LIKE 'Each program helps co-op office%' LIMIT 1;
    SELECT id INTO v_uni_ini_4  FROM initiative WHERE title LIKE 'Establish a vibrant Academic Development%' LIMIT 1;
    SELECT id INTO v_uni_ini_5  FROM initiative WHERE title LIKE 'More effective use of Early Alert%' LIMIT 1;
    SELECT id INTO v_uni_ini_6  FROM initiative WHERE title LIKE 'Target freshman students with program specific%' LIMIT 1;
    SELECT id INTO v_uni_ini_7  FROM initiative WHERE title LIKE 'Increase support for faculty research%' LIMIT 1;
    SELECT id INTO v_uni_ini_10 FROM initiative WHERE title LIKE 'Engage students with faculty to improve teaching%' LIMIT 1;
    SELECT id INTO v_uni_ini_11 FROM initiative WHERE title LIKE 'Increase use of IT to streamline%' LIMIT 1;
    SELECT id INTO v_uni_ini_12 FROM initiative WHERE title LIKE 'Formalize innovative teaching models%' LIMIT 1;
    SELECT id INTO v_uni_ini_13 FROM initiative WHERE title LIKE 'Establish a strategy for all programs%' LIMIT 1;
    SELECT id INTO v_uni_ini_14 FROM initiative WHERE title LIKE 'Introduce new programs and course offerings%' LIMIT 1;

    -- -------------------------------------------------------------------------
    -- Resolve MAS objective IDs
    -- -------------------------------------------------------------------------
    SELECT id INTO v_mas_obj_109 FROM objective WHERE title LIKE 'To promote and encourage Math and science research-based%' LIMIT 1;
    SELECT id INTO v_mas_obj_110 FROM objective WHERE title LIKE 'To invite speakers through webinars%' LIMIT 1;
    SELECT id INTO v_mas_obj_111 FROM objective WHERE title LIKE 'To prepare training workshops for faculty in Math%' LIMIT 1;
    SELECT id INTO v_mas_obj_112 FROM objective WHERE title LIKE 'To hold internal Math and Science competitions%' LIMIT 1;
    SELECT id INTO v_mas_obj_113 FROM objective WHERE title LIKE 'To share positive teaching and research%' LIMIT 1;
    SELECT id INTO v_mas_obj_114 FROM objective WHERE title LIKE 'To enhance the quality of teaching and learning%' LIMIT 1;

    -- -------------------------------------------------------------------------
    -- Resolve MAS initiative IDs
    -- -------------------------------------------------------------------------
    -- GOAL 1 (research activities)
    SELECT id INTO v_ini_131 FROM initiative WHERE title LIKE 'Direct collaboration with other departments in research-based projects%' LIMIT 1;
    SELECT id INTO v_ini_132 FROM initiative WHERE title LIKE 'Include an advisory board to support research ideas%' LIMIT 1;
    SELECT id INTO v_ini_133 FROM initiative WHERE title LIKE 'Encourage students to consider Math, Physics and Sciences%' LIMIT 1;
    SELECT id INTO v_ini_134 FROM initiative WHERE title LIKE 'Allocating direct fund for research-based activities%' LIMIT 1;
    SELECT id INTO v_ini_135 FROM initiative WHERE title LIKE 'Initiate developing a new program in MaS%' LIMIT 1;
    -- GOAL 2 (speakers / external collaboration)
    SELECT id INTO v_ini_136 FROM initiative WHERE title LIKE 'Direct or indirect collaboration with external institutions%' LIMIT 1;
    SELECT id INTO v_ini_137 FROM initiative WHERE title LIKE 'Promote more hands-on sessions in Math%' LIMIT 1;
    SELECT id INTO v_ini_138 FROM initiative WHERE title LIKE 'Promote recent developments in the area of teaching%' LIMIT 1;
    SELECT id INTO v_ini_139 FROM initiative WHERE title LIKE 'Collaborate with schools in terms of talks%' LIMIT 1;
    SELECT id INTO v_ini_140 FROM initiative WHERE title LIKE 'Collaboration with colleagues from other departments to present students%' LIMIT 1;
    SELECT id INTO v_ini_141 FROM initiative WHERE title LIKE 'Enhance engagement of faculty in student for a day%' LIMIT 1;
    SELECT id INTO v_ini_142 FROM initiative WHERE title LIKE 'Engage with day trips to industrial visits%' LIMIT 1;
    -- GOAL 3 (faculty training)
    SELECT id INTO v_ini_143 FROM initiative WHERE title LIKE 'Collaboration with FDC through department representative%' LIMIT 1;
    SELECT id INTO v_ini_144 FROM initiative WHERE title LIKE 'Guidance and suggestions from advisory board%' LIMIT 1;
    -- GOAL 4 (competitions)
    SELECT id INTO v_ini_145 FROM initiative WHERE title LIKE 'Organizing and holding competitions among schools%' LIMIT 1;
    SELECT id INTO v_ini_146 FROM initiative WHERE title LIKE 'Collaboration with RIT-D students union and ASC%' LIMIT 1;
    SELECT id INTO v_ini_147 FROM initiative WHERE title LIKE 'Forming a student MATH club%' LIMIT 1;
    SELECT id INTO v_ini_148 FROM initiative WHERE title LIKE 'Holding internal competitions around different subjects taught in MaS%' LIMIT 1;
    -- GOAL 5 (sharing activities)
    SELECT id INTO v_ini_149 FROM initiative WHERE title LIKE 'Collaboration and communication through RIT-D representative in GLEC%' LIMIT 1;
    SELECT id INTO v_ini_150 FROM initiative WHERE title LIKE 'Engage faculty in presenting research-based activities%' LIMIT 1;
    SELECT id INTO v_ini_151 FROM initiative WHERE title LIKE 'Develop MaS LinkedIn page%' LIMIT 1;
    SELECT id INTO v_ini_152 FROM initiative WHERE title LIKE 'Develop MaS page on RIT Dubai website%' LIMIT 1;
    SELECT id INTO v_ini_153 FROM initiative WHERE title LIKE 'Enhance faculty engagement in open days%' LIMIT 1;
    -- GOAL 6 (teaching quality)
    SELECT id INTO v_ini_154 FROM initiative WHERE title LIKE 'Enhancement of course learning outcomes to comply better%' LIMIT 1;
    SELECT id INTO v_ini_155 FROM initiative WHERE title LIKE 'Hold frequent meetings with chairs of other departments%' LIMIT 1;
    SELECT id INTO v_ini_156 FROM initiative WHERE title LIKE 'Consider quality of teaching while hiring%' LIMIT 1;
    SELECT id INTO v_ini_157 FROM initiative WHERE title LIKE 'Provide faculty with workshops, trainings, talks%' LIMIT 1;
    SELECT id INTO v_ini_158 FROM initiative WHERE title LIKE 'Explore and initiate new methods of teaching%' LIMIT 1;
    SELECT id INTO v_ini_159 FROM initiative WHERE title LIKE 'Increase peer-controlled support provided by ASC%' LIMIT 1;
    SELECT id INTO v_ini_160 FROM initiative WHERE title LIKE 'Frequent and clear communications with advising bodies%' LIMIT 1;
    SELECT id INTO v_ini_161 FROM initiative WHERE title LIKE 'Promote the importance of assessing PLOs%' LIMIT 1;

    -- -------------------------------------------------------------------------
    -- Clear any existing (incorrect) objective mappings for MAS objectives
    -- -------------------------------------------------------------------------
    DELETE FROM objective_mapping
    WHERE dept_objective_id IN (v_mas_obj_109, v_mas_obj_110, v_mas_obj_111,
                                 v_mas_obj_112, v_mas_obj_113, v_mas_obj_114);

    -- -------------------------------------------------------------------------
    -- Objective mappings
    -- Each entry = one (MAS objective, university objective) pair derived from
    -- the block(s) in the A&R sheet where MAS content appears.
    -- -------------------------------------------------------------------------

    -- GOAL 1 (research activities) → Uni Obj 2 (Block 1: alumni/employability)
    --                              → Uni Obj 4 (Block 7: faculty research/rank score)
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_mas_obj_109, v_uni_obj_2);
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_mas_obj_109, v_uni_obj_4);

    -- GOAL 2 (speakers/external collaboration) → Uni Obj 2 (Block 3: industry connections)
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_mas_obj_110, v_uni_obj_2);

    -- GOAL 3 (faculty training) → Uni Obj 2 (Block 2: faculty PD opportunities)
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_mas_obj_111, v_uni_obj_2);

    -- GOAL 4 (competitions & clubs) → Uni Obj 3 (Block 6: target freshman students)
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_mas_obj_112, v_uni_obj_3);

    -- GOAL 5 (sharing activities) → Uni Obj 7 (Block 12: innovative teaching models)
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_mas_obj_113, v_uni_obj_7);

    -- GOAL 6 (teaching quality) → Uni Obj 3 (Block 4: academic development)
    --                           → Uni Obj 5 (Block 10: engage students with faculty)
    --                           → Uni Obj 6 (Block 11: IT/technology use)
    --                           → Uni Obj 7 (Block 13: new advances in courses)
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_mas_obj_114, v_uni_obj_3);
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_mas_obj_114, v_uni_obj_5);
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_mas_obj_114, v_uni_obj_6);
    INSERT INTO objective_mapping (dept_objective_id, university_objective_id) VALUES (v_mas_obj_114, v_uni_obj_7);

    -- -------------------------------------------------------------------------
    -- Initiative mappings  (dept initiative → most specific matching uni initiative)
    -- -------------------------------------------------------------------------

    -- GOAL 1: research activities (obj 109)
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_ini_131, v_uni_ini_7);   -- direct research collab → faculty research support
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_ini_132, v_uni_ini_7);   -- advisory board for research → faculty research support
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_ini_133, v_uni_ini_10);  -- encourage students in research → engage students with faculty
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_ini_134, v_uni_ini_7);   -- fund for research activities → faculty research support
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_ini_135, v_uni_ini_14);  -- initiate new program in MaS → introduce new programs

    -- GOAL 2: speakers / external collaboration (obj 110)
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_ini_136, v_uni_ini_3);   -- external collaboration (healthcare, RIT global) → industry connections
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_ini_137, v_uni_ini_12);  -- hands-on Math sessions → innovative teaching models
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_ini_138, v_uni_ini_13);  -- promote recent teaching developments → new advances in courses
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_ini_139, v_uni_ini_3);   -- collaborate with schools → industry/external connections
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_ini_140, v_uni_ini_2);   -- cross-dept colleagues presenting in MaS → faculty PD opportunities
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_ini_141, v_uni_ini_6);   -- faculty in student-for-a-day → target freshman students
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_ini_142, v_uni_ini_3);   -- industrial visits → industry connections

    -- GOAL 3: faculty training (obj 111)
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_ini_143, v_uni_ini_2);   -- FDC collaboration, 1 dev activity per AY → faculty PD opportunities
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_ini_144, v_uni_ini_2);   -- advisory board guidance for faculty dev → faculty PD opportunities

    -- GOAL 4: competitions and clubs (obj 112)
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_ini_145, v_uni_ini_6);   -- competitions among schools → target freshman students
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_ini_146, v_uni_ini_6);   -- collaboration with students union/ASC → target freshman students
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_ini_147, v_uni_ini_6);   -- MATH club formation → target freshman students
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_ini_148, v_uni_ini_6);   -- internal MaS competitions → target freshman students

    -- GOAL 5: sharing activities (obj 113)
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_ini_149, v_uni_ini_12);  -- GLEC representation → innovative teaching models (sharing innovation)
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_ini_150, v_uni_ini_10);  -- faculty presenting in dept meetings → engage students with faculty
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_ini_151, v_uni_ini_12);  -- MaS LinkedIn page → innovative teaching models (showcasing)
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_ini_152, v_uni_ini_12);  -- MaS website page → innovative teaching models
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_ini_153, v_uni_ini_1);   -- faculty in open days → alumni/community engagement

    -- GOAL 6: quality of teaching and learning (obj 114)
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_ini_154, v_uni_ini_13);  -- CLO enhancement with NY → strategy for new advances in courses
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_ini_155, v_uni_ini_4);   -- meetings with dept chairs → establish academic development unit
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_ini_156, v_uni_ini_4);   -- teaching quality in faculty hiring → academic development unit
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_ini_157, v_uni_ini_2);   -- faculty workshops on teaching/learning → faculty PD opportunities
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_ini_158, v_uni_ini_11);  -- new teaching methods with technology → increase use of IT
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_ini_159, v_uni_ini_5);   -- ASC peer support → early alert + ASC connection
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_ini_160, v_uni_ini_5);   -- comms with advising for at-risk students → early alert + advising
    INSERT INTO initiative_mapping (dept_initiative_id, university_initiative_id) VALUES (v_ini_161, v_uni_ini_13);  -- promote PLO assessment importance → strategy for new advances

    RAISE NOTICE 'V9: MAS objective mappings (10) and initiative mappings (31) inserted successfully.';
END $$;
