package com.rit.spms.domain;

import com.rit.spms.domain.enums.VsmNodeType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * One symbol on a VsmMap's canvas. positionX/positionY are React Flow canvas coordinates, persisted
 * as-is so the layout survives a reload. The three typed metric columns are the classic lean-VSM
 * data-box fields, meaningful mainly for PROCESS nodes but harmless/unused on the others; anything
 * beyond them (e.g. "rework loops/semester") goes in the freeform {@link #metrics} child list rather
 * than a JSONB column, matching this codebase's existing idiom for per-item flexible data (see
 * SwotQuadrantResult, AnnualEvaluationCriteriaResult) instead of introducing a new one.
 */
@Entity
@Table(name = "vsm_node")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VsmNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vsm_map_id", nullable = false)
    private VsmMap vsmMap;

    @Enumerated(EnumType.STRING)
    @Column(name = "node_type", nullable = false, length = 30)
    private VsmNodeType nodeType;

    @Column(name = "position_x", nullable = false)
    private Double positionX;

    @Column(name = "position_y", nullable = false)
    private Double positionY;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cycle_time_minutes", precision = 10, scale = 2)
    private BigDecimal cycleTimeMinutes;

    @Column(name = "complete_accurate_percent", precision = 5, scale = 2)
    private BigDecimal completeAccuratePercent;

    @Column(name = "fail_rate_percent", precision = 5, scale = 2)
    private BigDecimal failRatePercent;

    @Builder.Default
    @OneToMany(mappedBy = "node", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<VsmNodeMetric> metrics = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
