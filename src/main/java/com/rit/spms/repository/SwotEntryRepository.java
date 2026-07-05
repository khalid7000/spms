package com.rit.spms.repository;

import com.rit.spms.domain.SwotEntry;
import com.rit.spms.domain.enums.SwotQuadrant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SwotEntryRepository extends JpaRepository<SwotEntry, Long> {
    List<SwotEntry> findBySwotSessionIdAndUserIdOrderByQuadrantAscSortOrderAsc(Long swotSessionId, Long userId);
    List<SwotEntry> findBySwotSessionIdAndUserIdAndQuadrant(Long swotSessionId, Long userId, SwotQuadrant quadrant);
    long countBySwotSessionIdAndUserIdAndQuadrant(Long swotSessionId, Long userId, SwotQuadrant quadrant);
    boolean existsBySwotSessionIdAndUserIdAndQuadrantAndNormalizedWord(
            Long swotSessionId, Long userId, SwotQuadrant quadrant, String normalizedWord);
    List<SwotEntry> findBySwotSessionIdOrderByQuadrantAscCreatedAtAsc(Long swotSessionId);
    List<SwotEntry> findBySwotSessionIdAndQuadrant(Long swotSessionId, SwotQuadrant quadrant);
}
