package com.rit.spms.dto.response;

import com.rit.spms.domain.enums.SwotQuadrant;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SwotVoteBallotResponse {
    private SwotQuadrant quadrant;
    private int rankCount;
    private List<SwotBallotCandidateResponse> candidates;
}
