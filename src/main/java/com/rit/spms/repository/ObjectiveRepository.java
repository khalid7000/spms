package com.rit.spms.repository;

import com.rit.spms.domain.Objective;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObjectiveRepository extends JpaRepository<Objective, Long> {
    List<Objective> findByGoalIdOrderBySortOrder(Long goalId);
    boolean existsByGoalId(Long goalId);

    @Query("SELECT o FROM Objective o WHERE o.goal.strategy.id = :strategyId ORDER BY o.sortOrder")
    List<Objective> findByStrategyId(@Param("strategyId") Long strategyId);
}
