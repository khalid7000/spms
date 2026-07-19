package com.rit.spms.platform.controller;

import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.platform.dto.response.OrganizationStatsResponse;
import com.rit.spms.platform.service.PlatformDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** The Super Admin landing page's data source -- behind {@code PlatformSecurityConfig}'s
 * filter chain, same as every other {@code /api/platform/**} endpoint. */
@RestController
@RequestMapping("/api/platform/dashboard")
@RequiredArgsConstructor
public class PlatformDashboardController {

    private final PlatformDashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrganizationStatsResponse>>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getDashboard()));
    }
}
