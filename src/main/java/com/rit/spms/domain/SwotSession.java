package com.rit.spms.domain;

import com.rit.spms.domain.enums.SwotPhase;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "swot_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SwotSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "strategy_id", nullable = false, unique = true)
    private Strategy strategy;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SwotPhase phase = SwotPhase.COLLECTING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "started_by")
    private AppUser startedBy;

    @Column(name = "voting_closed_at")
    private LocalDateTime votingClosedAt;

    @Column(name = "suggestions_generated_at")
    private LocalDateTime suggestionsGeneratedAt;

    // Set the moment generation is requested (vote close, or an owner's manual retry) — committed
    // synchronously before the (possibly slow) @Async generation call even starts, so a client
    // polling status can show "submitted at X, Y elapsed" while it's still running.
    @Column(name = "generation_requested_at")
    private LocalDateTime generationRequestedAt;

    // Null while a generation attempt is in flight or has never failed; set when the AI provider
    // call throws, so the owner sees *why* it's stuck rather than guessing whether it's just slow.
    @Column(name = "generation_failure_reason", length = 1000)
    private String generationFailureReason;

    @Column(name = "review_locked_at")
    private LocalDateTime reviewLockedAt;

    @Column(name = "finalized_at")
    private LocalDateTime finalizedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
