package com.rit.spms.domain;

import com.rit.spms.domain.enums.PortfolioReviewActionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee_goal_suggestion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeGoalSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cycle_id", nullable = false)
    private EmployeeGoalCycle cycle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private PortfolioCategory category;

    @Column(name = "suggested_title", nullable = false, length = 500)
    private String suggestedTitle;

    @Column(name = "suggested_description", columnDefinition = "TEXT")
    private String suggestedDescription;

    @Column(columnDefinition = "TEXT")
    private String rationale;

    /** Null means leader-authored ("Add a new goal"), not AI-generated. */
    @Column(name = "generated_by_model", length = 100)
    private String generatedByModel;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    // Leader's stage-1 review of this suggestion.
    @Enumerated(EnumType.STRING)
    @Column(name = "leader_action_type", length = 30)
    private PortfolioReviewActionType leaderActionType;

    @Column(name = "edited_title", length = 500)
    private String editedTitle;

    @Column(name = "edited_description", columnDefinition = "TEXT")
    private String editedDescription;

    @Column(name = "leader_reviewed_at")
    private LocalDateTime leaderReviewedAt;

    @Column(name = "rubric_unsatisfactory", columnDefinition = "TEXT")
    private String rubricUnsatisfactory;

    @Column(name = "rubric_meets_expectations", columnDefinition = "TEXT")
    private String rubricMeetsExpectations;

    @Column(name = "rubric_exceeds_expectations", columnDefinition = "TEXT")
    private String rubricExceedsExpectations;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
