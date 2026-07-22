package com.rit.spms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTaskNoteRequest {
    @NotBlank(message = "Note body is required")
    private String body;
}
