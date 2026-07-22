package com.rit.spms.domain;

import com.rit.spms.domain.enums.VsmEdgeType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/** A connector between two VsmNodes on the same map. Replaced wholesale on every canvas save (see
 *  VsmMapService#saveCanvas) -- nothing else references an edge by id, so full delete/recreate per
 *  save is simpler than diffing and carries no downside. */
@Entity
@Table(name = "vsm_edge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VsmEdge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vsm_map_id", nullable = false)
    private VsmMap vsmMap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_node_id", nullable = false)
    private VsmNode sourceNode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_node_id", nullable = false)
    private VsmNode targetNode;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "edge_type", nullable = false, length = 20)
    private VsmEdgeType edgeType = VsmEdgeType.MATERIAL_FLOW;

    @Column(length = 200)
    private String label;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
