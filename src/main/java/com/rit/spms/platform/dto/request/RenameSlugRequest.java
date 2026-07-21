package com.rit.spms.platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RenameSlugRequest {
    @NotBlank(message = "Slug is required")
    private String slug;
}
