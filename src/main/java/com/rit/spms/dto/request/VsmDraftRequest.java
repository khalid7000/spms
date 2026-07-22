package com.rit.spms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Kicks off (or retries) async AI draft generation for an already-created map -- see
 *  VsmDraftGenerationService. The map itself carries the scope; permission is checked the same way
 *  as any other edit to it. */
@Data
public class VsmDraftRequest {
    @NotBlank(message = "Process description is required")
    private String processDescription;
}
