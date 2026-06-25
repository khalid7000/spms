package com.rit.spms.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class InitiativeResponse {
    private Long id;
    private Long objectiveId;
    private String title;
    private String description;
    private Integer sortOrder;
    private Long universityInitiativeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<MeasurementResponse> measurements;
}
