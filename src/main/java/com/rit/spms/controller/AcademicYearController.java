package com.rit.spms.controller;

import com.rit.spms.domain.AcademicYear;
import com.rit.spms.dto.request.CreateAcademicYearRequest;
import com.rit.spms.dto.response.AcademicYearResponse;
import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.security.UserPrincipal;
import com.rit.spms.service.AcademicYearService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AcademicYearController {

    private final AcademicYearService academicYearService;

    @GetMapping("/api/academic-years")
    public ResponseEntity<ApiResponse<List<AcademicYearResponse>>> getAll() {
        List<AcademicYearResponse> years = academicYearService.getAll()
                .stream().map(AcademicYearResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.success(years));
    }

    @PostMapping("/api/admin/academic-years")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AcademicYearResponse>> create(
            @Valid @RequestBody CreateAcademicYearRequest req) {
        AcademicYear year = academicYearService.create(req.getName(), req.getStartDate(), req.getEndDate());
        return ResponseEntity.status(201).body(
                ApiResponse.success("Academic year created", AcademicYearResponse.from(year)));
    }

    @PatchMapping("/api/academic-years/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AcademicYearResponse>> close(@PathVariable Long id) {
        AcademicYear year = academicYearService.close(id);
        return ResponseEntity.ok(ApiResponse.success("Academic year closed", AcademicYearResponse.from(year)));
    }

    @PatchMapping("/api/academic-years/{id}/lock")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AcademicYearResponse>> lock(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        AcademicYear year = academicYearService.lock(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Academic year locked", AcademicYearResponse.from(year)));
    }

    @PatchMapping("/api/academic-years/{id}/unlock")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AcademicYearResponse>> unlock(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        AcademicYear year = academicYearService.unlock(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Academic year unlocked", AcademicYearResponse.from(year)));
    }
}
