-- Seeds the three-level evaluation rubric (Unsatisfactory / Meets Expectations / Exceeds
-- Expectations) for every Faculty and Research Faculty criteria, transcribed from the
-- "Faculty Annual Evaluation Rubrics Sheet" (RIT Dubai). Teaching and Service rubrics are
-- identical for Faculty and Research Faculty (Research Faculty copied those categories verbatim
-- in V44); Research differs -- Faculty uses the standard 3-criteria Section 2, Research Faculty
-- uses the single-criterion "Alternative Section 2 with Research Course Release".

-- ── Teaching (applies to both Faculty and Research Faculty) ─────────────────────────────────

UPDATE category_criteria cc SET
    rubric_unsatisfactory = 'Student evaluations and comments indicate problems in material delivery; classroom discipline issues; disrespect to students; excessive absences. Student evaluations are well below averages. Grading is not fair within the course or is inconsistent with the norm. Course grades are somewhat inflated. Professor is not interested in developing new course/lab material that leads to enhancing the student experience.',
    rubric_meets_expectations = 'Student evaluations and comments are positive. Student evaluations are at or slightly above the University average for the level of courses. No indication of substantial deficiencies in instructional duties. Student grades are not inflated. Professor engages in some development of course/lab material that leads to enhancing the learning experience.',
    rubric_exceeds_expectations = 'Student evaluations and comments are abundant and overwhelmingly positive, with no indication of deficiencies in classroom instructional activities. Student evaluations are well above average. Student grades are not inflated. Significant work is done in introducing new course/lab material and technologies that leads to an enhanced student class experience.'
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE cc.category_id = pc.id AND pc.category_name = 'Teaching' AND et.title_name IN ('Faculty', 'Research Faculty')
    AND cc.criteria_name = 'Classroom Performance';

UPDATE category_criteria cc SET
    rubric_unsatisfactory = 'Professor is not enforcing, is not pro-active enough, or is unreasonably zealous in enforcing academic integrity policies. Cheating and plagiarism incidents are not properly addressed by the professor. Professor does not practice good classroom and exam management.',
    rubric_meets_expectations = 'Academic integrity policies are enforced and respected in the classroom. Professor is diligent in addressing academic integrity violations as they emerge, and practices proper classroom and exam management.',
    rubric_exceeds_expectations = 'Professor is active in educating students about academic integrity and is reasonable and equitable in applying the policies. Professor exercises excellent classroom management skills and utilizes technology tools such as Turnitin to combat plagiarism.'
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE cc.category_id = pc.id AND pc.category_name = 'Teaching' AND et.title_name IN ('Faculty', 'Research Faculty')
    AND cc.criteria_name = 'Academic Integrity Policies';

UPDATE category_criteria cc SET
    rubric_unsatisfactory = 'Course activities are not balanced, and course portfolios are lacking in terms of content. Grading policies are not stated in the syllabus or are stated but not adhered to. Feedback to students is deficient. Course requires some improvement in terms of instructional material, frequency and quality of assignments, or course grade distribution. Course design and assessment tools need to be better aligned with course objectives.',
    rubric_meets_expectations = 'Course content and activities are indicative of a complete and balanced course. Course grades are adequately distributed among course activities (assignments, exams, projects, etc). Feedback to students is adequate and timely. New material is developed, consistent with the program goals.',
    rubric_exceeds_expectations = 'Course content and activities are superior in terms of instructional material, frequency and quality of assignments, and course grade distribution. Course design and assessment tools are fully consistent with course objectives. Feedback is abundant and uses multiple available channels, e.g. myCourses, EarlyAlert, etc.'
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE cc.category_id = pc.id AND pc.category_name = 'Teaching' AND et.title_name IN ('Faculty', 'Research Faculty')
    AND cc.criteria_name = 'Course Portfolio & Relevant Activities';

UPDATE category_criteria cc SET
    rubric_unsatisfactory = 'Assessment of course objectives reflects poor mapping and marginal interest in program improvement. Assessment forms (e.g., CAR) need revision to meet acceptable standards. Assessment tools are limited and do not have adequate coverage of all course outcomes.',
    rubric_meets_expectations = 'Assessment data on the courses and program are developed and shared with the Assessment Committee. Assessment results are directly used to close the loop by introducing improvements in the next cycle. Adequate relevant feedback is reported in the CAR and is utilized to close the loop.',
    rubric_exceeds_expectations = 'Professor develops and shares innovative assessment tools with other faculty members. Professor frequently contributes to program assessment. Assessment tools are clearly utilized to improve the program. Adequate relevant feedback is reported in the CAR and is consistently utilized to close the loop.'
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE cc.category_id = pc.id AND pc.category_name = 'Teaching' AND et.title_name IN ('Faculty', 'Research Faculty')
    AND cc.criteria_name = 'Course and Program Assessment';

