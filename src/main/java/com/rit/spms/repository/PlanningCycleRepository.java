package com.rit.spms.repository;

import com.rit.spms.domain.PlanningCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlanningCycleRepository extends JpaRepository<PlanningCycle, Long> {
    Optional<PlanningCycle> findByActiveTrue();
}
