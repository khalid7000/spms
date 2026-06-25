package com.rit.spms.controller;

import com.rit.spms.domain.Goal;
import com.rit.spms.dto.request.CreateGoalRequest;
import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.dto.response.GoalResponse;
import com.rit.spms.security.UserPrincipal;
import com.rit.spms.service.GoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @GetMapping("/api/strategies/{strategyId}/goals")
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getGoals(
            @PathVariable Long strategyId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(goalService.getGoals(strategyId, principal.getId())));
    }

    @PostMapping("/api/strategies/{strategyId}/goals")
    public ResponseEntity<ApiResponse<GoalResponse>> createGoal(
            @PathVariable Long strategyId,
            @Valid @RequestBody CreateGoalRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        Goal goal = goalService.createGoal(strategyId, req, principal.getId());
        return ResponseEntity.status(201).body(ApiResponse.success("Goal created", goalService.toResponse(goal)));
    }

    @PutMapping("/api/goals/{id}")
    public ResponseEntity<ApiResponse<GoalResponse>> updateGoal(
            @PathVariable Long id,
            @Valid @RequestBody CreateGoalRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        Goal goal = goalService.updateGoal(id, req, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(goalService.toResponse(goal)));
    }

    @DeleteMapping("/api/goals/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        goalService.deleteGoal(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Goal deleted", null));
    }
}
