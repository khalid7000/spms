package com.rit.spms.domain;

import jakarta.persistence.*;
import lombok.*;

/** One row per criteria under an {@link AnnualEvaluation}'s categories: only the head rates at this level (1-5). */
@Entity
@Table(name = "annual_evaluation_criteria_result", uniqueConstraints = @UniqueConstraint(columnNames = {"evaluation_id", "criteria_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnualEvaluationCriteriaResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_id", nullable = false)
    private AnnualEvaluation evaluation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criteria_id", nullable = false)
    private CategoryCriteria criteria;

    @Column(name = "head_rank")
    private Integer headRank;

    /** Employee explicitly declares no achievements apply to this criteria this cycle -- an alternative to tagging an entry. */
    @Column(name = "employee_nothing_to_report", nullable = false)
    @Builder.Default
    private Boolean employeeNothingToReport = false;

    /** The employee's own optional reflection on this criteria -- editable in DRAFT and RETURNED_TO_EMPLOYEE. */
    @Column(name = "employee_comments", columnDefinition = "TEXT")
    private String employeeComments;
}
