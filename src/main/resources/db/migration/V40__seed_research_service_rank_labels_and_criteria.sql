-- Give Faculty's Research and Service categories the same rank-label (1-5) and criteria setup
-- already seeded for Teaching in V38, adapted to each category's subject matter.

-- ── Research: rank labels ───────────────────────────────────────────────────
INSERT INTO category_rank_label (category_id, rank, label, description)
SELECT id, 1, 'Below Expectations', 'Does not meet minimum research productivity standards'
FROM portfolio_category WHERE category_name = 'Research' AND id IN (SELECT id FROM portfolio_category WHERE title_id IN (SELECT id FROM employee_title WHERE title_name = 'Faculty') LIMIT 1);

INSERT INTO category_rank_label (category_id, rank, label, description)
SELECT id, 2, 'Needs Improvement', 'Some areas of scholarly activity require development'
FROM portfolio_category WHERE category_name = 'Research' AND id IN (SELECT id FROM portfolio_category WHERE title_id IN (SELECT id FROM employee_title WHERE title_name = 'Faculty') LIMIT 1);

INSERT INTO category_rank_label (category_id, rank, label, description)
SELECT id, 3, 'Meets Expectations', 'Consistently demonstrates effective research productivity'
FROM portfolio_category WHERE category_name = 'Research' AND id IN (SELECT id FROM portfolio_category WHERE title_id IN (SELECT id FROM employee_title WHERE title_name = 'Faculty') LIMIT 1);

INSERT INTO category_rank_label (category_id, rank, label, description)
SELECT id, 4, 'Exceeds Expectations', 'Demonstrates outstanding research productivity and impact'
FROM portfolio_category WHERE category_name = 'Research' AND id IN (SELECT id FROM portfolio_category WHERE title_id IN (SELECT id FROM employee_title WHERE title_name = 'Faculty') LIMIT 1);

INSERT INTO category_rank_label (category_id, rank, label, description)
SELECT id, 5, 'Outstanding', 'Exemplary research excellence with measurable impact on the field'
FROM portfolio_category WHERE category_name = 'Research' AND id IN (SELECT id FROM portfolio_category WHERE title_id IN (SELECT id FROM employee_title WHERE title_name = 'Faculty') LIMIT 1);

-- ── Research: criteria ──────────────────────────────────────────────────────
INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT id, 'Publication Output', 'Peer-reviewed journal articles, conference papers, and other scholarly publications', 1
FROM portfolio_category WHERE category_name = 'Research' AND id IN (SELECT id FROM portfolio_category WHERE title_id IN (SELECT id FROM employee_title WHERE title_name = 'Faculty') LIMIT 1);

INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT id, 'Research Funding & Grants', 'Securing and managing external funding and grant-supported projects', 2
FROM portfolio_category WHERE category_name = 'Research' AND id IN (SELECT id FROM portfolio_category WHERE title_id IN (SELECT id FROM employee_title WHERE title_name = 'Faculty') LIMIT 1);

INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT id, 'Impact & Dissemination', 'Citations, invited talks, and dissemination of research to the field', 3
FROM portfolio_category WHERE category_name = 'Research' AND id IN (SELECT id FROM portfolio_category WHERE title_id IN (SELECT id FROM employee_title WHERE title_name = 'Faculty') LIMIT 1);

INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT id, 'Collaboration & Mentorship', 'Collaborative research partnerships and mentoring of students or junior researchers', 4
FROM portfolio_category WHERE category_name = 'Research' AND id IN (SELECT id FROM portfolio_category WHERE title_id IN (SELECT id FROM employee_title WHERE title_name = 'Faculty') LIMIT 1);

-- ── Service: rank labels ─────────────────────────────────────────────────────
INSERT INTO category_rank_label (category_id, rank, label, description)
SELECT id, 1, 'Below Expectations', 'Does not meet minimum service expectations'
FROM portfolio_category WHERE category_name = 'Service' AND id IN (SELECT id FROM portfolio_category WHERE title_id IN (SELECT id FROM employee_title WHERE title_name = 'Faculty') LIMIT 1);

INSERT INTO category_rank_label (category_id, rank, label, description)
SELECT id, 2, 'Needs Improvement', 'Some areas of service contribution require development'
FROM portfolio_category WHERE category_name = 'Service' AND id IN (SELECT id FROM portfolio_category WHERE title_id IN (SELECT id FROM employee_title WHERE title_name = 'Faculty') LIMIT 1);

INSERT INTO category_rank_label (category_id, rank, label, description)
SELECT id, 3, 'Meets Expectations', 'Consistently demonstrates effective service contributions'
FROM portfolio_category WHERE category_name = 'Service' AND id IN (SELECT id FROM portfolio_category WHERE title_id IN (SELECT id FROM employee_title WHERE title_name = 'Faculty') LIMIT 1);

INSERT INTO category_rank_label (category_id, rank, label, description)
SELECT id, 4, 'Exceeds Expectations', 'Demonstrates outstanding service contributions and leadership'
FROM portfolio_category WHERE category_name = 'Service' AND id IN (SELECT id FROM portfolio_category WHERE title_id IN (SELECT id FROM employee_title WHERE title_name = 'Faculty') LIMIT 1);

INSERT INTO category_rank_label (category_id, rank, label, description)
SELECT id, 5, 'Outstanding', 'Exemplary service excellence with measurable impact on university, profession, and community'
FROM portfolio_category WHERE category_name = 'Service' AND id IN (SELECT id FROM portfolio_category WHERE title_id IN (SELECT id FROM employee_title WHERE title_name = 'Faculty') LIMIT 1);

-- ── Service: criteria ────────────────────────────────────────────────────────
INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT id, 'University Service', 'Active participation in committees, governance, and institutional initiatives', 1
FROM portfolio_category WHERE category_name = 'Service' AND id IN (SELECT id FROM portfolio_category WHERE title_id IN (SELECT id FROM employee_title WHERE title_name = 'Faculty') LIMIT 1);

INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT id, 'Professional Service', 'Contributions to professional organizations, editorial boards, or peer review', 2
FROM portfolio_category WHERE category_name = 'Service' AND id IN (SELECT id FROM portfolio_category WHERE title_id IN (SELECT id FROM employee_title WHERE title_name = 'Faculty') LIMIT 1);

INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT id, 'Community Engagement', 'Outreach and engagement activities benefiting the broader community', 3
FROM portfolio_category WHERE category_name = 'Service' AND id IN (SELECT id FROM portfolio_category WHERE title_id IN (SELECT id FROM employee_title WHERE title_name = 'Faculty') LIMIT 1);

INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT id, 'Leadership & Initiative', 'Taking on leadership roles and initiating impactful service activities', 4
FROM portfolio_category WHERE category_name = 'Service' AND id IN (SELECT id FROM portfolio_category WHERE title_id IN (SELECT id FROM employee_title WHERE title_name = 'Faculty') LIMIT 1);
