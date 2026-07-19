-- Chair/Dean/Provost are legitimate title strings already in use (department/org-group head
-- titles) but were missing from employee_title until the admin console's auto-sync created them
-- on first visit to Portfolio Category Management. Guard the insert in case that already happened
-- on this database (or a fresh database where it hasn't yet) -- either way, ensure the titles
-- exist before seeding their categories.
INSERT INTO employee_title (title_name, is_system_default)
SELECT 'Chair', true WHERE NOT EXISTS (SELECT 1 FROM employee_title WHERE title_name = 'Chair');

INSERT INTO employee_title (title_name, is_system_default)
SELECT 'Dean', true WHERE NOT EXISTS (SELECT 1 FROM employee_title WHERE title_name = 'Dean');

INSERT INTO employee_title (title_name, is_system_default)
SELECT 'Provost', true WHERE NOT EXISTS (SELECT 1 FROM employee_title WHERE title_name = 'Provost');

-- Same leadership categories already seeded for Manager/Director (V38): Strategic Thinker,
-- Lead by Example, Business & Financial Acumen.
INSERT INTO portfolio_category (title_id, category_name, description, sort_order, is_system_default)
SELECT id, 'Strategic Thinker', 'Ability to align work with organizational vision and plan for future success', 1, true
FROM employee_title WHERE title_name = 'Chair';

INSERT INTO portfolio_category (title_id, category_name, description, sort_order, is_system_default)
SELECT id, 'Lead by Example', 'Demonstrates integrity, sets positive tone, and motivates team through actions', 2, true
FROM employee_title WHERE title_name = 'Chair';

INSERT INTO portfolio_category (title_id, category_name, description, sort_order, is_system_default)
SELECT id, 'Business & Financial Acumen', 'Understanding of business drivers, fiscal responsibility, and operational efficiency', 3, true
FROM employee_title WHERE title_name = 'Chair';

INSERT INTO portfolio_category (title_id, category_name, description, sort_order, is_system_default)
SELECT id, 'Strategic Thinker', 'Ability to align work with organizational vision and plan for future success', 1, true
FROM employee_title WHERE title_name = 'Dean';

INSERT INTO portfolio_category (title_id, category_name, description, sort_order, is_system_default)
SELECT id, 'Lead by Example', 'Demonstrates integrity, sets positive tone, and motivates team through actions', 2, true
FROM employee_title WHERE title_name = 'Dean';

INSERT INTO portfolio_category (title_id, category_name, description, sort_order, is_system_default)
SELECT id, 'Business & Financial Acumen', 'Understanding of business drivers, fiscal responsibility, and operational efficiency', 3, true
FROM employee_title WHERE title_name = 'Dean';

INSERT INTO portfolio_category (title_id, category_name, description, sort_order, is_system_default)
SELECT id, 'Strategic Thinker', 'Ability to align work with organizational vision and plan for future success', 1, true
FROM employee_title WHERE title_name = 'Provost';

INSERT INTO portfolio_category (title_id, category_name, description, sort_order, is_system_default)
SELECT id, 'Lead by Example', 'Demonstrates integrity, sets positive tone, and motivates team through actions', 2, true
FROM employee_title WHERE title_name = 'Provost';

INSERT INTO portfolio_category (title_id, category_name, description, sort_order, is_system_default)
SELECT id, 'Business & Financial Acumen', 'Understanding of business drivers, fiscal responsibility, and operational efficiency', 3, true
FROM employee_title WHERE title_name = 'Provost';
