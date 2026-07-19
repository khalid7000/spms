-- Replace Faculty's placeholder criteria with the real rubric-driven criteria from
-- "Faculty Evaluation Rubrics Sheet" (Sections 1-3), each carrying the data the faculty member
-- must actually provide per "Faculty and Academic Unit Head Self-Assessment of Performance".

-- ── Teaching: remove the old 4 placeholder criteria, replace with the rubric's 6 ───────────────
DELETE FROM category_criteria
WHERE category_id IN (
    SELECT pc.id FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
    WHERE pc.category_name = 'Teaching' AND et.title_name = 'Faculty'
);

INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT pc.id, 'Classroom Performance',
'Provide your Teaching Effectiveness and Classroom Performance Statement, including any laboratory development activities. Include courses taught (course title/number, contact hours, number of students, and student evaluation results) and course performance evaluation per semester.',
1
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE pc.category_name = 'Teaching' AND et.title_name = 'Faculty';

INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT pc.id, 'Academic Integrity Policies',
'Describe your philosophy, application, and achievements this academic year regarding academic integrity policies -- enforcement, classroom/exam management, and any technology tools used (e.g. Turnitin).',
2
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE pc.category_name = 'Teaching' AND et.title_name = 'Faculty';

INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT pc.id, 'Course Portfolio & Relevant Activities',
'Confirm whether all required course files/portfolios (special materials such as textbooks, software, manuals, websites; assessment quality and coverage; CLO assessment; sharing) were submitted by the deadline with no missing components. If any files were late or incomplete, provide details explaining what was missing and why.',
3
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE pc.category_name = 'Teaching' AND et.title_name = 'Faculty';

INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT pc.id, 'Course and Program Assessment',
'Describe your achievements this academic year in course and program assessment: assessment data developed and shared with the Assessment Committee, how assessment results were used to close the loop, and relevant feedback reported in the CAR.',
4
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE pc.category_name = 'Teaching' AND et.title_name = 'Faculty';

INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT pc.id, 'Advising and Availability to Help Students',
'Provide your graduate advising activity (student name and project title per semester), whether you participated in undergraduate advising (Y/N) and approximately how many students you regularly advise, and describe your availability to students (office hours, responsiveness).',
5
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE pc.category_name = 'Teaching' AND et.title_name = 'Faculty';

INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT pc.id, 'Extramural Education',
'Describe independent studies supervised, professional development undertaken in support of teaching, extramural education activities organized (field trips, invited speakers, seminars, internships, study tours, student competitions -- include artifacts such as seminar posters or competition brochures), and any other relevant activity.',
6
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE pc.category_name = 'Teaching' AND et.title_name = 'Faculty';

-- ── Research: standard Section 2 (3 criteria) ───────────────────────────────────────────────────
INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT pc.id, 'Publications and Patents',
'List all publications that appeared in print this year (peer-reviewed, non-peer-reviewed, book chapters, conference proceedings -- include paper title, journal/venue name, co-authors, and date published), plus publications in press (accepted but not yet published) and publications under review (journal name, co-authors, submission date). Include invention disclosures and patent applications. Attach related artifacts (e.g. published paper) where available.',
1
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE pc.category_name = 'Research' AND et.title_name = 'Faculty';

INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT pc.id, 'Professional Consulting or Funded Research',
'List research proposals submitted (title, agency, co-investigators, amount) and research grants awarded (title, granting agency, co-investigators, amount). Describe professional consulting projects undertaken, including any students engaged in that work.',
2
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE pc.category_name = 'Research' AND et.title_name = 'Faculty';

INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT pc.id, 'Conference Participation',
'Describe presentations at professional meetings (invited talks, other talks/posters/abstracts) and conference attendance funded through professional development resources. Note whether findings were shared with colleagues upon return.',
3
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE pc.category_name = 'Research' AND et.title_name = 'Faculty';

-- ── Service (3 criteria) ─────────────────────────────────────────────────────────────────────────
INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT pc.id, 'Program Service',
'Describe your service contributions to your academic program/department this year: committee appointments within the program, student recruitment activities, and -- for department chairs -- academic unit goals achievement, guidance/support provided to faculty and staff, leadership in program/curriculum review and accreditation, representing the university and academic unit, and departmental planning/reporting.',
1
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE pc.category_name = 'Service' AND et.title_name = 'Faculty';

INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT pc.id, 'Institutional Service',
'Describe your university-level service contributions this year: university service activities and committee appointments at the institutional level.',
2
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE pc.category_name = 'Service' AND et.title_name = 'Faculty';

INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT pc.id, 'Service to Community and Profession',
'Describe your service to the community and profession: professional organizations service, industrial interaction, community services, serving as a reviewer for scholarly journals/conference submissions or for grants, and outreach activities.',
3
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE pc.category_name = 'Service' AND et.title_name = 'Faculty';
