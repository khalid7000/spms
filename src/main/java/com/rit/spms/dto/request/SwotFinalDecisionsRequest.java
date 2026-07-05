package com.rit.spms.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class SwotFinalDecisionsRequest {
    @NotEmpty(message = "At least one decision is required")
    @Valid
    private List<SwotFinalDecisionRequest> decisions;
}
