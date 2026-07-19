-- "Research Faculty" is a distinct default title: identical Teaching and Service setup to
-- Faculty, but Research follows the Rubric Sheet's "Alternative Section 2 with Research Course
-- Release" -- a single Publications and Patents criterion weighted 100%, instead of the standard
-- three-criteria Research breakdown.

INSERT INTO employee_title (title_name, is_system_default)
VALUES ('Research Faculty', true);

-- Categories: same three names/descriptions as Faculty
INSERT INTO portfolio_category (title_id, category_name, description, sort_order, is_system_default)
SELECT (SELECT id FROM employee_title WHERE title_name = 'Research Faculty'), pc.category_name, pc.description, pc.sort_order, true
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE et.title_name = 'Faculty';

-- Teaching: copy all 6 criteria from Faculty's Teaching
INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT rf_cat.id, cc.criteria_name, cc.description, cc.sort_order
FROM category_criteria cc
JOIN portfolio_category fac_cat ON fac_cat.id = cc.category_id
JOIN employee_title fac_title ON fac_title.id = fac_cat.title_id AND fac_title.title_name = 'Faculty'
JOIN portfolio_category rf_cat ON rf_cat.category_name = fac_cat.category_name
JOIN employee_title rf_title ON rf_title.id = rf_cat.title_id AND rf_title.title_name = 'Research Faculty'
WHERE fac_cat.category_name = 'Teaching';

-- Service: copy all 3 criteria from Faculty's Service
INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT rf_cat.id, cc.criteria_name, cc.description, cc.sort_order
FROM category_criteria cc
JOIN portfolio_category fac_cat ON fac_cat.id = cc.category_id
JOIN employee_title fac_title ON fac_title.id = fac_cat.title_id AND fac_title.title_name = 'Faculty'
JOIN portfolio_category rf_cat ON rf_cat.category_name = fac_cat.category_name
JOIN employee_title rf_title ON rf_title.id = rf_cat.title_id AND rf_title.title_name = 'Research Faculty'
WHERE fac_cat.category_name = 'Service';

-- Research: single 100%-weighted criterion per the Alternative Section 2 rubric
INSERT INTO category_criteria (category_id, criteria_name, description, sort_order)
SELECT pc.id, 'Publications and Patents',
'List all publications that appeared in print this year (peer-reviewed, non-peer-reviewed, book chapters, conference proceedings -- include paper title, journal/venue name, co-authors, and date published), plus publications in press (accepted but not yet published) and publications under review (journal name, co-authors, submission date). Include invention disclosures and patent applications. Attach related artifacts (e.g. published paper) where available.',
1
FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
WHERE pc.category_name = 'Research' AND et.title_name = 'Research Faculty';

-- Rank labels: same 1-5 scale as Faculty
INSERT INTO title_rank_label (title_id, rank, label, description)
SELECT (SELECT id FROM employee_title WHERE title_name = 'Research Faculty'), trl.rank, trl.label, trl.description
FROM title_rank_label trl JOIN employee_title et ON et.id = trl.title_id
WHERE et.title_name = 'Faculty';
