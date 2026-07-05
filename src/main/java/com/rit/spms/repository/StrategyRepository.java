package com.rit.spms.repository;

import com.rit.spms.domain.Strategy;
import com.rit.spms.domain.enums.StrategyState;
import com.rit.spms.domain.enums.StrategyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StrategyRepository extends JpaRepository<Strategy, Long> {
    Optional<Strategy> findByPlanningCycleIdAndDepartmentIsNull(Long planningCycleId);
    Optional<Strategy> findByPlanningCycleIdAndDepartmentIsNullAndStrategyType(Long planningCycleId, StrategyType type);
    Optional<Strategy> findByPlanningCycleIdAndDepartmentId(Long planningCycleId, Long departmentId);
    boolean existsByPlanningCycleId(Long planningCycleId);

    @Query("SELECT s FROM Strategy s WHERE s.strategyType = :type")
    List<Strategy> findByStrategyType(@Param("type") StrategyType type);

    @Query("SELECT DISTINCT s FROM Strategy s JOIN RoleAssignment ra ON ra.strategy = s WHERE ra.user.id = :userId")
    List<Strategy> findByUserId(@Param("userId") Long userId);

    List<Strategy> findByState(StrategyState state);
}