UPDATE category_criteria cc SET
    rubric_unsatisfactory = 'Professor''s absence from campus is noticeable. Student feedback indicates erratic availability especially during office hours. Student advising is minimal.',
    rubric_meets_expectations = 'Professor is available during office hours and, in general, during the day. No significant student complaints indicating lack of availability or student advising.',
    rubric_exceeds_expectations = 'Professor goes out of way to help students in and out of the classroom, as evidenced by student evaluations, student advising, and presence on campus.'
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE cc.category_id = pc.id AND pc.category_name = 'Teaching' AND et.title_name IN ('Faculty', 'Research Faculty')
    AND cc.criteria_name = 'Advising and Availability to Help Students';

UPDATE category_criteria cc SET
    rubric_unsatisfactory = 'Professor''s interest in extramural education is minimal. Professor does not engage, support or promote education outside the classroom. Professor is not interested in engaging with students in opportunities for professional development possibly through an approved independent study.',
    rubric_meets_expectations = 'Professor actively supports extramural learning and organizes a couple of extramural educational activities for students during the semester. Activities may include field trips, invited speakers, approved independent study course, or student competitions.',
    rubric_exceeds_expectations = 'Professor is very active in promoting learning beyond the classroom and organizes multiple activities such as field trips, seminars, internships, and study tours. Professor publishes at, or attends, engineering education conferences.'
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE cc.category_id = pc.id AND pc.category_name = 'Teaching' AND et.title_name IN ('Faculty', 'Research Faculty')
    AND cc.criteria_name = 'Extramural Education';

-- ── Research: Faculty (standard Section 2) ──────────────────────────────────────────────────

UPDATE category_criteria cc SET
    rubric_unsatisfactory = 'Faculty member did not publish or engage in any form of scholarly or creative activity.',
    rubric_meets_expectations = 'Faculty member presents scholarly or creative work at a local or international conference, self-funded or funded through professional development resources.',
    rubric_exceeds_expectations = 'Faculty member publishes one high-quality peer-reviewed journal paper as specified by the department or present/publish two conference papers. Or: Faculty member conducts research and/or writes material under contract leading to publication of a book or textbook as author, co-author, or editor. Or: Faculty member publishes a book or book-length project (scholarly monograph, collection of poems, three short fictions, novella, novel, or produced play) as author, co-author, or editor, subject to review and approval of Department Chair. From faculty producing creative work, department chairs can accept placement of 3 poems or one short story as equivalent to a published article, so long as the venues reflect a high standard of selection and review as specified by the department.'
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE cc.category_id = pc.id AND pc.category_name = 'Research' AND et.title_name = 'Faculty'
    AND cc.criteria_name = 'Publications and Patents';

UPDATE category_criteria cc SET
    rubric_unsatisfactory = 'Faculty member does not engage in any form of professional consulting, funded research, or other commissioned work.',
    rubric_meets_expectations = 'Faculty member utilizes internally-funded research grants or commission to conduct research or creative work leading to publication, and engages student(s) in that work. Or: Faculty member engages in one or two professional consulting projects and shares the findings with or engages students in such activities.',
    rubric_exceeds_expectations = 'Faculty member secures externally funded grants in excess of AED 10,000 to support self/students. Or: Faculty member conducts multiple professional consulting activities, participates in productive retreats, organizes and contributes to public readings, or fulfills a residency, and engages students in such activities.'
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE cc.category_id = pc.id AND pc.category_name = 'Research' AND et.title_name = 'Faculty'
    AND cc.criteria_name = 'Professional Consulting or Funded Research';

