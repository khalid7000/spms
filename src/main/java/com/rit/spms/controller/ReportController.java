package com.rit.spms.controller;

import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.dto.response.CoverageReportResponse;
import com.rit.spms.security.UserPrincipal;
import com.rit.spms.service.ExcelExportService;
import com.rit.spms.service.MappingService;
import com.rit.spms.service.PdfExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final MappingService mappingService;
    private final PdfExportService pdfExportService;
    private final ExcelExportService excelExportService;

    @GetMapping("/coverage/{strategyId}")
    public ResponseEntity<ApiResponse<CoverageReportResponse>> coverageReport(
            @PathVariable Long strategyId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                mappingService.getCoverageReport(strategyId, principal.getId())));
    }

    @GetMapping("/pdf/{strategyId}")
    public ResponseEntity<byte[]> pdfReport(
            @PathVariable Long strategyId,
            @AuthenticationPrincipal UserPrincipal principal) {
        byte[] pdf = pdfExportService.exportStrategy(strategyId, principal.getId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"strategy-report-" + strategyId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/excel/{strategyId}")
    public ResponseEntity<byte[]> excelReport(
            @PathVariable Long strategyId,
            @AuthenticationPrincipal UserPrincipal principal) {
        byte[] excel = excelExportService.exportStrategy(strategyId, principal.getId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"strategy-report-" + strategyId + ".xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
}
