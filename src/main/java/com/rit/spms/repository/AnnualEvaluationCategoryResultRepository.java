package com.rit.spms.repository;

import com.rit.spms.domain.AnnualEvaluationCategoryResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnualEvaluationCategoryResultRepository extends JpaRepository<AnnualEvaluationCategoryResult, Long> {
    List<AnnualEvaluationCategoryResult> findByEvaluationId(Long evaluationId);
    Optional<AnnualEvaluationCategoryResult> findByEvaluationIdAndCategoryId(Long evaluationId, Long categoryId);
}
