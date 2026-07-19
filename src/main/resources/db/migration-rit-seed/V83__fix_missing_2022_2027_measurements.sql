-- V6__seed_2022_2027.sql created 19 initiatives under the University-level 2022-2027
-- Strategic Plan without a following measurement (KPI) insert, which silently blocked
-- users from recording achievements against them (no measurement = nowhere to attach an
-- achievement). Matched by initiative title (scoped to the 2022-2027 strategy) rather than
-- a hardcoded id, since BIGSERIAL-assigned ids aren't guaranteed to be numbered the same
-- way in every environment this migration runs against. "Each department holds at least
-- one k-12 event per year in the Innovation Center" appears twice under different
-- objectives -- both get the same KPI, which is intentional.

INSERT INTO measurement (initiative_id, description, unit, target_value, sort_order)
SELECT i.id, v.description, v.unit, v.target_value, 1
FROM initiative i
JOIN objective o ON o.id = i.objective_id
JOIN goal g ON g.id = o.goal_id
JOIN strategy s ON s.id = g.strategy_id
JOIN (VALUES
    ('More engagement with Alumni. Devoted resource to complete DB and establish connection. Start more regular events.',
        'need to hold Alumni events and activities', '#', 1),
    ('Create 6 applied research/consultancy projects-coop',
        'Number of applied research/consultancy projects created with co-op partners', '#', 3),
    ('Deliver at least 2 training diplomas to UAE government entities',
        'Number of training diplomas delivered to UAE government entities', '#', 2),
    ('Utilize RIT365 and extra coaching sessions to raise interns/coop quality',
        'Number of extra coaching sessions delivered via RIT365 to interns/co-op students', '#', 5),
    ('Keep Alumni data completed and accurate for at least 75% of all Alumni',
        'Percentage of Alumni records completed and accurate', '%', 75),
    ('Each Academic department is engaged in at least one event per term',
        'Number of admission-related events each department participates in per term', '#', 1),
    ('Meet 10 Gov. entities to promote courses.',
        'Number of government entities met to promote courses', '#', 10),
    ('Academic programs engage with all programs, especially EE&C, in promoting executive education offerings',
        'Number of academic programs actively promoting executive education offerings', '#', 1),
    ('Facilitate engagement of degree programs with all programs like EE&C',
        'Number of degree programs engaged with EE&C-like programs', '#', 1),
    ('Collaborate with admission on sponsoring an event with a k-12 school',
        'Number of k-12 school events sponsored in collaboration with admissions', '#', 1),
    ('Funds are identified by the center. Application process is established by designated taskforce. Projects are submitted and launched. Improve the deployment of the Innovation Journey in all programs through proper measurement and faculty training.',
        'Number of new student/faculty businesses launched through the Innovation Center', '#', 1),
    ('Each department holds at least one k-12 event per year in the Innovation Center',
        'Number of k-12 events held per department per year in the Innovation Center', '#', 1),
    ('Conduct One Major Alumni event per year',
        'Number of major Alumni events conducted per year', '#', 1),
    ('Participate and/or deliver competitions with UAE government entities',
        'Number of competitions participated in or delivered with UAE government entities', '#', 1),
    ('Target 2 event sponsors (Gov. or Corp.)',
        'Number of event sponsors secured (Government or Corporate)', '#', 2),
    ('Each program helps co-op office with at least one industry connection every year',
        'Number of industry connections each program helps establish with the co-op office annually', '#', 1),
    ('Introduce new programs and course offerings',
        'Number of new programs, minors, CBMCs, immersions, course offerings or skills enhancements introduced', '#', 1),
    ('Meet Gov. entities to build relationship',
        'Number of government entities met to build relationships', '#', 1)
) AS v(initiative_title, description, unit, target_value)
    ON v.initiative_title = i.title
WHERE s.title = 'RIT Dubai 2022-2027 Strategic Plan'
  AND NOT EXISTS (SELECT 1 FROM measurement m WHERE m.initiative_id = i.id);
