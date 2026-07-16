package com.rit.spms.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * A head's saved Rating Assistant word selections for one criterion or goal within an evaluation
 * -- lets them pick up exactly where they left off, any time, not just for the current browser
 * session. Strictly private: only ever read/written by the evaluation's own head (see
 * RatingAssistantSelectionService) -- never exposed to the employee or any other viewer.
 */
@Entity
@Table(name = "rating_assistant_selection",
       uniqueConstraints = @UniqueConstraint(columnNames = {"evaluation_id", "head_id", "target_type", "target_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingAssistantSelection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_id", nullable = false)
    private AnnualEvaluation evaluation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "head_id", nullable = false)
    private AppUser head;

    /** "CRITERIA" or "GOAL". */
    @Column(name = "target_type", nullable = false, length = 20)
    private String targetType;

    /** The criteria id or goal id, depending on targetType. */
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    // JSON array of selection keys (e.g. ["left-0","center-2"]) -- order preserved so "clear last"
    // can undo in the same sequence the head clicked them.
    @Column(name = "selection_history", nullable = false, columnDefinition = "TEXT")
    private String selectionHistory;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
