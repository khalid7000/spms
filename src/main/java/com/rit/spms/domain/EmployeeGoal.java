package com.rit.spms.domain;

import com.rit.spms.domain.enums.PortfolioReviewActionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee_goal", uniqueConstraints = @UniqueConstraint(columnNames = {"cycle_id", "goal_title"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cycle_id", nullable = false)
    private EmployeeGoalCycle cycle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private PortfolioCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "measurement_id")
    private Measurement measurement;

    @Column(name = "goal_title", nullable = false, length = 500)
    private String goalTitle;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    // Employee's stage-2 review of this materialized goal. Sequential single-actor review
    // (leader decides first, then employee), so this lives as columns here rather than a
    // separate reviewer-keyed table. REJECT is never valid here -- enforced in the service.
    @Enumerated(EnumType.STRING)
    @Column(name = "employee_action_type", length = 30)
    private PortfolioReviewActionType employeeActionType;

    @Column(name = "employee_edited_title", length = 500)
    private String employeeEditedTitle;

    @Column(name = "employee_edited_description", columnDefinition = "TEXT")
    private String employeeEditedDescription;

    @Column(name = "employee_reviewed_at")
    private LocalDateTime employeeReviewedAt;

    @Column(name = "rubric_unsatisfactory", columnDefinition = "TEXT")
    private String rubricUnsatisfactory;

    @Column(name = "rubric_meets_expectations", columnDefinition = "TEXT")
    private String rubricMeetsExpectations;

    @Column(name = "rubric_exceeds_expectations", columnDefinition = "TEXT")
    private String rubricExceedsExpectations;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
