package com.rit.spms.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Thin 1:1 evaluation extension of an {@link Achievement} — the achievement record itself
 * (title, details, measurement) lives entirely on Achievement; this only carries the
 * portfolio-specific evaluation metadata (category, rating, goal link).
 */
@Entity
@Table(name = "portfolio_entry")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", nullable = false, unique = true)
    private Achievement achievement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private AppUser employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private PortfolioCategory category;

    /**
     * Which criteria within the category this achievement counts toward. Nullable because
     * entries logged before criteria-level tagging existed have none; going forward the logging
     * form requires one, and an Annual Evaluation can't be submitted while any in-period entry
     * is still missing this.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criteria_id")
    private CategoryCriteria criteria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    private EmployeeGoal goal;

    @Column(name = "category_rating")
    private Integer categoryRating;

    @Column(name = "evidence_url", length = 500)
    private String evidenceUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Whether this entry counts toward the given academic year -- matched by the achievement's own
     * assessment period name (the same convention used by AchievementService.isYearFrozen and the
     * Strategy Tree's year-linkage backfill), not the wall-clock moment it was recorded. Falls back
     * to a recordedAt date-range check only for legacy achievements with no period at all. Shared
     * by AnnualEvaluationService (which achievements count toward an evaluation) and
     * PortfolioEntryService (year-scoped "My Portfolio" totals) so both use one definition of
     * "belongs to this year" instead of drifting into their own slightly different checks.
     */
    public boolean belongsToAcademicYear(AcademicYear year) {
        AssessmentPeriod period = achievement.getAssessmentPeriod();
        if (period != null) {
            return period.getName().equals(year.getName());
        }
        if (year.getStartDate() == null || year.getEndDate() == null) {
            return true;
        }
        var recordedDate = achievement.getRecordedAt().toLocalDate();
        return !recordedDate.isBefore(year.getStartDate()) && !recordedDate.isAfter(year.getEndDate());
    }
}
