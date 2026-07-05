package com.rit.spms.repository;

import com.rit.spms.domain.SwotSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SwotSuggestionRepository extends JpaRepository<SwotSuggestion, Long> {
    List<SwotSuggestion> findBySwotSessionIdOrderBySortOrder(Long swotSessionId);
    void deleteBySwotSessionId(Long swotSessionId);
}
