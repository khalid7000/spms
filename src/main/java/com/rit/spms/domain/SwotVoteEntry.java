package com.rit.spms.domain;

import com.rit.spms.domain.enums.SwotQuadrant;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "swot_vote_entry")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SwotVoteEntry {

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
    @Column(nullable = false, length = 20)
    private SwotQuadrant quadrant;

    @Column(nullable = false)
    private Integer rank;

    @Column(name = "normalized_word", nullable = false, length = 100)
    private String normalizedWord;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
