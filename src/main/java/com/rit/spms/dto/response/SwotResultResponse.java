package com.rit.spms.dto.response;

import com.rit.spms.domain.enums.SwotQuadrant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SwotResultResponse {
    private SwotQuadrant quadrant;
    private String word;
    private int totalScore;
    private int rankPosition;
}
