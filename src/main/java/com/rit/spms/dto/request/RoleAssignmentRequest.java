package com.rit.spms.dto.request;

import com.rit.spms.domain.enums.RoleType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoleAssignmentRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Role is required")
    private RoleType role;
}
