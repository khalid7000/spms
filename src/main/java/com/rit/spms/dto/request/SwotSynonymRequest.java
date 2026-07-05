package com.rit.spms.dto.request;

import com.rit.spms.domain.enums.SwotQuadrant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SwotSynonymRequest {
    @NotNull(message = "Quadrant is required")
    private SwotQuadrant quadrant;

    @NotBlank(message = "A partial word is required")
    private String partialWord;
}
