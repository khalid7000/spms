package com.rit.spms.dto.response;

import com.rit.spms.domain.enums.SwotQuadrant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SwotEntryResponse {
    private Long id;
    private SwotQuadrant quadrant;
    private String word;
    private String justification;
    private Integer sortOrder;
}
