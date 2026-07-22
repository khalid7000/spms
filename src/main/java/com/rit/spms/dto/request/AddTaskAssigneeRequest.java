package com.rit.spms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddTaskAssigneeRequest {
    @NotNull(message = "Employee is required")
    private Long employeeId;
}
