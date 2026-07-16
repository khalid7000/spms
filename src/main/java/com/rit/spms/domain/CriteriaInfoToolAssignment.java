package com.rit.spms.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Admin assignment of a {@link com.rit.spms.service.CriteriaInfoTool} (identified by its stable
 * code) to one {@link CategoryCriteria} -- the head-only-viewer counterpart to {@link
 * CriteriaAchievementModule}. Same "at most one assignment per (tool, repository source type) per
 * EmployeeTitle" rule, enforced in {@code PortfolioCategoryService} for the same reason (the title
 * relationship spans criteria -> category -> title, so the schema alone can't express it) --
 * {@code repositorySourceType} is included in that scope (and in the DB unique constraint) because
 * {@code CentralRepositoryViewerTool} behaves as a functionally distinct tool per source type
 * (Early Alert vs Grade Distribution), even though they share one {@code toolCode}. Unlike
 * achievement modules, the display name has no hardcoded fallback -- an Admin must always set one,
 * since the tool implementation itself carries no display name (see {@code CriteriaInfoTool}).
 */
@Entity
@Table(name = "criteria_info_tool_assignment", uniqueConstraints = @UniqueConstraint(columnNames = {"tool_code", "repository_source_type", "criteria_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CriteriaInfoToolAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tool_code", nullable = false, length = 100)
    private String toolCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criteria_id", nullable = false)
    private CategoryCriteria criteria;

    @Column(name = "display_name", nullable = false, length = 200)
    private String displayName;

    // CentralRepositoryViewerTool-specific setting riding along on the shared assignment row (same
    // pragmatic style as mandatory/maxAchievementsPerYear on CriteriaAchievementModule) -- a
    // hypothetical future non-repository tool would just leave this null.
    @Column(name = "repository_source_type", length = 100)
    private String repositorySourceType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
