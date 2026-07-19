-- Seed Default Portfolio Categories for Faculty and Manager/Director titles

-- Create Faculty title
INSERT INTO employee_title (title_name, is_system_default)
VALUES ('Faculty', true);

-- Create Manager/Director titles  
INSERT INTO employee_title (title_name, is_system_default)
VALUES ('Manager', true);

INSERT INTO employee_title (title_name, is_system_default)
VALUES ('Director', true);

-- Faculty Categories: Teaching, Research, Service
INSERT INTO portfolio_category (title_id, category_name, description, sort_order, is_system_default)
SELECT id, 'Teaching', 'Excellence in classroom instruction, course development, and student learning outcomes', 1, true
FROM employee_title WHERE title_name = 'Faculty';

INSERT INTO portfolio_category (title_id, category_name, description, sort_order, is_system_default)
SELECT id, 'Research', 'Scholarly activity, research productivity, and advancement of knowledge', 2, true
FROM employee_title WHERE title_name = 'Faculty';

INSERT INTO portfolio_category (title_id, category_name, description, sort_order, is_system_default)
SELECT id, 'Service', 'Contribution to university, profession, and community', 3, true
FROM employee_title WHERE title_name = 'Faculty';

-- Manager Categories: Strategic Thinker, Lead by Example, Business & Financial Acumen
INSERT INTO portfolio_category (title_id, category_name, description, sort_order, is_system_default)
SELECT id, 'Strategic Thinker', 'Ability to align work with organizational vision and plan for future success', 1, true
FROM employee_title WHERE title_name = 'Manager';

INSERT INTO portfolio_category (title_id, category_name, description, sort_order, is_system_default)
SELECT id, 'Lead by Example', 'Demonstrates integrity, sets positive tone, and motivates team through actions', 2, true
FROM employee_title WHERE title_name = 'Manager';

INSERT INTO portfolio_category (title_id, category_name, description, sort_order, is_system_default)
SELECT id, 'Business & Financial Acumen', 'Understanding of business drivers, fiscal responsibility, and operational efficiency', 3, true
FROM employee_title WHERE title_name = 'Manager';

-- Director Categories: Same as Manager (for now)
INSERT INTO portfolio_category (title_id, category_name, description, sort_order, is_system_default)
SELECT id, 'Strategic Thinker', 'Ability to align work with organizational vision and plan for future success', 1, true
FROM employee_title WHERE title_name = 'Director';

INSERT INTO portfolio_category (title_id, category_name, description, sort_order, is_system_default)
SELECT id, 'Lead by Example', 'Demonstrates integrity, sets positive tone, and motivates team through actions', 2, true
FROM employee_title WHERE title_name = 'Director';

INSERT INTO portfolio_category (title_id, category_name, description, sort_order, is_system_default)
SELECT id, 'Business & Financial Acumen', 'Understanding of business drivers, fiscal responsibility, and operational efficiency', 3, true
FROM employee_title WHERE title_name = 'Director';

-- Seed rank labels for all categories (1=Below Expectations, 2=Needs Improvement, 3=Meets Expectations, 4=Exceeds Expectations, 5=Outstanding)
-- Faculty - Teaching
INSERT INTO category_rank_label (category_id, rank, label, description)
SELECT id, 1, 'Below Expectations', 'Does not meet minimum teaching standards'
FROM portfolio_category WHERE category_name = 'Teaching' AND id IN (SELECT id FROM portfolio_category WHERE title_id IN (SELECT id FROM employee_title WHERE title_name = 'Faculty') LIMIT 1);

INSERT INTO category_rank_label (category_id, rank, label, description)
SELECT id, 2, 'Needs Improvement', 'Some areas of teaching require development'
FROM portfolio_category WHERE category_name = 'Teaching' AND id IN (SELECT id FROM portfolio_category WHERE title_id IN (SELECT id FROM employee_title WHERE title_name = 'Faculty') LIMIT 1);

INSERT INTO category_rank_label (category_id, rank, label, description)
SELECT id, 3, 'Meets Expectations', 'Consistently demonstrates effective teaching practices'
FROM portfolio_category WHERE category_name = 'Teaching' AND id IN (SELECT id FROM portfolio_category WHERE title_id IN (SELECT id FROM employee_title WHERE title_name = 'Faculty') LIMIT 1);

INSERT INTO category_rank_label (category_id, rank, label, description)
SELECT id, 4, 'Exceeds Expectations', 'Demonstrates outstanding teaching skills and innovation'
FROM portfolio_category WHERE category_name = 'Teaching' AND id IN (SELECT id FROM portfolio_category WHERE title_id IN (SELECT id FROM employee_title WHERE title_name = 'Faculty') LIMIT 1);

INSERT INTO category_rank_label (category_id, rank, label, description)
SELECT id, 5, 'Outstanding', 'Exemplary teaching excellence with measurable impact on student learning'
FROM portfolio_category WHERE category_name = 'Teaching' AND id IN (SELECT id FROM portfolio_category WHERE title_id IN (SELECT id FROM employee_title WHERE title_name = 'Faculty') LIMIT 1);

-- Seed default criteria for Teaching category
INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT id, 'Course Organization', 'Clear structure, learning outcomes, and assessment methods', 1
FROM portfolio_category WHERE category_name = 'Teaching' AND id IN (SELECT id FROM portfolio_category WHERE title_id IN (SELECT id FROM employee_title WHERE title_name = 'Faculty') LIMIT 1);

INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT id, 'Student Engagement', 'Active learning, interaction, and inclusivity in classroom', 2
FROM portfolio_category WHERE category_name = 'Teaching' AND id IN (SELECT id FROM portfolio_category WHERE title_id IN (SELECT id FROM employee_title WHERE title_name = 'Faculty') LIMIT 1);

INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT id, 'Assessment & Feedback', 'Effective assessment strategies and meaningful student feedback', 3
FROM portfolio_category WHERE category_name = 'Teaching' AND id IN (SELECT id FROM portfolio_category WHERE title_id IN (SELECT id FROM employee_title WHERE title_name = 'Faculty') LIMIT 1);

INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT id, 'Continuous Improvement', 'Incorporation of feedback and course improvements over time', 4
FROM portfolio_category WHERE category_name = 'Teaching' AND id IN (SELECT id FROM portfolio_category WHERE title_id IN (SELECT id FROM employee_title WHERE title_name = 'Faculty') LIMIT 1);
