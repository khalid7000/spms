package com.rit.spms.domain;

import com.rit.spms.domain.enums.SwotReviewActionType;
import com.rit.spms.domain.enums.SwotReviewTargetType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "swot_review_item",
        uniqueConstraints = @UniqueConstraint(columnNames = {"swot_session_id", "reviewer_id", "target_type", "target_id", "is_owner_final"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SwotReviewItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "swot_session_id", nullable = false)
    private SwotSession swotSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private AppUser reviewer;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private SwotReviewTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 30)
    private SwotReviewActionType actionType;

    @Column(name = "edited_title", length = 500)
    private String editedTitle;

    @Column(name = "edited_description", columnDefinition = "TEXT")
    private String editedDescription;

    @Builder.Default
    @Column(name = "is_owner_final", nullable = false)
    private Boolean isOwnerFinal = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
