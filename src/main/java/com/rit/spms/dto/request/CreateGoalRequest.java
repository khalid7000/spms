package com.rit.spms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateGoalRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private Long themeId;
    private Integer sortOrder = 0;
}
