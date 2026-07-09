package com.rit.spms.repository;

import com.rit.spms.domain.AnnualEvaluationCriteriaResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnualEvaluationCriteriaResultRepository extends JpaRepository<AnnualEvaluationCriteriaResult, Long> {
    List<AnnualEvaluationCriteriaResult> findByEvaluationId(Long evaluationId);
    Optional<AnnualEvaluationCriteriaResult> findByEvaluationIdAndCriteriaId(Long evaluationId, Long criteriaId);
}
