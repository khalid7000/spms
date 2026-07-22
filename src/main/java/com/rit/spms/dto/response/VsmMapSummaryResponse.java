package com.rit.spms.dto.response;

import com.rit.spms.domain.VsmMap;
import com.rit.spms.domain.enums.VsmMapState;
import com.rit.spms.domain.enums.VsmScopeType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** Row shape for the VSM map list page -- no nodes/edges, just enough to list and route into one. */
@Data
@Builder
public class VsmMapSummaryResponse {
    private Long id;
    private VsmScopeType scopeType;
    private Long departmentId;
    private String departmentName;
    private Long orgGroupId;
    private String orgGroupName;
    private String title;
    private VsmMapState state;
    private String createdByName;
    private LocalDateTime updatedAt;

    public static VsmMapSummaryResponse from(VsmMap map) {
        return VsmMapSummaryResponse.builder()
                .id(map.getId())
                .scopeType(map.getScopeType())
                .departmentId(map.getDepartment() != null ? map.getDepartment().getId() : null)
                .departmentName(map.getDepartment() != null ? map.getDepartment().getName() : null)
                .orgGroupId(map.getOrgGroup() != null ? map.getOrgGroup().getId() : null)
                .orgGroupName(map.getOrgGroup() != null ? map.getOrgGroup().getTitle() : null)
                .title(map.getTitle())
                .state(map.getState())
                .createdByName(map.getCreatedBy().getFname() + " " + map.getCreatedBy().getLname())
                .updatedAt(map.getUpdatedAt())
                .build();
    }
}
