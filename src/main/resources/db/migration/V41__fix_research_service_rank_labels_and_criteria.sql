-- V40 was a no-op: its "id IN (SELECT id FROM portfolio_category WHERE title_id IN (...) LIMIT 1)"
-- clause resolves to a single arbitrary category under Faculty (in practice, Teaching's row,
-- the lowest id) regardless of which category_name the outer query asked for -- so it never
-- matched Research or Service and inserted nothing. Re-seed here with the correct, direct
-- predicate (category_name + title_id, no bogus indirection). V40 is left in place unedited
-- since it's already applied history and was harmless (zero rows affected either way).

-- ── Research: rank labels ───────────────────────────────────────────────────
INSERT INTO category_rank_label (category_id, rank, label, description)
SELECT pc.id, 1, 'Below Expectations', 'Does not meet minimum research productivity standards'
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE pc.category_name = 'Research' AND et.title_name = 'Faculty';

INSERT INTO category_rank_label (category_id, rank, label, description)
SELECT pc.id, 2, 'Needs Improvement', 'Some areas of scholarly activity require development'
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE pc.category_name = 'Research' AND et.title_name = 'Faculty';

INSERT INTO category_rank_label (category_id, rank, label, description)
SELECT pc.id, 3, 'Meets Expectations', 'Consistently demonstrates effective research productivity'
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE pc.category_name = 'Research' AND et.title_name = 'Faculty';

INSERT INTO category_rank_label (category_id, rank, label, description)
SELECT pc.id, 4, 'Exceeds Expectations', 'Demonstrates outstanding research productivity and impact'
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE pc.category_name = 'Research' AND et.title_name = 'Faculty';

INSERT INTO category_rank_label (category_id, rank, label, description)
SELECT pc.id, 5, 'Outstanding', 'Exemplary research excellence with measurable impact on the field'
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE pc.category_name = 'Research' AND et.title_name = 'Faculty';

-- ── Research: criteria ──────────────────────────────────────────────────────
INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT pc.id, 'Publication Output', 'Peer-reviewed journal articles, conference papers, and other scholarly publications', 1
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE pc.category_name = 'Research' AND et.title_name = 'Faculty';

INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT pc.id, 'Research Funding & Grants', 'Securing and managing external funding and grant-supported projects', 2
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE pc.category_name = 'Research' AND et.title_name = 'Faculty';

INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT pc.id, 'Impact & Dissemination', 'Citations, invited talks, and dissemination of research to the field', 3
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE pc.category_name = 'Research' AND et.title_name = 'Faculty';

INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT pc.id, 'Collaboration & Mentorship', 'Collaborative research partnerships and mentoring of students or junior researchers', 4
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE pc.category_name = 'Research' AND et.title_name = 'Faculty';

-- ── Service: rank labels ─────────────────────────────────────────────────────
INSERT INTO category_rank_label (category_id, rank, label, description)
SELECT pc.id, 1, 'Below Expectations', 'Does not meet minimum service expectations'
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE pc.category_name = 'Service' AND et.title_name = 'Faculty';

INSERT INTO category_rank_label (category_id, rank, label, description)
SELECT pc.id, 2, 'Needs Improvement', 'Some areas of service contribution require development'
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE pc.category_name = 'Service' AND et.title_name = 'Faculty';

INSERT INTO category_rank_label (category_id, rank, label, description)
SELECT pc.id, 3, 'Meets Expectations', 'Consistently demonstrates effective service contributions'
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE pc.category_name = 'Service' AND et.title_name = 'Faculty';

INSERT INTO category_rank_label (category_id, rank, label, description)
SELECT pc.id, 4, 'Exceeds Expectations', 'Demonstrates outstanding service contributions and leadership'
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE pc.category_name = 'Service' AND et.title_name = 'Faculty';

INSERT INTO category_rank_label (category_id, rank, label, description)
SELECT pc.id, 5, 'Outstanding', 'Exemplary service excellence with measurable impact on university, profession, and community'
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE pc.category_name = 'Service' AND et.title_name = 'Faculty';

-- ── Service: criteria ────────────────────────────────────────────────────────
INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT pc.id, 'University Service', 'Active participation in committees, governance, and institutional initiatives', 1
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE pc.category_name = 'Service' AND et.title_name = 'Faculty';

INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT pc.id, 'Professional Service', 'Contributions to professional organizations, editorial boards, or peer review', 2
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE pc.category_name = 'Service' AND et.title_name = 'Faculty';

INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT pc.id, 'Community Engagement', 'Outreach and engagement activities benefiting the broader community', 3
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE pc.category_name = 'Service' AND et.title_name = 'Faculty';

INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT pc.id, 'Leadership & Initiative', 'Taking on leadership roles and initiating impactful service activities', 4
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE pc.category_name = 'Service' AND et.title_name = 'Faculty';
