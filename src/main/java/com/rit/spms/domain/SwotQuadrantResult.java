package com.rit.spms.domain;

import com.rit.spms.domain.enums.SwotQuadrant;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "swot_quadrant_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SwotQuadrantResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "swot_session_id", nullable = false)
    private SwotSession swotSession;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SwotQuadrant quadrant;

    @Column(name = "normalized_word", nullable = false, length = 100)
    private String normalizedWord;

    @Column(name = "display_word", nullable = false, length = 100)
    private String displayWord;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore;

    @Column(name = "rank_position", nullable = false)
    private Integer rankPosition;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
