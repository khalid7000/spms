package com.rit.spms.repository;

import com.rit.spms.domain.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByStrategyIdOrderBySortOrder(Long strategyId);
    List<Goal> findByAreaId(Long areaId);
    boolean existsByStrategyId(Long strategyId);
    boolean existsByStrategyIdAndAreaId(Long strategyId, Long areaId);
    boolean existsByAreaId(Long areaId);
}
