package com.rit.spms.repository;

import com.rit.spms.domain.RatingAssistantSelection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RatingAssistantSelectionRepository extends JpaRepository<RatingAssistantSelection, Long> {
    Optional<RatingAssistantSelection> findByEvaluationIdAndHeadIdAndTargetTypeAndTargetId(
            Long evaluationId, Long headId, String targetType, Long targetId);
}
