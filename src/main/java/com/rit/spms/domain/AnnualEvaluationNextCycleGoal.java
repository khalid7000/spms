package com.rit.spms.domain;

import com.rit.spms.domain.enums.PortfolioReviewActionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * A goal for the employee's NEXT annual cycle, drafted and reviewed by both the head and the
 * employee during THIS evaluation's own review/sign exchange (not a separate approval workflow --
 * see AnnualEvaluationService). Once the evaluation concludes, whichever rows neither party
 * rejected are eligible to be pulled into a real EmployeeGoalCycle later from Team Goal Setting
 * (see EmployeeGoalCycleService.findReusableNextCycleGoals/useNextCycleGoals), at which point
 * `used`/`usedInCycle` are set so they're never offered again.
 */
@Entity
@Table(name = "annual_evaluation_next_cycle_goal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnualEvaluationNextCycleGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_id", nullable = false)
    private AnnualEvaluation evaluation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private PortfolioCategory category;

    @Column(name = "suggested_title", nullable = false, length = 500)
    private String suggestedTitle;

    @Column(name = "suggested_description", columnDefinition = "TEXT")
    private String suggestedDescription;

    @Column(columnDefinition = "TEXT")
    private String rationale;

    /** Null means head-authored, not AI-generated. */
    @Column(name = "generated_by_model", length = 100)
    private String generatedByModel;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "rubric_unsatisfactory", columnDefinition = "TEXT")
    private String rubricUnsatisfactory;

    @Column(name = "rubric_meets_expectations", columnDefinition = "TEXT")
    private String rubricMeetsExpectations;

    @Column(name = "rubric_exceeds_expectations", columnDefinition = "TEXT")
    private String rubricExceedsExpectations;

    // The head's own review, editable during the head's normal edit window.
    @Enumerated(EnumType.STRING)
    @Column(name = "leader_action_type", length = 30)
    private PortfolioReviewActionType leaderActionType;

    @Column(name = "leader_edited_title", length = 500)
    private String leaderEditedTitle;

    @Column(name = "leader_edited_description", columnDefinition = "TEXT")
    private String leaderEditedDescription;

    @Column(name = "leader_reviewed_at")
    private LocalDateTime leaderReviewedAt;

    // The employee's own review -- required before they may sign or refuse the evaluation.
    // Unlike EmployeeGoal's stage-2 employee review, REJECT is valid here: this is still just a
    // proposal for a future cycle, not an already-materialized goal.
    @Enumerated(EnumType.STRING)
    @Column(name = "employee_action_type", length = 30)
    private PortfolioReviewActionType employeeActionType;

    @Column(name = "employee_edited_title", length = 500)
    private String employeeEditedTitle;

    @Column(name = "employee_edited_description", columnDefinition = "TEXT")
    private String employeeEditedDescription;

    @Column(name = "employee_reviewed_at")
    private LocalDateTime employeeReviewedAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean used = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "used_in_cycle_id")
    private EmployeeGoalCycle usedInCycle;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
