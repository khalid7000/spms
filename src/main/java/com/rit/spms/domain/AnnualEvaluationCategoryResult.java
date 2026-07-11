package com.rit.spms.domain;

import jakarta.persistence.*;
import lombok.*;

/** One row per category applicable to an {@link AnnualEvaluation}: the employee's self-rank and the head's rank for that category. */
@Entity
@Table(name = "annual_evaluation_category_result", uniqueConstraints = @UniqueConstraint(columnNames = {"evaluation_id", "category_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnualEvaluationCategoryResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_id", nullable = false)
    private AnnualEvaluation evaluation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private PortfolioCategory category;

    @Column(name = "employee_self_rank")
    private Integer employeeSelfRank;

    @Column(name = "head_category_rank")
    private Integer headCategoryRank;

    /** The head's written comments for this category, split into two required parts -- before submitting their evaluation. */
    @Column(name = "head_comments_strengths", columnDefinition = "TEXT")
    private String headCommentsStrengths;

    @Column(name = "head_comments_improvements", columnDefinition = "TEXT")
    private String headCommentsImprovements;
}
