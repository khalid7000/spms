package com.rit.spms.domain;

import com.rit.spms.domain.enums.VsmTaskState;
import com.rit.spms.domain.enums.VsmTaskType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * An improvement task spawned from a Kaizen-burst node -- the Kanban unit of work faculty browse
 * and pull. {@code linkedInitiative} is an optional reporting link set at creation (decision #1 in
 * the round-1 VSM plan); {@code achievement} is only ever set when an IMPROVEMENT task completes
 * (Phase 4) -- MINOR tasks close without ever touching it, and no Achievement gets created for them
 * at all. There is deliberately no separate KanbanBoard entity: "per-map board" and "department
 * rollup" are both just queries over this table (see VsmBoardService).
 */
@Entity
@Table(name = "improvement_task")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImprovementTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kaizen_node_id", nullable = false)
    private VsmNode kaizenNode;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false, length = 20)
    private VsmTaskType taskType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VsmTaskState state = VsmTaskState.BACKLOG;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private AppUser createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pulled_by")
    private AppUser pulledBy;

    @Column(name = "pulled_at")
    private LocalDateTime pulledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_initiative_id")
    private Initiative linkedInitiative;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", unique = true)
    private Achievement achievement;

    @Column(name = "done_at")
    private LocalDateTime doneAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
