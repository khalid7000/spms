package com.rit.spms.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SwotAlternativeProposalResponse {
    private Long id;
    private Long proposedById;
    private String proposedByName;
    private String name;
    private String rationale;
    private LocalDateTime createdAt;
    private List<SwotAlternativeGoalResponse> goals;
}
