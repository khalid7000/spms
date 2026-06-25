package com.rit.spms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateStrategyRequest {
    @NotNull(message = "Planning cycle ID is required")
    private Long planningCycleId;

    private Long departmentId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
}
