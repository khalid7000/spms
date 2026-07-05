package com.rit.spms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateAcademicYearRequest {
    @NotBlank(message = "Name is required")
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
}
