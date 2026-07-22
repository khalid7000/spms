package com.rit.spms.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * A note an employee leaves on a task they're working on (or the map's author/admin leaves on any
 * task at any time). Permanent and author-attributed: reassigning the task, adding/removing other
 * collaborators, or the map's author stripping it back to the board (see
 * ImprovementTaskService#returnToBoard) never touches these rows -- only the {@link
 * ImprovementTaskAssignee} links change. No edit/delete in v1; it's a running log, not a
 * document.
 */
@Entity
@Table(name = "improvement_task_note")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImprovementTaskNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "improvement_task_id", nullable = false)
    private ImprovementTask improvementTask;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private AppUser author;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
