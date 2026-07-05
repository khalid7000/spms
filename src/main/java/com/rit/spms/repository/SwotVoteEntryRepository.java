package com.rit.spms.repository;

import com.rit.spms.domain.SwotVoteEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SwotVoteEntryRepository extends JpaRepository<SwotVoteEntry, Long> {
    List<SwotVoteEntry> findBySwotSessionIdAndUserId(Long swotSessionId, Long userId);
    List<SwotVoteEntry> findBySwotSessionId(Long swotSessionId);
    void deleteBySwotSessionIdAndUserId(Long swotSessionId, Long userId);
}
