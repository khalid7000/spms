package com.rit.spms.service;

import com.rit.spms.domain.AppUser;
import com.rit.spms.domain.Department;
import com.rit.spms.domain.OrgGroup;
import com.rit.spms.domain.VsmAuthorGrant;
import com.rit.spms.domain.enums.SystemRole;
import com.rit.spms.domain.enums.VsmAuthorGrantStatus;
import com.rit.spms.domain.enums.VsmScopeType;
import com.rit.spms.dto.request.CreateVsmAuthorGrantRequest;
import com.rit.spms.exception.BusinessRuleException;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.exception.UnauthorizedException;
import com.rit.spms.repository.AppUserRepository;
import com.rit.spms.repository.DepartmentRepository;
import com.rit.spms.repository.OrgGroupRepository;
import com.rit.spms.repository.VsmAuthorGrantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * VSM author delegation (Phase 4 of the VSM module, judgment call #2 in the round-1 plan): an Admin
 * grants "VSM author" rights over a unit to an employee, but it only takes effect once the
 * top-of-hierarchy head above that employee approves. {@code requiredApprover} is always resolved
 * server-side via {@link PermissionService#resolveTopOfHierarchyGroup} + {@link
 * PermissionService#resolveEffectiveApprover} -- the same seam a future time-bound delegation
 * feature will hook into for every approval flow in the app, not just this one.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class VsmAuthorGrantService {

    private final VsmAuthorGrantRepository vsmAuthorGrantRepository;
    private final AppUserRepository appUserRepository;
    private final DepartmentRepository departmentRepository;
    private final OrgGroupRepository orgGroupRepository;
    private final PermissionService permissionService;
    private final ApplicationEventPublisher eventPublisher;

    public VsmAuthorGrant createGrant(CreateVsmAuthorGrantRequest req, Long adminUserId) {
        AppUser admin = requireUser(adminUserId);
        AppUser employee = requireUser(req.getEmployeeId());

        VsmAuthorGrant.VsmAuthorGrantBuilder builder = VsmAuthorGrant.builder()
                .employee(employee)
                .grantedByAdmin(admin)
                .scopeType(req.getScopeType())
                .status(VsmAuthorGrantStatus.PENDING_APPROVAL);

        OrgGroup rootGroup;
        if (req.getScopeType() == VsmScopeType.DEPARTMENT) {
            Department dept = departmentRepository.findById(req.getScopeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", req.getScopeId()));
            builder.department(dept);
            rootGroup = permissionService.resolveTopOfHierarchyGroup(dept)
                    .orElseThrow(() -> new BusinessRuleException(
                            "No top-of-hierarchy head could be resolved for this department's org-group chain"));
        } else {
            OrgGroup group = orgGroupRepository.findById(req.getScopeId())
                    .orElseThrow(() -> new ResourceNotFoundException("OrgGroup", req.getScopeId()));
            builder.orgGroup(group);
            rootGroup = permissionService.resolveTopOfHierarchyGroup(group)
                    .orElseThrow(() -> new BusinessRuleException(
                            "No top-of-hierarchy head could be resolved for this org-group chain"));
        }
        if (rootGroup.getHead() == null) {
            throw new BusinessRuleException("The top-level org-group has no head assigned to approve this grant");
        }
        AppUser nominalApprover = rootGroup.getHead();
        AppUser effectiveApprover = permissionService.resolveEffectiveApprover(nominalApprover, LocalDateTime.now().toLocalDate());

        builder.requiredApprover(effectiveApprover)
                .approverTitle(rootGroup.getHeadTitle() + ", " + rootGroup.getTitle());

        VsmAuthorGrant grant = vsmAuthorGrantRepository.save(builder.build());
        eventPublisher.publishEvent(new VsmAuthorGrantPendingEvent(grant.getId()));
        return grant;
    }

    public VsmAuthorGrant decide(Long grantId, Long approverId, boolean approved) {
        VsmAuthorGrant grant = requireGrant(grantId);
        if (!grant.getRequiredApprover().getId().equals(approverId)) {
            throw new UnauthorizedException("Only the required approver can decide this grant");
        }
        if (grant.getStatus() != VsmAuthorGrantStatus.PENDING_APPROVAL) {
            throw new BusinessRuleException("This grant has already been decided");
        }
        grant.setStatus(approved ? VsmAuthorGrantStatus.ACTIVE : VsmAuthorGrantStatus.REJECTED);
        grant.setDecidedAt(LocalDateTime.now());
        VsmAuthorGrant saved = vsmAuthorGrantRepository.save(grant);
        eventPublisher.publishEvent(new VsmAuthorGrantDecidedEvent(saved.getId()));
        return saved;
    }

    public VsmAuthorGrant revoke(Long grantId, Long adminUserId) {
        AppUser admin = requireUser(adminUserId);
        if (!admin.hasRole(SystemRole.ADMIN)) {
            throw new UnauthorizedException("Only an admin can revoke a VSM author grant");
        }
        VsmAuthorGrant grant = requireGrant(grantId);
        grant.setStatus(VsmAuthorGrantStatus.REVOKED);
        grant.setDecidedAt(LocalDateTime.now());
        return vsmAuthorGrantRepository.save(grant);
    }

    @Transactional(readOnly = true)
    public List<VsmAuthorGrant> getAll() {
        return vsmAuthorGrantRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<VsmAuthorGrant> getPendingForApprover(Long approverId) {
        return vsmAuthorGrantRepository.findByRequiredApproverIdAndStatus(approverId, VsmAuthorGrantStatus.PENDING_APPROVAL);
    }

    @Transactional(readOnly = true)
    public List<VsmAuthorGrant> getMine(Long employeeId) {
        return vsmAuthorGrantRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
    }

    private VsmAuthorGrant requireGrant(Long grantId) {
        return vsmAuthorGrantRepository.findById(grantId)
                .orElseThrow(() -> new ResourceNotFoundException("VsmAuthorGrant", grantId));
    }

    private AppUser requireUser(Long userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", userId));
    }
}
