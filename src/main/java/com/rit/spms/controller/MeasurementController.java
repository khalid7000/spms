package com.rit.spms.controller;

import com.rit.spms.domain.Measurement;
import com.rit.spms.dto.request.CreateMeasurementRequest;
import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.dto.response.MeasurementResponse;
import com.rit.spms.security.UserPrincipal;
import com.rit.spms.service.MeasurementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MeasurementController {

    private final MeasurementService measurementService;

    @GetMapping("/api/initiatives/{initiativeId}/measurements")
    public ResponseEntity<ApiResponse<List<MeasurementResponse>>> getMeasurements(
            @PathVariable Long initiativeId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                measurementService.getMeasurements(initiativeId, principal.getId())));
    }

    @PostMapping("/api/initiatives/{initiativeId}/measurements")
    public ResponseEntity<ApiResponse<MeasurementResponse>> createMeasurement(
            @PathVariable Long initiativeId,
            @Valid @RequestBody CreateMeasurementRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        Measurement measurement = measurementService.createMeasurement(initiativeId, req, principal.getId());
        return ResponseEntity.status(201).body(
                ApiResponse.success("Measurement created", measurementService.toResponse(measurement)));
    }

    @PutMapping("/api/measurements/{id}")
    public ResponseEntity<ApiResponse<MeasurementResponse>> updateMeasurement(
            @PathVariable Long id,
            @Valid @RequestBody CreateMeasurementRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        Measurement measurement = measurementService.updateMeasurement(id, req, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(measurementService.toResponse(measurement)));
    }

    @DeleteMapping("/api/measurements/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMeasurement(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        measurementService.deleteMeasurement(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Measurement deleted", null));
    }
}
