package com.rit.spms.dto.request;

import com.rit.spms.domain.enums.VsmScopeType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateVsmAuthorGrantRequest {
    @NotNull(message = "Employee is required")
    private Long employeeId;

    @NotNull(message = "Scope type is required")
    private VsmScopeType scopeType;

    @NotNull(message = "Scope id is required")
    private Long scopeId;
}
