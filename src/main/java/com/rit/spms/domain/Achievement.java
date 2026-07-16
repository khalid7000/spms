package com.rit.spms.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "achievement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Null for achievements produced by a customizable achievement module (see
    // CustomizableAchievementModule) -- those are evaluation-only and deliberately not linked to
    // the Strategy Tree. Every Strategy/Initiative-side query reaches Achievement through a
    // measurementId, so a null-measurement row simply never surfaces there.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "measurement_id")
    private Measurement measurement;

    @Column(nullable = false, length = 500)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_type_id", nullable = false)
    private AchievementType achievementType;

    // Populated only when achievementType is the "Other" preset -- lets the user describe their
    // own type instead of being forced into the closest existing option.
    @Column(name = "custom_type_name", length = 200)
    private String customTypeName;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "private_notes", columnDefinition = "TEXT")
    private String privateNotes;

    // Set only when this achievement was produced by a CustomizableAchievementModule tool (e.g.
    // "TEACHING_EVALUATIONS"), never by the employee choosing it themselves -- null for achievements
    // logged the normal, manual way. That module fixed this achievement's category/criteria at
    // creation time (see TeachingEvaluationSessionService.finalizeAchievement), so callers use this
    // field to keep those two fields locked afterward too, even though the row itself is a normal
    // PortfolioEntry/Achievement from then on.
    @Column(name = "created_by_module_code", length = 100)
    private String createdByModuleCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private AppUser author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_period_id")
    private AssessmentPeriod assessmentPeriod;

    @CreationTimestamp
    @Column(name = "recorded_at", nullable = false, updatable = false)
    private LocalDateTime recordedAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** Display name: the typed custom type when set (achievementType is "Other"), else the preset's own name. */
    public String getEffectiveTypeName() {
        return (customTypeName != null && !customTypeName.isBlank()) ? customTypeName : achievementType.getName();
    }
}
