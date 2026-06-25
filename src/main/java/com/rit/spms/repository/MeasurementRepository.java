package com.rit.spms.repository;

import com.rit.spms.domain.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeasurementRepository extends JpaRepository<Measurement, Long> {
    List<Measurement> findByInitiativeIdOrderBySortOrder(Long initiativeId);

    @Query("SELECT m FROM Measurement m WHERE m.initiative.objective.goal.strategy.id = :strategyId ORDER BY m.sortOrder")
    List<Measurement> findByStrategyId(@Param("strategyId") Long strategyId);
}
