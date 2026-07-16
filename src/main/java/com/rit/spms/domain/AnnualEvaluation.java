package com.rit.spms.domain;

import com.rit.spms.domain.enums.AnnualEvaluationState;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * One per employee per academic year. Employee tags achievements to criteria and self-assesses
 * each category, then the head rates every criterion/category and an overall rank, then either
 * party may sign (or the employee may refuse to sign with a rationale) -- see
 * {@code AnnualEvaluationService} for the exact edit-window/locking rules.
 */
@Entity
@Table(name = "annual_evaluation", uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "academic_year_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnualEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private AppUser employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "head_id", nullable = false)
    private AppUser head;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private AnnualEvaluationState state = AnnualEvaluationState.DRAFT;

    @Column(name = "head_overall_rank")
    private Integer headOverallRank;

    /** The head's written comments for the whole Annual Goals section, split into two required parts -- parallel to how each category has its own. */
    @Column(name = "goals_head_comments_strengths", columnDefinition = "TEXT")
    private String goalsHeadCommentsStrengths;

    @Column(name = "goals_head_comments_improvements", columnDefinition = "TEXT")
    private String goalsHeadCommentsImprovements;

    /** The employee's 1-5 self-rank for the whole Annual Goals section -- one field, parallel to how each category has its own. */
    @Column(name = "goals_employee_self_rank")
    private Integer goalsEmployeeSelfRank;

    /** The head's 1-5 rank for the whole Annual Goals section -- parallel to headCategoryRank on AnnualEvaluationCategoryResult. */
    @Column(name = "goals_head_rank")
    private Integer goalsHeadRank;

    // ─── Next Cycle Goals: drafted/reviewed during this evaluation, reused later in Team Goal
    // Setting once concluded (see AnnualEvaluationNextCycleGoal). Notes here mirror
    // EmployeeGoalCycle.leaderStrengths/leaderWeaknesses -- same purpose, different lifecycle.

    @Column(name = "next_cycle_notes_strengths", columnDefinition = "TEXT")
    private String nextCycleNotesStrengths;

    @Column(name = "next_cycle_notes_weaknesses", columnDefinition = "TEXT")
    private String nextCycleNotesWeaknesses;

    @Column(name = "next_cycle_generation_requested_at")
    private LocalDateTime nextCycleGenerationRequestedAt;

    @Column(name = "next_cycle_generated_at")
    private LocalDateTime nextCycleGeneratedAt;

    @Column(name = "next_cycle_generation_failure_reason", length = 1000)
    private String nextCycleGenerationFailureReason;

    @Column(name = "employee_submitted_at")
    private LocalDateTime employeeSubmittedAt;

    /** Set the one time the head sends this back to the employee for another round; a non-null value means that round has already been used. */
    @Column(name = "returned_to_employee_at")
    private LocalDateTime returnedToEmployeeAt;

    /** The employee's own reflection on the whole Annual Goals section -- editable in DRAFT and RETURNED_TO_EMPLOYEE, shown before the head's comments. */
    @Column(name = "goals_employee_comments", columnDefinition = "TEXT")
    private String goalsEmployeeComments;

    /** The employee's required closing statement for the whole evaluation -- distinct from the per-category/goals reflections. */
    @Column(name = "employee_final_summary", columnDefinition = "TEXT")
    private String employeeFinalSummary;

    @Column(name = "head_submitted_at")
    private LocalDateTime headSubmittedAt;

    @Column(name = "head_signed_at")
    private LocalDateTime headSignedAt;

    /** The head's typed full name, captured at the moment of signing -- the affirmative act that records the signature. */
    @Column(name = "head_signature_name", length = 200)
    private String headSignatureName;

    @Column(name = "employee_signed_at")
    private LocalDateTime employeeSignedAt;

    /** The employee's typed full name, captured at the moment of signing. */
    @Column(name = "employee_signature_name", length = 200)
    private String employeeSignatureName;

    @Column(name = "employee_refused", nullable = false)
    @Builder.Default
    private Boolean employeeRefused = false;

    @Column(name = "employee_refusal_rationale", columnDefinition = "TEXT")
    private String employeeRefusalRationale;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** Once either party has acted, all rank/self-assessment edits are frozen for both. */
    public boolean isLocked() {
        return headSignedAt != null || employeeSignedAt != null || Boolean.TRUE.equals(employeeRefused);
    }

    public boolean isConcluded() {
        return headSignedAt != null && (employeeSignedAt != null || Boolean.TRUE.equals(employeeRefused));
    }
}
