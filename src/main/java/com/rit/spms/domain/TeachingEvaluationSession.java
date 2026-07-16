package com.rit.spms.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * A short-lived working area for the "Teaching Evaluations" achievement module -- the employee
 * uploads course-evaluation files (parsed to plain text on upload, binary discarded) and the AI
 * drafts a "Details" blob from the accumulated text. Discarded once finalized into a real
 * Achievement/PortfolioEntry (see TeachingEvaluationSessionService.finalizeAchievement).
 */
@Entity
@Table(name = "teaching_evaluation_session", uniqueConstraints = @UniqueConstraint(columnNames = {"evaluation_id", "criteria_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeachingEvaluationSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_id", nullable = false)
    private AnnualEvaluation evaluation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criteria_id", nullable = false)
    private CategoryCriteria criteria;

    // Plain reference note for the employee's own context -- never read, never validated, never
    // sent to the AI (confirmed with user).
    @Column(name = "local_folder_note", length = 1000)
    private String localFolderNote;

    // Concatenated extracted text from every uploaded file so far.
    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

    // Display-only list of uploaded file names (comma-joined) -- no binary is retained.
    @Column(name = "uploaded_file_names", length = 2000)
    private String uploadedFileNames;

    // Same async-generation status tracking pattern as EmployeeGoalCycle/AnnualEvaluation's
    // next-cycle-goal generation.
    @Column(name = "generation_requested_at")
    private LocalDateTime generationRequestedAt;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "generation_failure_reason", length = 1000)
    private String generationFailureReason;

    @Column(name = "draft_details", columnDefinition = "TEXT")
    private String draftDetails;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
