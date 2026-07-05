package com.rit.spms.repository;

import com.rit.spms.domain.SwotAlternativeProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SwotAlternativeProposalRepository extends JpaRepository<SwotAlternativeProposal, Long> {
    List<SwotAlternativeProposal> findBySwotSessionIdOrderByCreatedAt(Long swotSessionId);
    List<SwotAlternativeProposal> findBySwotSessionIdAndProposedById(Long swotSessionId, Long proposedById);
}
