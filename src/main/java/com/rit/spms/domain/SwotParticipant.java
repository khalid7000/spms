package com.rit.spms.domain;

import com.rit.spms.domain.enums.RoleType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "swot_participant", uniqueConstraints = @UniqueConstraint(columnNames = {"swot_session_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SwotParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "swot_session_id", nullable = false)
    private SwotSession swotSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_at_invite", nullable = false, length = 20)
    private RoleType roleAtInvite;

    @CreationTimestamp
    @Column(name = "invited_at", nullable = false, updatable = false)
    private LocalDateTime invitedAt;

    @Column(name = "swot_submitted_at")
    private LocalDateTime swotSubmittedAt;

    @Column(name = "vote_submitted_at")
    private LocalDateTime voteSubmittedAt;

    @Column(name = "review_submitted_at")
    private LocalDateTime reviewSubmittedAt;
}
