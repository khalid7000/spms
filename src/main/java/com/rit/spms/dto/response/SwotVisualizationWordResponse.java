package com.rit.spms.dto.response;

import com.rit.spms.domain.enums.SwotQuadrant;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SwotVisualizationWordResponse {
    private SwotQuadrant quadrant;
    private String word;
    private int submitterCount;
    private List<SwotJustificationResponse> justifications;
}
