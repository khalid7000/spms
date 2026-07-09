package com.rit.spms.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee_goal_cycle", uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "academic_year_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeGoalCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private AppUser employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", nullable = false)
    private AppUser leader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private CycleState state = CycleState.DRAFT;

    @Column(name = "leader_strengths", columnDefinition = "TEXT")
    private String leaderStrengths;

    @Column(name = "leader_weaknesses", columnDefinition = "TEXT")
    private String leaderWeaknesses;

    @Column(name = "leader_submitted_at")
    private LocalDateTime leaderSubmittedAt;

    @Column(name = "employee_accepted_at")
    private LocalDateTime employeeAcceptedAt;

    // Typed-name signature captured when the employee accepts and deploys their goals -- an
    // explicit affirmation, not just a click-through confirm.
    @Column(name = "employee_signature_name", length = 200)
    private String employeeSignatureName;

    // Same async-generation status tracking as SwotSession: set the moment AI suggestion
    // generation is requested (committed synchronously, before the @Async call even starts) so a
    // client polling right afterward already sees it -- lets the UI show "submitted at X, Y
    // elapsed" for the whole time the background call runs, not just after it finishes.
    @Column(name = "generation_requested_at")
    private LocalDateTime generationRequestedAt;

    @Column(name = "suggestions_generated_at")
    private LocalDateTime suggestionsGeneratedAt;

    // Null while a generation attempt is in flight or has never failed; set when the AI provider
    // call throws, so the leader sees *why* it's stuck rather than guessing whether it's just slow.
    @Column(name = "generation_failure_reason", length = 1000)
    private String generationFailureReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum CycleState {
        DRAFT, LEADER_SUBMITTED, EMPLOYEE_REVIEW, EMPLOYEE_SUBMITTED, DEPLOYED, ARCHIVED
    }
}
