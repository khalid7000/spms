package com.rit.spms.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * A single evaluation criteria within a {@link PortfolioCategory}. Besides the data faculty must
 * provide ({@link #description}), the admin sets a three-level evaluation rubric (Unsatisfactory /
 * Meets Expectations / Exceeds Expectations) here, per the Faculty Evaluation Rubrics Sheet -- the
 * head uses this rubric text as reference when giving the 1-5 rank during an Annual Evaluation.
 */
@Entity
@Table(name = "category_criteria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private PortfolioCategory category;

    @Column(name = "criteria_name", nullable = false, length = 300)
    private String criteriaName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "rubric_unsatisfactory", columnDefinition = "TEXT")
    private String rubricUnsatisfactory;

    @Column(name = "rubric_meets_expectations", columnDefinition = "TEXT")
    private String rubricMeetsExpectations;

    @Column(name = "rubric_exceeds_expectations", columnDefinition = "TEXT")
    private String rubricExceedsExpectations;
}
