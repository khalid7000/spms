package com.rit.spms.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * One row per goal (from the employee's deployed {@link EmployeeGoalCycle} for the evaluation's
 * academic year) under an {@link AnnualEvaluation} -- lets the employee explicitly declare "nothing
 * to report" for a goal, the same way {@link AnnualEvaluationCriteriaResult} does for a criteria.
 */
@Entity
@Table(name = "annual_evaluation_goal_result", uniqueConstraints = @UniqueConstraint(columnNames = {"evaluation_id", "goal_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnualEvaluationGoalResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_id", nullable = false)
    private AnnualEvaluation evaluation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private EmployeeGoal goal;

    @Column(name = "employee_nothing_to_report", nullable = false)
    @Builder.Default
    private Boolean employeeNothingToReport = false;

    /** The head's 1-5 rank for this goal -- rated the same way as a category, required before the head can submit. */
    @Column(name = "head_goal_rank")
    private Integer headGoalRank;
}
