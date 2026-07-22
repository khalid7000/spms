package com.rit.spms.domain;

import com.rit.spms.domain.enums.VsmAuthorGrantStatus;
import com.rit.spms.domain.enums.VsmScopeType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Phase 4 of the VSM module: an Admin can grant "VSM author" rights over a unit to an employee who
 * isn't its head, but the grant needs sign-off from the top-of-hierarchy head above that employee
 * before it's active (judgment call #2 in the round-1 plan). Shaped deliberately like {@link
 * StrategyApproval} ({@code requiredApprover}/{@code approverTitle}, same field names) so a future
 * "my approvals" feature can merge results across both tables without a schema change.
 */
@Entity
@Table(name = "vsm_author_grant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VsmAuthorGrant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private AppUser employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "granted_by_admin_id", nullable = false)
    private AppUser grantedByAdmin;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false, length = 20)
    private VsmScopeType scopeType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_group_id")
    private OrgGroup orgGroup;

    /** Server-resolved via PermissionService#resolveTopOfHierarchyHead + #resolveEffectiveApprover
     *  -- never client-supplied, same principle as StrategyApproval#requiredApprover. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "required_approver_id", nullable = false)
    private AppUser requiredApprover;

    /** Display label, e.g. "Provost, University" -- same idiom as StrategyApproval#approverTitle. */
    @Column(name = "approver_title", nullable = false, length = 300)
    private String approverTitle;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VsmAuthorGrantStatus status = VsmAuthorGrantStatus.PENDING_APPROVAL;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
