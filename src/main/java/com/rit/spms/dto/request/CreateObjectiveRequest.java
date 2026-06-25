package com.rit.spms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateObjectiveRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private Integer sortOrder = 0;
    private List<Long> universityObjectiveIds;
}
