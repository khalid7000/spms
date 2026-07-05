package com.rit.spms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SwotAlternativeGoalRequest {
    @NotBlank(message = "Goal title is required")
    private String title;

    private String description;
}
