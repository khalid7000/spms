package com.rit.spms.platform.controller;

import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.platform.domain.Organization;
import com.rit.spms.platform.dto.request.RenameSlugRequest;
import com.rit.spms.platform.dto.response.OrganizationResponse;
import com.rit.spms.platform.dto.response.TenantUserSummaryResponse;
import com.rit.spms.platform.repository.OrganizationRepository;
import com.rit.spms.platform.service.OrganizationProvisioningService;
import com.rit.spms.platform.service.PlatformOrganizationUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/** Organization management for Super Admins -- behind {@code PlatformSecurityConfig}'s
 * filter chain (see that class for why this is a wholly separate auth boundary). */
@RestController
@RequestMapping("/api/platform/organizations")
@RequiredArgsConstructor
public class PlatformOrganizationController {

    private final OrganizationProvisioningService provisioningService;
    private final OrganizationRepository organizationRepository;
    private final PlatformOrganizationUserService organizationUserService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<OrganizationResponse>> createOrganization(
            @RequestParam String name,
            @RequestParam String slug,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "false") boolean isDefault,
            @RequestParam String adminEmail,
            @RequestParam String adminPassword,
            @RequestParam(required = false) MultipartFile logo) {
        Organization organization = provisioningService.createOrganization(
                name, slug, address, description, isDefault, adminEmail, adminPassword, logo);
        return ResponseEntity.status(201).body(ApiResponse.success("Organization created", toResponse(organization)));
    }

    private OrganizationResponse toResponse(Organization o) {
        return OrganizationResponse.builder()
                .id(o.getId())
                .name(o.getName())
                .slug(o.getSlug())
                .isDefault(o.getIsDefault())
                .logoPath(o.getLogoPath())
                .address(o.getAddress())
                .description(o.getDescription())
                .status(o.getStatus())
                .authMode(o.getAuthMode())
                .createdAt(o.getCreatedAt())
                .build();
    }

    @GetMapping("/slug-check")
    public ResponseEntity<ApiResponse<Boolean>> slugAvailable(@RequestParam String slug) {
        return ResponseEntity.ok(ApiResponse.success(!organizationRepository.existsBySlug(slug)));
    }

    @PatchMapping("/{id}/slug")
    public ResponseEntity<ApiResponse<OrganizationResponse>> renameSlug(
            @PathVariable Long id, @Valid @RequestBody RenameSlugRequest request) {
        Organization organization = provisioningService.renameSlug(id, request.getSlug());
        return ResponseEntity.ok(ApiResponse.success("Slug updated", toResponse(organization)));
    }

    @GetMapping("/{id}/users")
    public ResponseEntity<ApiResponse<List<TenantUserSummaryResponse>>> listUsers(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(organizationUserService.listUsers(id)));
    }

    @PostMapping("/{id}/users/{userId}/reset-password")
    public ResponseEntity<ApiResponse<Map<String, String>>> resetPassword(
            @PathVariable Long id, @PathVariable Long userId) {
        String newPassword = organizationUserService.resetPassword(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Password reset", Map.of("newPassword", newPassword)));
    }
}
