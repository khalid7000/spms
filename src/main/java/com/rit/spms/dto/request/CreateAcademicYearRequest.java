package com.rit.spms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateAcademicYearRequest {
    @NotBlank(message = "Name is required")
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    @NotNull(message = "A university-level strategy must be selected")
    private Long universityStrategyId;
}
