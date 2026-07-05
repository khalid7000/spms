package com.rit.spms.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/** A new goal an Editor or the Owner proposes under an existing AI-suggested area (not one of the AI's own goals). */
@Entity
@Table(name = "swot_suggested_goal_addition")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SwotSuggestedGoalAddition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "swot_suggestion_id", nullable = false)
    private SwotSuggestion swotSuggestion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposed_by", nullable = false)
    private AppUser proposedBy;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
