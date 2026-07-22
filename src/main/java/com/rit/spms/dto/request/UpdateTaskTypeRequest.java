package com.rit.spms.dto.request;

import com.rit.spms.domain.enums.VsmTaskType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateTaskTypeRequest {
    @NotNull(message = "Task type is required")
    private VsmTaskType taskType;
}
