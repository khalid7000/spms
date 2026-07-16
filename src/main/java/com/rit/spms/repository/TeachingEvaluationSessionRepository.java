package com.rit.spms.repository;

import com.rit.spms.domain.TeachingEvaluationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeachingEvaluationSessionRepository extends JpaRepository<TeachingEvaluationSession, Long> {
    Optional<TeachingEvaluationSession> findByEvaluationIdAndCriteriaId(Long evaluationId, Long criteriaId);
}
