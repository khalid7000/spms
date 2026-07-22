package com.rit.spms.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * A collaborator an owner (or the map's author/admin) added to a task -- they can see it in their
 * own task list and add notes, but only the task's recorded owner ({@code
 * ImprovementTask#pulledBy}) or an admin may change its state (see
 * ImprovementTaskService#assertIsPullerOrAdmin, deliberately not extended to cover this table).
 * Wiped entirely when the map's author returns the task to the board -- see
 * ImprovementTaskService#returnToBoard.
 */
@Entity
@Table(name = "improvement_task_assignee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImprovementTaskAssignee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "improvement_task_id", nullable = false)
    private ImprovementTask improvementTask;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private AppUser employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by_id", nullable = false)
    private AppUser addedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
