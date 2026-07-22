package com.rit.spms.dto.response;

import com.rit.spms.domain.VsmEdge;
import com.rit.spms.domain.enums.VsmEdgeType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VsmEdgeResponse {
    private Long id;
    private Long sourceNodeId;
    private Long targetNodeId;
    private VsmEdgeType edgeType;
    private String label;

    public static VsmEdgeResponse from(VsmEdge edge) {
        return VsmEdgeResponse.builder()
                .id(edge.getId())
                .sourceNodeId(edge.getSourceNode().getId())
                .targetNodeId(edge.getTargetNode().getId())
                .edgeType(edge.getEdgeType())
                .label(edge.getLabel())
                .build();
    }
}
