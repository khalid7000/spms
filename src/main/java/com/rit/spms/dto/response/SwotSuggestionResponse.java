package com.rit.spms.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SwotSuggestionResponse {
    private Long id;
    private String name;
    private String rationale;
    private Integer sortOrder;
    private String generatedByModel;
    private List<SwotSuggestedGoalResponse> goals;
}
