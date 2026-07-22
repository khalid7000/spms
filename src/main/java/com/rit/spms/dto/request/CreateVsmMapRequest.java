package com.rit.spms.dto.request;

import com.rit.spms.domain.enums.VsmScopeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateVsmMapRequest {
    @NotNull(message = "Scope type is required")
    private VsmScopeType scopeType;

    /** The Department id (scopeType=DEPARTMENT) or OrgGroup id (scopeType=ORG_GROUP) this map belongs to. */
    @NotNull(message = "Scope id is required")
    private Long scopeId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
}
