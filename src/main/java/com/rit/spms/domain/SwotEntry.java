package com.rit.spms.domain;

import com.rit.spms.domain.enums.SwotQuadrant;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "swot_entry")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SwotEntry {

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

    @Column(nullable = false, length = 100)
    private String word;

    @Column(name = "normalized_word", nullable = false, length = 100)
    private String normalizedWord;

    @Column(nullable = false, length = 500)
    private String justification;

    @Builder.Default
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
