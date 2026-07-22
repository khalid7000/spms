package com.rit.spms.dto.request;

import com.rit.spms.domain.enums.VsmMapState;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateVsmMapRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    /** Optional -- omit to leave the map's current state unchanged. */
    private VsmMapState state;
}
