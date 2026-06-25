package com.rit.spms.dto.request;

import com.rit.spms.domain.enums.StrategyState;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeStateRequest {
    @NotNull(message = "New state is required")
    private StrategyState newState;
}
