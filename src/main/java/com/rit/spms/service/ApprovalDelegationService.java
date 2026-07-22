package com.rit.spms.service;

import com.rit.spms.domain.ApprovalDelegation;
import com.rit.spms.domain.AppUser;
import com.rit.spms.domain.Department;
import com.rit.spms.domain.OrgGroup;
import com.rit.spms.domain.enums.ApprovalDelegationStatus;
import com.rit.spms.domain.enums.DelegationScopeType;
import com.rit.spms.dto.request.CreateApprovalDelegationRequest;
import com.rit.spms.exception.BusinessRuleException;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.exception.UnauthorizedException;
import com.rit.spms.repository.AppUserRepository;
import com.rit.spms.repository.ApprovalDelegationRepository;
import com.rit.spms.repository.DepartmentRepository;
import com.rit.spms.repository.OrgGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * General-purpose approval-authority delegation: any employee who is a headship-derived required
 * approver (Strategy approval chains, VSM author-grant approval, and any future type resolved via
 * {@link PermissionService#resolveEffectiveApprover}) can hand that authority to another employee
 * for a bounded window (capped at 4.5 months). Eligibility for the delegate:
 * <ol>
 *   <li>Any head above the delegator's own scope in the hierarchy chain -- activates immediately.</li>
 *   <li>Anyone directly reporting to the delegator -- activates immediately.</li>
 *   <li>Anyone else -- requires the delegator's own manager to approve first, unless the delegator
 *       has no manager (top of the org pyramid), in which case it also activates immediately.</li>
 * </ol>
 * A delegate can never re-delegate the same scope: creating a delegation requires actually being
 * the department/org-group's recorded head, which a delegate never becomes (they're only ever
 * resolved dynamically as the *effective* approver).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ApprovalDelegationService {

    private final ApprovalDelegationRepository approvalDelegationRepository;
    private final AppUserRepository appUserRepository;
    private final DepartmentRepository departmentRepository;
    private final OrgGroupRepository orgGroupRepository;
    private final PermissionService permissionService;
    private final ApplicationEventPublisher eventPublisher;

    public ApprovalDelegation createDelegation(CreateApprovalDelegationRequest req, Long delegatorUserId) {
        AppUser delegator = requireUser(delegatorUserId);
        AppUser delegate = requireUser(req.getDelegateId());
        if (delegate.getId().equals(delegator.getId())) {
            throw new BusinessRuleException("You cannot delegate your approval authority to yourself");
        }

        Department dept = null;
        OrgGroup group = null;
        if (req.getScopeType() == DelegationScopeType.DEPARTMENT) {
            dept = departmentRepository.findById(req.getScopeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", req.getScopeId()));
            if (dept.getHead() == null || !dept.getHead().getId().equals(delegatorUserId)) {
                throw new UnauthorizedException("You are not the head of this department");
            }
        } else {
            group = orgGroupRepository.findById(req.getScopeId())
                    .orElseThrow(() -> new ResourceNotFoundException("OrgGroup", req.getScopeId()));
            if (group.getHead() == null || !group.getHead().getId().equals(delegatorUserId)) {
                throw new UnauthorizedException("You are not the head of this org group");
            }
        }

        validateDates(req.getStartDate(), req.getEndDate());

        ApprovalDelegation.ApprovalDelegationBuilder builder = ApprovalDelegation.builder()
                .delegator(delegator)
                .delegate(delegate)
                .scopeType(req.getScopeType())
                .department(dept)
                .orgGroup(group)
                .startDate(req.getStartDate())
                .endDate(req.getEndDate());

        boolean eligibleWithoutApproval = isAncestorHead(delegate, req.getScopeType(), dept, group)
                || isDirectReport(delegate, delegator, req.getScopeType(), group);

        ApprovalDelegation saved;
        if (eligibleWithoutApproval) {
            saved = approvalDelegationRepository.save(builder
                    .requiresManagerApproval(false)
                    .status(ApprovalDelegationStatus.ACTIVE)
                    .build());
            eventPublisher.publishEvent(new ApprovalDelegationActivatedEvent(saved.getId()));
        } else {
            Optional<AppUser> manager = permissionService.resolveSupervisor(delegator);
            if (manager.isEmpty()) {
                // Top of the org pyramid: no manager to ask, so this delegate choice is allowed unrestricted.
                saved = approvalDelegationRepository.save(builder
                        .requiresManagerApproval(false)
                        .status(ApprovalDelegationStatus.ACTIVE)
                        .build());
                eventPublisher.publishEvent(new ApprovalDelegationActivatedEvent(saved.getId()));
            } else {
                saved = approvalDelegationRepository.save(builder
                        .requiresManagerApproval(true)
                        .managerApprover(manager.get())
                        .status(ApprovalDelegationStatus.PENDING_MANAGER_APPROVAL)
                        .build());
                eventPublisher.publishEvent(new ApprovalDelegationPendingEvent(saved.getId()));
            }
        }
        return saved;
    }

    public ApprovalDelegation decide(Long delegationId, Long managerUserId, boolean approved) {
        ApprovalDelegation delegation = requireDelegation(delegationId);
        if (delegation.getManagerApprover() == null || !delegation.getManagerApprover().getId().equals(managerUserId)) {
            throw new UnauthorizedException("Only the required manager approver can decide this delegation");
        }
        if (delegation.getStatus() != ApprovalDelegationStatus.PENDING_MANAGER_APPROVAL) {
            throw new BusinessRuleException("This delegation has already been decided");
        }
        delegation.setStatus(approved ? ApprovalDelegationStatus.ACTIVE : ApprovalDelegationStatus.REJECTED);
        delegation.setDecidedAt(java.time.LocalDateTime.now());
        ApprovalDelegation saved = approvalDelegationRepository.save(delegation);
        eventPublisher.publishEvent(new ApprovalDelegationDecidedEvent(saved.getId()));
        if (approved) {
            eventPublisher.publishEvent(new ApprovalDelegationActivatedEvent(saved.getId()));
        }
        return saved;
    }

    public ApprovalDelegation cancel(Long delegationId, Long currentUserId) {
        AppUser currentUser = requireUser(currentUserId);
        ApprovalDelegation delegation = requireDelegation(delegationId);
        boolean isDelegator = delegation.getDelegator().getId().equals(currentUserId);
        boolean isAdmin = currentUser.hasRole(com.rit.spms.domain.enums.SystemRole.ADMIN);
        if (!isDelegator && !isAdmin) {
            throw new UnauthorizedException("Only the delegator or an admin can cancel this delegation");
        }
        if (delegation.getStatus() != ApprovalDelegationStatus.ACTIVE
                && delegation.getStatus() != ApprovalDelegationStatus.PENDING_MANAGER_APPROVAL) {
            throw new BusinessRuleException("This delegation is no longer active or pending");
        }
        delegation.setStatus(ApprovalDelegationStatus.CANCELLED);
        delegation.setDecidedAt(java.time.LocalDateTime.now());
        return approvalDelegationRepository.save(delegation);
    }

    @Transactional(readOnly = true)
    public List<ApprovalDelegation> getMine(Long delegatorId) {
        return approvalDelegationRepository.findByDelegatorIdOrderByCreatedAtDesc(delegatorId);
    }

    @Transactional(readOnly = true)
    public List<ApprovalDelegation> getDelegatedToMe(Long delegateId) {
        return approvalDelegationRepository.findByDelegateIdOrderByCreatedAtDesc(delegateId);
    }

    @Transactional(readOnly = true)
    public List<ApprovalDelegation> getPendingForManagerApproval(Long managerId) {
        return approvalDelegationRepository.findByManagerApproverIdAndStatus(managerId, ApprovalDelegationStatus.PENDING_MANAGER_APPROVAL);
    }

    // ── eligibility helpers ─────────────────────────────────────────────────

    /** Is `delegate` a head anywhere above the delegator's own scope in the hierarchy chain? */
    private boolean isAncestorHead(AppUser delegate, DelegationScopeType scopeType, Department dept, OrgGroup group) {
        OrgGroup walk = scopeType == DelegationScopeType.DEPARTMENT
                ? (dept != null ? dept.getOrgGroup() : null)
                : (group != null ? group.getParent() : null);
        while (walk != null) {
            if (walk.getHead() != null && walk.getHead().getId().equals(delegate.getId())) {
                return true;
            }
            walk = walk.getParent();
        }
        return false;
    }

    /** Is `delegate` a direct report of the delegator? Covers ordinary employees under a headed
     *  department (via {@link PermissionService#resolveSupervisor}, which also handles a department
     *  head reporting up to their org group's head) plus the head of any org group whose immediate
     *  parent is the delegator's own headed org group -- a case resolveSupervisor can't see since
     *  it's keyed off department membership, not org-group membership. */
    private boolean isDirectReport(AppUser delegate, AppUser delegator, DelegationScopeType scopeType, OrgGroup group) {
        boolean viaSupervisorChain = permissionService.resolveSupervisor(delegate)
                .map(s -> s.getId().equals(delegator.getId()))
                .orElse(false);
        if (viaSupervisorChain) {
            return true;
        }
        if (scopeType == DelegationScopeType.ORG_GROUP && group != null) {
            return orgGroupRepository.findByParentId(group.getId()).stream()
                    .anyMatch(child -> child.getHead() != null && child.getHead().getId().equals(delegate.getId()));
        }
        return false;
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isBefore(LocalDate.now())) {
            throw new BusinessRuleException("Start date cannot be in the past");
        }
        if (!endDate.isAfter(startDate)) {
            throw new BusinessRuleException("End date must be after the start date");
        }
        LocalDate maxEnd = startDate.plusMonths(4).plusDays(15);
        if (endDate.isAfter(maxEnd)) {
            throw new BusinessRuleException("A delegation cannot last longer than 4.5 months");
        }
    }

    private ApprovalDelegation requireDelegation(Long id) {
        return approvalDelegationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApprovalDelegation", id));
    }

    private AppUser requireUser(Long userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", userId));
    }
}
