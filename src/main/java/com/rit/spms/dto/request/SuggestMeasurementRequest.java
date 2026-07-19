package com.rit.spms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SuggestMeasurementRequest {
    @NotBlank(message = "Initiative title is required")
    private String initiativeTitle;

    private String initiativeDescription;
}
