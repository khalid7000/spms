package com.rit.spms.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SwotAlternativeGoalResponse {
    private Long id;
    private String title;
    private String description;
    private Integer sortOrder;
}
