package com.rit.spms.repository;

import com.rit.spms.domain.AssessmentPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentPeriodRepository extends JpaRepository<AssessmentPeriod, Long> {
    List<AssessmentPeriod> findByPlanningCycleIdOrderBySortOrder(Long planningCycleId);
    void deleteByPlanningCycleId(Long planningCycleId);

    // Reverse of AcademicYearRepository.findByName -- the two are matched by name convention, not
    // an FK (see PortfolioEntry.belongsToAcademicYear). Used to resolve which AssessmentPeriod an
    // Achievement should carry when it's created from an AnnualEvaluation rather than the Strategy
    // Tree (see TeachingEvaluationSessionService.finalizeAchievement).
    Optional<AssessmentPeriod> findFirstByNameOrderByIdDesc(String name);
}
