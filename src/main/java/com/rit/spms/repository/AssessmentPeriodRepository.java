package com.rit.spms.repository;

import com.rit.spms.domain.AssessmentPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentPeriodRepository extends JpaRepository<AssessmentPeriod, Long> {
    List<AssessmentPeriod> findByPlanningCycleIdOrderBySortOrder(Long planningCycleId);
    void deleteByPlanningCycleId(Long planningCycleId);
}
