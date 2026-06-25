package com.rit.spms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateVisionAreaRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private Integer sortOrder = 0;
}
