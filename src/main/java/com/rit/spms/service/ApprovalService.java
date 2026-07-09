package com.rit.spms.service;

import com.rit.spms.domain.*;
import com.rit.spms.domain.enums.RoleType;
import com.rit.spms.domain.enums.StrategyState;
import com.rit.spms.domain.enums.StrategyType;
import com.rit.spms.dto.response.ApprovalRequestResponse;
import com.rit.spms.exception.BusinessRuleException;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ApprovalService {

    private final StrategyApprovalRepository approvalRepository;
    private final StrategyRepository strategyRepository;
    private final RoleAssignmentRepository roleAssignmentRepository;
    private final AppUserRepository appUserRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AcademicYearService academicYearService;

    /**
     * Called when an owner requests deployment.
     * Builds the approval chain from the owner's department hierarchy.
     * If the chain is empty (no heads set, or owner has no dept), deploys immediately.
     */
    public void initiateApproval(Strategy strategy, AppUser owner) {
        // Clear stale records from a previous attempt
        approvalRepository.deleteByStrategy(strategy);

        List<StrategyApproval> chain = buildApprovalChain(strategy, owner);
        if (chain.isEmpty()) {
            strategy.setState(StrategyState.DEPLOYED);
            academicYearService.backfillInitiativeCopiesForNewlyDeployedStrategy(strategy);
        } else {
            approvalRepository.saveAll(chain);
            strategy.setState(StrategyState.APPROVAL_PENDING);
            // Every approver in the chain can act immediately (approval isn't sequential -- see
            // `approve()`'s allMatch check), so notify all of them now rather than one at a time.
            for (StrategyApproval approval : chain) {
                eventPublisher.publishEvent(new StrategyApprovalPendingEvent(strategy.getId(), approval.getRequiredApprover().getId()));
            }
        }
        strategyRepository.save(strategy);
    }

    /** Approve a strategy on behalf of the current user. Auto-deploys when all approve. */
    public void approve(Long strategyId, Long approverId) {
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy", strategyId));

        if (strategy.getState() != StrategyState.APPROVAL_PENDING) {
            throw new BusinessRuleException("Strategy is not awaiting approval");
        }

        StrategyApproval approval = approvalRepository
                .findByStrategyIdAndRequiredApproverIdAndApprovedFalse(strategyId, approverId)
                .orElseThrow(() -> new BusinessRuleException(
                        "You do not have a pending approval for this strategy"));

        approval.setApproved(true);
        approval.setApprovedAt(LocalDateTime.now());
        approvalRepository.save(approval);

        boolean allApproved = approvalRepository
                .findByStrategyIdOrderByApprovalOrder(strategyId)
                .stream().allMatch(StrategyApproval::getApproved);

        if (allApproved) {
            strategy.setState(StrategyState.DEPLOYED);
            strategyRepository.save(strategy);
            academicYearService.backfillInitiativeCopiesForNewlyDeployedStrategy(strategy);
        }
    }

    @Transactional(readOnly = true)
    public List<ApprovalRequestResponse> getPendingForUser(Long userId) {
        return approvalRepository.findByRequiredApproverIdAndApprovedFalse(userId)
                .stream()
                .map(a -> {
                    String[] o = resolveOwner(a.getStrategy());
                    return ApprovalRequestResponse.from(a, o[0], o[1]);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApprovalRequestResponse> getApprovalStatusForStrategy(Long strategyId) {
        return approvalRepository.findByStrategyIdOrderByApprovalOrder(strategyId)
                .stream()
                .map(a -> {
                    String[] o = resolveOwner(a.getStrategy());
                    return ApprovalRequestResponse.from(a, o[0], o[1]);
                })
                .toList();
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private List<StrategyApproval> buildApprovalChain(Strategy strategy, AppUser owner) {
        // A university-wide plan isn't owned by any one department, so it doesn't make sense to
        // route it through the owner's own personal department chair/dean -- only the single
        // authority at the top of the org hierarchy (e.g. the Provost) is required.
        if (strategy.getStrategyType() == StrategyType.UNIVERSITY) {
            return buildTopOfHierarchyChain(strategy, owner);
        }

        List<StrategyApproval> chain = new ArrayList<>();
        Set<Long> seen = new LinkedHashSet<>();   // dedup: same user can't appear twice
        int order = 1;

        Department dept = owner.getDepartment();
        if (dept == null) return chain;

        if (dept.getHead() != null && seen.add(dept.getHead().getId())) {
            String label = (dept.getHeadTitle() != null ? dept.getHeadTitle() : "Head")
                    + ", " + dept.getName();
            chain.add(buildRecord(strategy, dept.getHead(), label, order++));
        }

        OrgGroup group = dept.getOrgGroup();
        while (group != null) {
            if (group.getHead() != null && seen.add(group.getHead().getId())) {
                String label = group.getHeadTitle() + ", " + group.getTitle();
                chain.add(buildRecord(strategy, group.getHead(), label, order++));
            }
            group = group.getParent();
        }

        return chain;
    }

    /** Walks up from the owner's department to the root OrgGroup (no parent) and requires only its head. */
    private List<StrategyApproval> buildTopOfHierarchyChain(Strategy strategy, AppUser owner) {
        Department dept = owner.getDepartment();
        OrgGroup group = dept != null ? dept.getOrgGroup() : null;
        OrgGroup root = null;
        while (group != null) {
            root = group;
            group = group.getParent();
        }

        if (root == null || root.getHead() == null) {
            return new ArrayList<>();
        }
        String label = root.getHeadTitle() + ", " + root.getTitle();
        return new ArrayList<>(List.of(buildRecord(strategy, root.getHead(), label, 1)));
    }

    private StrategyApproval buildRecord(Strategy strategy, AppUser approver,
                                         String title, int order) {
        return StrategyApproval.builder()
                .strategy(strategy)
                .requiredApprover(approver)
                .approverTitle(title)
                .approvalOrder(order)
                .build();
    }

    private String[] resolveOwner(Strategy strategy) {
        return roleAssignmentRepository.findByStrategyId(strategy.getId())
                .stream()
                .filter(ra -> ra.getRole() == RoleType.OWNER)
                .findFirst()
                .map(ra -> new String[]{
                        ra.getUser().getEmail(),
                        ra.getUser().getFname() + " " + ra.getUser().getLname()})
                .orElse(new String[]{"", ""});
    }
}
