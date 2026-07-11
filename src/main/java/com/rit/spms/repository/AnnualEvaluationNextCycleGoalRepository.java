package com.rit.spms.repository;

import com.rit.spms.domain.AnnualEvaluationNextCycleGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnualEvaluationNextCycleGoalRepository extends JpaRepository<AnnualEvaluationNextCycleGoal, Long> {
    List<AnnualEvaluationNextCycleGoal> findByEvaluationIdOrderBySortOrder(Long evaluationId);

    void deleteByEvaluationId(Long evaluationId);

    @Query("SELECT g FROM AnnualEvaluationNextCycleGoal g WHERE g.evaluation.employee.id = :employeeId " +
           "AND g.used = false AND g.evaluation.state = com.rit.spms.domain.enums.AnnualEvaluationState.CONCLUDED " +
           "AND g.leaderActionType <> com.rit.spms.domain.enums.PortfolioReviewActionType.REJECT " +
           "AND g.employeeActionType <> com.rit.spms.domain.enums.PortfolioReviewActionType.REJECT " +
           "ORDER BY g.evaluation.academicYear.startDate DESC, g.sortOrder")
    List<AnnualEvaluationNextCycleGoal> findReusableByEmployeeId(@Param("employeeId") Long employeeId);

    // Same eligibility rules as findReusableByEmployeeId, scoped to one specific source evaluation
    // year -- used by the Team Goal Setting batch-reuse check, where the head explicitly names
    // which year's evaluations to pull from rather than pulling from any year.
    @Query("SELECT g FROM AnnualEvaluationNextCycleGoal g WHERE g.evaluation.employee.id = :employeeId " +
           "AND g.evaluation.academicYear.id = :academicYearId " +
           "AND g.used = false AND g.evaluation.state = com.rit.spms.domain.enums.AnnualEvaluationState.CONCLUDED " +
           "AND g.leaderActionType <> com.rit.spms.domain.enums.PortfolioReviewActionType.REJECT " +
           "AND g.employeeActionType <> com.rit.spms.domain.enums.PortfolioReviewActionType.REJECT " +
           "ORDER BY g.sortOrder")
    List<AnnualEvaluationNextCycleGoal> findReusableByEmployeeIdAndSourceAcademicYearId(
            @Param("employeeId") Long employeeId, @Param("academicYearId") Long academicYearId);
}
