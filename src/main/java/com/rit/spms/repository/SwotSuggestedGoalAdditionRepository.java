package com.rit.spms.repository;

import com.rit.spms.domain.SwotSuggestedGoalAddition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SwotSuggestedGoalAdditionRepository extends JpaRepository<SwotSuggestedGoalAddition, Long> {
    List<SwotSuggestedGoalAddition> findBySwotSuggestionIdOrderBySortOrder(Long swotSuggestionId);
    List<SwotSuggestedGoalAddition> findBySwotSuggestion_SwotSession_IdOrderByCreatedAt(Long swotSessionId);
    List<SwotSuggestedGoalAddition> findBySwotSuggestion_SwotSession_IdAndProposedByIdOrderByCreatedAt(
            Long swotSessionId, Long proposedById);
}
