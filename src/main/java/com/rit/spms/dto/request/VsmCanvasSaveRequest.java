package com.rit.spms.dto.request;

import com.rit.spms.domain.enums.VsmEdgeType;
import com.rit.spms.domain.enums.VsmNodeType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Bulk "save the whole canvas" payload -- one call per save session rather than one per drag, since
 * a React Flow canvas edit session naturally batches many local changes before the user hits Save.
 * A node with {@code id == null} is brand new; its {@code tempId} (client-generated, e.g. a random
 * string) is how edges in the same payload can reference it before it has a real database id -- the
 * same mechanism the future AI-draft-to-canvas flow (Phase 2) will reuse for nodes/edges the AI
 * proposed but nothing has persisted yet. Any existing node/edge not present in this payload is
 * deleted (this is a full-replace of the map's node/edge/metric set, not a diff).
 */
@Data
public class VsmCanvasSaveRequest {
    @NotNull
    @Valid
    private List<NodeItem> nodes;

    @NotNull
    @Valid
    private List<EdgeItem> edges;

    @Data
    public static class NodeItem {
        /** Null for a brand-new node; set to update an existing one. */
        private Long id;

        /** Required when id is null -- lets edges in the same payload reference this new node. */
        private String tempId;

        @NotNull
        private VsmNodeType nodeType;

        @NotNull
        private Double positionX;

        @NotNull
        private Double positionY;

        @NotBlank
        private String title;

        private String description;
        private BigDecimal cycleTimeMinutes;
        private BigDecimal completeAccuratePercent;
        private BigDecimal failRatePercent;

        @Valid
        private List<MetricItem> metrics;
    }

    @Data
    public static class MetricItem {
        @NotBlank
        private String label;

        @NotNull
        private BigDecimal value;

        private String unit;
        private Integer displayOrder;
    }

    @Data
    public static class EdgeItem {
        /** Either an existing node's real id (as a string) or another node item's tempId. */
        @NotBlank
        private String sourceRef;

        @NotBlank
        private String targetRef;

        private VsmEdgeType edgeType;
        private String label;
    }
}
