package com.rit.spms.dto.response;

import com.rit.spms.domain.VsmNode;
import com.rit.spms.domain.VsmNodeMetric;
import com.rit.spms.domain.enums.VsmNodeType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class VsmNodeResponse {
    private Long id;
    private VsmNodeType nodeType;
    private Double positionX;
    private Double positionY;
    private String title;
    private String description;
    private BigDecimal cycleTimeMinutes;
    private BigDecimal completeAccuratePercent;
    private BigDecimal failRatePercent;
    private List<MetricItem> metrics;

    public static VsmNodeResponse from(VsmNode node) {
        return VsmNodeResponse.builder()
                .id(node.getId())
                .nodeType(node.getNodeType())
                .positionX(node.getPositionX())
                .positionY(node.getPositionY())
                .title(node.getTitle())
                .description(node.getDescription())
                .cycleTimeMinutes(node.getCycleTimeMinutes())
                .completeAccuratePercent(node.getCompleteAccuratePercent())
                .failRatePercent(node.getFailRatePercent())
                .metrics(node.getMetrics().stream().map(VsmNodeResponse::metricFrom).toList())
                .build();
    }

    private static MetricItem metricFrom(VsmNodeMetric m) {
        return MetricItem.builder()
                .id(m.getId())
                .label(m.getLabel())
                .value(m.getValue())
                .unit(m.getUnit())
                .displayOrder(m.getDisplayOrder())
                .build();
    }

    @Data
    @Builder
    public static class MetricItem {
        private Long id;
        private String label;
        private BigDecimal value;
        private String unit;
        private Integer displayOrder;
    }
}
