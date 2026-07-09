-- V43 replaced Teaching's criteria correctly (explicit DELETE first) but forgot to delete the
-- old placeholder Research/Service criteria (from an earlier fix, V41) before inserting the new
-- rubric-based ones -- leaving both sets present. V44 then copied that duplicated Service set into
-- the new "Research Faculty" title too. Remove the old criteria by name, scoped to exactly the
-- categories/titles this affected.

DELETE FROM category_criteria
WHERE criteria_name IN (
    'Publication Output', 'Research Funding & Grants', 'Impact & Dissemination', 'Collaboration & Mentorship',
    'University Service', 'Professional Service', 'Community Engagement', 'Leadership & Initiative'
)
AND category_id IN (
    SELECT pc.id FROM portfolio_category pc JOIN employee_title et ON et.id = pc.title_id
    WHERE pc.category_name IN ('Research', 'Service') AND et.title_name IN ('Faculty', 'Research Faculty')
);
