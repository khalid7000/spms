package com.rit.spms.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class SwotAlternativeProposalRequest {
    @NotBlank(message = "Area name is required")
    private String name;

    private String rationale;

    @NotEmpty(message = "At least one goal is required")
    @Valid
    private List<SwotAlternativeGoalRequest> goals;
}
