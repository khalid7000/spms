package com.rit.spms.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Admin assignment of a {@link com.rit.spms.service.CustomizableAchievementModule} (identified by
 * its stable code) to one {@link CategoryCriteria}. A given module code has at most one such row
 * per EmployeeTitle -- enforced in {@code PortfolioCategoryService}, not the schema, since "one
 * criterion per title" spans the criteria -> category -> title chain.
 */
@Entity
@Table(name = "criteria_achievement_module", uniqueConstraints = @UniqueConstraint(columnNames = {"module_code", "criteria_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CriteriaAchievementModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "module_code", nullable = false, length = 100)
    private String moduleCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criteria_id", nullable = false)
    private CategoryCriteria criteria;

    // Required, positive -- how many achievements an employee may record through this module for
    // this criterion within a single academic year. Enforced in TeachingEvaluationSessionService
    // (and mirrored by every other module's session service, once more modules exist).
    @Column(name = "max_achievements_per_year", nullable = false)
    private Integer maxAchievementsPerYear;

    // Optional by default -- when true, the employee cannot submit their Annual Evaluation
    // self-assessment without at least one achievement from this module for this criterion,
    // overriding "nothing to report" (enforced in AnnualEvaluationService.submitEmployeeSelfAssessment).
    @Column(name = "mandatory", nullable = false)
    @Builder.Default
    private Boolean mandatory = false;

    // Null means "use the module's hardcoded buttonLabel" (CustomizableAchievementModule.getButtonLabel())
    // -- an Admin can override the label shown on this specific criterion's button without touching Java.
    @Column(name = "display_name", length = 200)
    private String displayName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
