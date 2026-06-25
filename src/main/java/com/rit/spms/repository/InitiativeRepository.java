package com.rit.spms.repository;

import com.rit.spms.domain.Initiative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InitiativeRepository extends JpaRepository<Initiative, Long> {
    List<Initiative> findByObjectiveIdOrderBySortOrder(Long objectiveId);

    @Query("SELECT i FROM Initiative i WHERE i.objective.goal.strategy.id = :strategyId ORDER BY i.sortOrder")
    List<Initiative> findByStrategyId(@Param("strategyId") Long strategyId);
}
