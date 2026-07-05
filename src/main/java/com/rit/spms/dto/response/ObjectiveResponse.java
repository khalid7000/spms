package com.rit.spms.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ObjectiveResponse {
    private Long id;
    private Long goalId;
    private String title;
    private String description;
    private Integer sortOrder;
    private Boolean frozen;
    private List<Long> universityObjectiveIds;
    private List<String> universityObjectiveTitles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<InitiativeResponse> initiatives;
}
