package com.rit.spms.repository;

import com.rit.spms.domain.Initiative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InitiativeRepository extends JpaRepository<Initiative, Long> {

    // Base (template) initiatives — used during strategy planning
    List<Initiative> findByObjectiveIdAndAcademicYearIsNullOrderBySortOrder(Long objectiveId);
    boolean existsByObjectiveIdAndAcademicYearIsNull(Long objectiveId);

    // Year-specific copies
    List<Initiative> findByObjectiveIdAndAcademicYearIdOrderBySortOrder(Long objectiveId, Long academicYearId);

    // All initiatives for an objective regardless of academic year (used by reports/PDF)
    List<Initiative> findByObjectiveIdOrderBySortOrder(Long objectiveId);

    @Query("SELECT i FROM Initiative i WHERE i.objective.goal.strategy.id = :strategyId AND i.academicYear IS NULL ORDER BY i.sortOrder")
    List<Initiative> findBaseByStrategyId(@Param("strategyId") Long strategyId);

    @Query("SELECT i FROM Initiative i WHERE i.objective.goal.strategy.id = :strategyId ORDER BY i.sortOrder")
    List<Initiative> findByStrategyId(@Param("strategyId") Long strategyId);
}
