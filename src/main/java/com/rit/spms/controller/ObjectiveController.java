package com.rit.spms.controller;

import com.rit.spms.domain.Objective;
import com.rit.spms.dto.request.CreateObjectiveRequest;
import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.dto.response.ObjectiveResponse;
import com.rit.spms.security.UserPrincipal;
import com.rit.spms.service.ObjectiveService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ObjectiveController {

    private final ObjectiveService objectiveService;

    @GetMapping("/api/goals/{goalId}/objectives")
    public ResponseEntity<ApiResponse<List<ObjectiveResponse>>> getObjectives(
            @PathVariable Long goalId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(objectiveService.getObjectives(goalId, principal.getId())));
    }

    @PostMapping("/api/goals/{goalId}/objectives")
    public ResponseEntity<ApiResponse<ObjectiveResponse>> createObjective(
            @PathVariable Long goalId,
            @Valid @RequestBody CreateObjectiveRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        Objective obj = objectiveService.createObjective(goalId, req, principal.getId());
        return ResponseEntity.status(201).body(
                ApiResponse.success("Objective created", objectiveService.toResponse(obj)));
    }

    @PutMapping("/api/objectives/{id}")
    public ResponseEntity<ApiResponse<ObjectiveResponse>> updateObjective(
            @PathVariable Long id,
            @Valid @RequestBody CreateObjectiveRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        Objective obj = objectiveService.updateObjective(id, req, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(objectiveService.toResponse(obj)));
    }

    @DeleteMapping("/api/objectives/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteObjective(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        objectiveService.deleteObjective(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Objective deleted", null));
    }

    @PatchMapping("/api/objectives/{id}/freeze")
    public ResponseEntity<ApiResponse<ObjectiveResponse>> setFrozen(
            @PathVariable Long id,
            @Valid @RequestBody FreezeRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        Objective obj = objectiveService.setFrozen(id, Boolean.TRUE.equals(req.getFrozen()), principal.getId());
        return ResponseEntity.ok(ApiResponse.success(
                Boolean.TRUE.equals(req.getFrozen()) ? "Objective frozen" : "Objective unfrozen",
                objectiveService.toResponse(obj)));
    }

    @Data
    public static class FreezeRequest {
        @NotNull
        private Boolean frozen;
    }
}
