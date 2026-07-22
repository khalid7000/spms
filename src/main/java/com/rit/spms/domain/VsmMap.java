package com.rit.spms.domain;

import com.rit.spms.domain.enums.VsmMapState;
import com.rit.spms.domain.enums.VsmScopeType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * A Value Stream Map: a leader's visual model of one critical process they own (e.g. a curriculum,
 * a hiring pipeline). Standalone from the Strategy tree on purpose -- a process doesn't reset every
 * academic year the way an Initiative does, so there is no planningCycle/academicYear FK here.
 * Exactly one of department/orgGroup is set, matching scopeType (see VsmScopeType).
 */
@Entity
@Table(name = "vsm_map")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VsmMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false, length = 20)
    private VsmScopeType scopeType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_group_id")
    private OrgGroup orgGroup;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VsmMapState state = VsmMapState.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private AppUser createdBy;

    // AI draft generation tracking (async background job, same shape as EmployeeGoalCycle's
    // generationRequestedAt/suggestionsGeneratedAt/generationFailureReason and
    // TeachingEvaluationSession's generationRequestedAt/generatedAt/generationFailureReason) --
    // "generating" is derived by the frontend from these three fields, there is no separate state
    // enum for it. draftProcessDescription is the input text the async job reads (re-fetched fresh
    // in its own REQUIRES_NEW transaction, not passed through the async method call) so a retry
    // after failure needs no user re-entry and a page reload mid-generation loses nothing.
    @Column(name = "generation_requested_at")
    private LocalDateTime generationRequestedAt;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "generation_failure_reason", length = 1000)
    private String generationFailureReason;

    @Column(name = "draft_process_description", columnDefinition = "TEXT")
    private String draftProcessDescription;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
