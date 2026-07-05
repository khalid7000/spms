package com.rit.spms.repository;

import com.rit.spms.domain.SwotAlternativeProposedGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SwotAlternativeProposedGoalRepository extends JpaRepository<SwotAlternativeProposedGoal, Long> {
    List<SwotAlternativeProposedGoal> findByAlternativeProposalIdOrderBySortOrder(Long alternativeProposalId);
    List<SwotAlternativeProposedGoal> findByAlternativeProposalIdInOrderBySortOrder(List<Long> alternativeProposalIds);
}
