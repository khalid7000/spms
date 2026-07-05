-- Drop the old constraint that only allows one null-department strategy per cycle.
-- Replace with a type-scoped constraint: only one UNIVERSITY-type (the aggregator) per cycle.
-- UNIT-type strategies (university-level without a department) are now allowed alongside it.
DROP INDEX IF EXISTS uq_strategy_cycle_univ;
CREATE UNIQUE INDEX uq_strategy_cycle_univ
    ON strategy(planning_cycle_id)
    WHERE department_id IS NULL AND strategy_type = 'UNIVERSITY';
