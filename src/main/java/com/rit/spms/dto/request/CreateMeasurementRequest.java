package com.rit.spms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateMeasurementRequest {
    @NotBlank(message = "Description is required")
    private String description;

    private String unit;
    private BigDecimal targetValue;
    private BigDecimal actualValue;
    private Integer sortOrder = 0;
}
