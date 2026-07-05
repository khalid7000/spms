package com.rit.spms.dto.request;

import com.rit.spms.domain.enums.SwotQuadrant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SwotWordRequest {
    @NotNull(message = "Quadrant is required")
    private SwotQuadrant quadrant;

    @NotBlank(message = "Word is required")
    @Size(max = 100, message = "Word must be 100 characters or fewer")
    private String word;

    @NotBlank(message = "Justification is required")
    @Size(max = 500, message = "Justification must be 500 characters or fewer")
    private String justification;
}