UPDATE category_criteria cc SET
    rubric_unsatisfactory = 'Faculty member does not make use of available professional development opportunities funded by the university.',
    rubric_meets_expectations = 'Faculty member takes advantages of available professional development resources to attend local and international conferences and present work.',
    rubric_exceeds_expectations = 'Faculty member takes advantage of available professional development resources to attend conferences and presents work, shares the findings with colleagues upon return, and attends additional conferences as invited guest or through self-sponsorship.'
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE cc.category_id = pc.id AND pc.category_name = 'Research' AND et.title_name = 'Faculty'
    AND cc.criteria_name = 'Conference Participation';

-- ── Research: Research Faculty (Alternative Section 2 with Research Course Release) ─────────

UPDATE category_criteria cc SET
    rubric_unsatisfactory = 'Faculty member did not publish or engage in any form of scholarly activities.',
    rubric_meets_expectations = 'Faculty member publishes one high-quality peer-reviewed journal paper as specified by the department or present/publish two conference papers. Or: Faculty member conducts research and/or writes material under contract leading to publication of a book or textbook as author, co-author, or editor. Or: Faculty member publishes a book or book-length project (scholarly monograph, collection of poems, three short fictions, novella, novel, or produced play) as author, co-author, or editor, subject to review and approval of Department Chair. From faculty producing creative work, department chairs can accept placement of 3 poems or one short story as equivalent to a published article, so long as the venues reflect a high standard of selection and review as specified by the department.',
    rubric_exceeds_expectations = 'Faculty member performs more than 2 of the actions from column 2, as adjudicated by Department Chair.'
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE cc.category_id = pc.id AND pc.category_name = 'Research' AND et.title_name = 'Research Faculty'
    AND cc.criteria_name = 'Publications and Patents';

-- ── Service (applies to both Faculty and Research Faculty) ──────────────────────────────────

UPDATE category_criteria cc SET
    rubric_unsatisfactory = 'Faculty member does not fulfill service duties as assigned or are done with lack of timeliness and/or proper communication with colleagues and superiors. Service to the program needs significant improvement in terms of quality and timeliness.',
    rubric_meets_expectations = 'Faculty member is proactive in conducting and concluding assigned duties on time and according to expected quality. Faculty member communicates properly and shares information with colleagues.',
    rubric_exceeds_expectations = 'Faculty member is proactively going beyond assigned duties to serve the Program. Faculty member always shares relevant information with colleagues and demonstrates genuine concern for the well-being of the Program. Impact of such service with the students, the faculty colleagues, any agency like the ministry, the media, etc. can be used as evidence.'
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE cc.category_id = pc.id AND pc.category_name = 'Service' AND et.title_name IN ('Faculty', 'Research Faculty')
    AND cc.criteria_name = 'Program Service';

UPDATE category_criteria cc SET
    rubric_unsatisfactory = 'Faculty member does not fulfill service duties as assigned or are done with lack of timeliness and/or proper communication with colleagues and superiors. Service to the institution needs significant improvement in terms of quality and timeliness.',
    rubric_meets_expectations = 'Faculty member is proactive in conducting and concluding assigned duties on time and according to expected quality. Faculty member communicates properly and shares information with colleagues.',
    rubric_exceeds_expectations = 'Faculty member is proactively going beyond assigned duties to serve the University. Faculty member always shares relevant information with colleagues and demonstrates genuine concern for the well-being of the university. Impact of such service with the students, the faculty colleagues, any agency like the ministry, the media, etc. can be used as evidence.'
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE cc.category_id = pc.id AND pc.category_name = 'Service' AND et.title_name IN ('Faculty', 'Research Faculty')
    AND cc.criteria_name = 'Institutional Service';

UPDATE category_criteria cc SET
    rubric_unsatisfactory = 'Faculty member does not engage in service to the community or the profession, and does not seek to participate in such activities.',
    rubric_meets_expectations = 'Faculty member is involved in three to five community and/or professional service activities. These include serving as a reviewer for journals and conference articles, participating in technical committees of local and/or international societies, chairing conference sessions, conducting interviews with the media, involvement in charitable activities, or serving on business councils and advisory boards.',
    rubric_exceeds_expectations = 'Faculty member is highly active in community and/or professional service, shares the opportunities with colleagues, and engages the service of other faculty members in such activities. Impact of such service with the students, the faculty colleagues, any agency like the ministry, the media, etc. can be used as evidence.'
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE cc.category_id = pc.id AND pc.category_name = 'Service' AND et.title_name IN ('Faculty', 'Research Faculty')
    AND cc.criteria_name = 'Service to Community and Profession';
