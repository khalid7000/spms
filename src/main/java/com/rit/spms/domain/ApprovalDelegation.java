package com.rit.spms.domain;

import com.rit.spms.domain.enums.ApprovalDelegationStatus;
import com.rit.spms.domain.enums.DelegationScopeType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * General-purpose, time-bound delegation of a headship's approval authority to another employee --
 * the feature {@link com.rit.spms.service.PermissionService#resolveEffectiveApprover} was built as a
 * seam for. A delegator may only hand off to: an ancestor head above their own scope, or a direct
 * report -- either of which activates immediately -- or, for anyone else, only after the
 * delegator's own manager approves (skipped entirely if the delegator has no manager, i.e. sits at
 * the top of the org pyramid). Once ACTIVE and within [startDate, endDate], every nominal-approver
 * resolution in the app (Strategy approval chains, VSM author-grant approval) treats {@code
 * delegate} as the approver instead of {@code delegator} -- see {@code resolveEffectiveApprover}.
 * A delegate cannot re-delegate the same scope: creating a new delegation requires actually being
 * the department/org-group's recorded head, which the delegate never becomes.
 */
@Entity
@Table(name = "approval_delegation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalDelegation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delegator_id", nullable = false)
    private AppUser delegator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delegate_id", nullable = false)
    private AppUser delegate;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false, length = 20)
    private DelegationScopeType scopeType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_group_id")
    private OrgGroup orgGroup;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApprovalDelegationStatus status = ApprovalDelegationStatus.ACTIVE;

    /** True only when the delegate is neither an ancestor head nor a direct report -- the case that
     *  requires {@code managerApprover}'s sign-off before activation. */
    @Column(name = "requires_manager_approval", nullable = false)
    private boolean requiresManagerApproval;

    /** The delegator's own resolved supervisor -- only set when {@code requiresManagerApproval} is
     *  true. Null whenever the delegator is at the top of the org pyramid. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_approver_id")
    private AppUser managerApprover;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
