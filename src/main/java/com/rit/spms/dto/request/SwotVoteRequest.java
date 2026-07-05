package com.rit.spms.dto.request;

import com.rit.spms.domain.enums.SwotQuadrant;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SwotVoteRequest {
    /** Quadrant -> ranked words, best first (index 0 = rank 1). */
    @NotEmpty(message = "At least one quadrant ranking is required")
    private Map<SwotQuadrant, List<String>> rankedWordsByQuadrant;
}
