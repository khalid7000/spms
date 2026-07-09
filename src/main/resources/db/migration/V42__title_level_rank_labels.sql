-- Rank labels (1-5) move from PortfolioCategory to EmployeeTitle: specified once per title,
-- applying to every category under that title as well as the final overall rank given during an
-- evaluation cycle, rather than being repeated (and potentially inconsistent) per category.

CREATE TABLE title_rank_label (
    id BIGSERIAL PRIMARY KEY,
    title_id BIGINT NOT NULL REFERENCES employee_title(id) ON DELETE CASCADE,
    rank INT NOT NULL,
    label VARCHAR(200) NOT NULL,
    description TEXT,
    UNIQUE(title_id, rank)
);

-- Faculty's rank labels carry over whatever is currently set on the Teaching category (the only
-- category with rank labels so far, including any admin customization already made) -- this is a
-- straight copy, not a hardcoded default, so it preserves exactly what's there today.
INSERT INTO title_rank_label (title_id, rank, label, description)
SELECT pc.title_id, crl.rank, crl.label, crl.description
FROM category_rank_label crl
JOIN portfolio_category pc ON pc.id = crl.category_id
WHERE pc.category_name = 'Teaching';

-- Every other title with categories but no rank labels yet gets the same generic 1-5 defaults
-- Teaching originally shipped with (V38), so the admin has something to start customizing from.
INSERT INTO title_rank_label (title_id, rank, label, description)
SELECT et.id, r.rank, r.label, r.description
FROM employee_title et
CROSS JOIN (VALUES
    (1, 'Below Expectations', 'Does not meet minimum standards for this title'),
    (2, 'Needs Improvement', 'Some areas require development'),
    (3, 'Meets Expectations', 'Consistently demonstrates effective performance'),
    (4, 'Exceeds Expectations', 'Demonstrates outstanding performance'),
    (5, 'Outstanding', 'Exemplary performance with measurable impact')
) AS r(rank, label, description)
WHERE NOT EXISTS (SELECT 1 FROM title_rank_label trl WHERE trl.title_id = et.id);

DROP TABLE category_rank_label;
