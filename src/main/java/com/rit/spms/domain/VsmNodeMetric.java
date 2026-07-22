package com.rit.spms.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * One freeform metric attached to a VsmNode (e.g. "Rework loops / semester" = 2.3), for anything
 * beyond the three typed data-box columns already on VsmNode. Replaced wholesale on every canvas
 * save (see VsmMapService#saveCanvas) rather than diffed -- these are display values, not audited
 * history, so there's nothing lost by treating the list as fully owned by its parent node.
 */
@Entity
@Table(name = "vsm_node_metric")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VsmNodeMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "node_id", nullable = false)
    private VsmNode node;

    @Column(nullable = false, length = 200)
    private String label;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal value;

    @Column(length = 20)
    private String unit;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;
}
