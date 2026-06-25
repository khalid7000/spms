package com.rit.spms.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class MeasurementResponse {
    private Long id;
    private Long initiativeId;
    private String description;
    private String unit;
    private BigDecimal targetValue;
    private BigDecimal actualValue;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
