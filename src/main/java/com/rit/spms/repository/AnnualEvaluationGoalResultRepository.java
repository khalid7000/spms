package com.rit.spms.repository;

import com.rit.spms.domain.AnnualEvaluationGoalResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnualEvaluationGoalResultRepository extends JpaRepository<AnnualEvaluationGoalResult, Long> {
    List<AnnualEvaluationGoalResult> findByEvaluationId(Long evaluationId);
    Optional<AnnualEvaluationGoalResult> findByEvaluationIdAndGoalId(Long evaluationId, Long goalId);
}
