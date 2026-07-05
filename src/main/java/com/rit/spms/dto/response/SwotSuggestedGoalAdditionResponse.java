package com.rit.spms.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SwotSuggestedGoalAdditionResponse {
    private Long id;
    private Long swotSuggestionId;
    private Long proposedById;
    private String proposedByName;
    private String title;
    private String description;
    private LocalDateTime createdAt;
}
