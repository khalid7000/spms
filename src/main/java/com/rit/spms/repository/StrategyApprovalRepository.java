package com.rit.spms.repository;

import com.rit.spms.domain.Strategy;
import com.rit.spms.domain.StrategyApproval;
import org.springframework.data.jpa.repository.JpaRepository;
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

    void deleteByStrategy(Strategy strategy);
}
