package com.rit.spms.controller;

import com.rit.spms.domain.Initiative;
import com.rit.spms.dto.request.CreateInitiativeRequest;
import com.rit.spms.dto.request.SuggestMeasurementRequest;
import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.dto.response.InitiativeResponse;
import com.rit.spms.security.UserPrincipal;
import com.rit.spms.service.InitiativeService;
import com.rit.spms.service.MeasurementSuggestionGenerator.SuggestedMeasurementDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class InitiativeController {

    private final InitiativeService initiativeService;

    @GetMapping("/api/objectives/{objectiveId}/initiatives")
    public ResponseEntity<ApiResponse<List<InitiativeResponse>>> getInitiatives(
            @PathVariable Long objectiveId,
            @RequestParam(required = false) Long academicYearId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                initiativeService.getInitiatives(objectiveId, academicYearId, principal.getId())));
    }

    @PostMapping("/api/objectives/{objectiveId}/initiatives/suggest-measurement")
    public ResponseEntity<ApiResponse<SuggestedMeasurementDto>> suggestMeasurement(
            @PathVariable Long objectiveId,
            @Valid @RequestBody SuggestMeasurementRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        SuggestedMeasurementDto suggestion =
                initiativeService.suggestMeasurement(objectiveId, req, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(suggestion));
    }

    @PostMapping("/api/objectives/{objectiveId}/initiatives")
    public ResponseEntity<ApiResponse<InitiativeResponse>> createInitiative(
            @PathVariable Long objectiveId,
            @Valid @RequestBody CreateInitiativeRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        Initiative initiative = initiativeService.createInitiative(objectiveId, req, principal.getId());
        return ResponseEntity.status(201).body(
                ApiResponse.success("Initiative created", initiativeService.toResponse(initiative)));
    }

    @PutMapping("/api/initiatives/{id}")
    public ResponseEntity<ApiResponse<InitiativeResponse>> updateInitiative(
            @PathVariable Long id,
            @Valid @RequestBody CreateInitiativeRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        Initiative initiative = initiativeService.updateInitiative(id, req, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(initiativeService.toResponse(initiative)));
    }

    @DeleteMapping("/api/initiatives/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInitiative(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        initiativeService.deleteInitiative(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Initiative deleted", null));
    }
}
