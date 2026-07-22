package com.rit.spms.dto.request;

import com.rit.spms.domain.enums.DelegationScopeType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateApprovalDelegationRequest {
    @NotNull(message = "Delegate is required")
    private Long delegateId;

    @NotNull(message = "Scope type is required")
    private DelegationScopeType scopeType;

    @NotNull(message = "Scope id is required")
    private Long scopeId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;
}
