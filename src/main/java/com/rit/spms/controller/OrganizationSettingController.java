package com.rit.spms.controller;

import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.dto.response.OrganizationSettingResponse;
import com.rit.spms.service.OrganizationSettingService;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Admin-editable, fixed-key display labels (e.g. what to call an "Academic Year" for this
 *  organization) -- see OrganizationSettingService for why the key set can't grow via the API. */
@RestController
@RequestMapping("/api/admin/organization-settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class OrganizationSettingController {

    private final OrganizationSettingService settingService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrganizationSettingResponse>>> getAll() {
        List<OrganizationSettingResponse> settings = settingService.getAll()
                .stream().map(OrganizationSettingResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @PutMapping("/{key}")
    public ResponseEntity<ApiResponse<OrganizationSettingResponse>> update(
            @PathVariable String key, @jakarta.validation.Valid @RequestBody OrganizationSettingUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Setting updated",
                OrganizationSettingResponse.from(settingService.updateValue(key, req.getValue()))));
    }

    /** Read-only, for every authenticated user -- consumed by the frontend's terminology hook. */
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<OrganizationSettingResponse>>> getAllPublic() {
        List<OrganizationSettingResponse> settings = settingService.getAll()
                .stream().map(OrganizationSettingResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @Data
    public static class OrganizationSettingUpdateRequest {
        @NotBlank
        private String value;
    }
}
