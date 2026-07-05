package com.rit.spms.repository;

import com.rit.spms.domain.SwotSuggestedGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SwotSuggestedGoalRepository extends JpaRepository<SwotSuggestedGoal, Long> {
    List<SwotSuggestedGoal> findBySwotSuggestionIdOrderBySortOrder(Long swotSuggestionId);
    List<SwotSuggestedGoal> findBySwotSuggestionIdInOrderBySortOrder(List<Long> swotSuggestionIds);
}
