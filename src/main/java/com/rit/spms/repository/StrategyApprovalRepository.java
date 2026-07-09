package com.rit.spms.repository;

import com.rit.spms.domain.Strategy;
import com.rit.spms.domain.StrategyApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StrategyApprovalRepository extends JpaRepository<StrategyApproval, Long> {

    List<StrategyApproval> findByStrategyIdOrderByApprovalOrder(Long strategyId);

    List<StrategyApproval> findByRequiredApproverIdAndApprovedFalse(Long approverId);

    Optional<StrategyApproval> findByStrategyIdAndRequiredApproverIdAndApprovedFalse(
            Long strategyId, Long approverId);

    boolean existsByStrategyIdAndRequiredApproverIdAndApprovedFalse(
            Long strategyId, Long approverId);

    /**
     * Must be a bulk DELETE (not a derived delete-by-entity method) so it executes immediately
     * as SQL. A derived delete loads matching rows and calls entityManager.remove() on each,
     * which only *queues* the removal as a persistence-context action -- and Hibernate's fixed
     * flush ordering (inserts, updates, then deletes) runs the new chain's inserts in
     * initiateApproval() BEFORE these queued deletes physically hit the table, tripping the
     * (strategy_id, required_approver_id) unique constraint whenever the rebuilt chain reuses an
     * approver from the old one (always true for a UNIVERSITY strategy, whose sole approver is
     * the same org-hierarchy head every time).
     *
     * NOTE: deliberately no clearAutomatically here -- that detaches every entity already managed
     * in this transaction (including the Strategy/AppUser the caller loaded before calling this),
     * which then breaks their not-yet-initialized lazy associations (e.g. owner.getDepartment())
     * with a LazyInitializationException. The bulk JPQL delete already executes immediately
     * against the DB regardless; clearing the context isn't needed for that and isn't safe here.
     */
    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM StrategyApproval sa WHERE sa.strategy = :strategy")
    void deleteByStrategy(@Param("strategy") Strategy strategy);
}
