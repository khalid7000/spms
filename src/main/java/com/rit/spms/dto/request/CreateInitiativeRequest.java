package com.rit.spms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateInitiativeRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private Integer sortOrder = 0;
    private Long universityInitiativeId;
}
