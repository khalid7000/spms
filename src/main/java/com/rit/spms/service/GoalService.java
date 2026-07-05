package com.rit.spms.service;

import com.rit.spms.domain.*;
import com.rit.spms.dto.request.CreateGoalRequest;
import com.rit.spms.dto.response.GoalResponse;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GoalService {

    private final GoalRepository goalRepository;
    private final StrategyRepository strategyRepository;
    private final AppUserRepository appUserRepository;
    private final ThemeRepository themeRepository;
    private final VisionAreaRepository visionAreaRepository;
    private final PermissionService permissionService;
    private final AuditService auditService;

    public Goal createGoal(Long strategyId, CreateGoalRequest req, Long currentUserId) {
        permissionService.assertCanEditContent(currentUserId, strategyId);

        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy", strategyId));
        AppUser creator = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));

        Theme theme = null;
        if (req.getThemeId() != null) {
            theme = themeRepository.findById(req.getThemeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Theme", req.getThemeId()));
        }

        VisionArea area = null;
        if (req.getVisionAreaId() != null) {
            area = visionAreaRepository.findById(req.getVisionAreaId())
                    .orElseThrow(() -> new ResourceNotFoundException("VisionArea", req.getVisionAreaId()));
        }

        Goal goal = Goal.builder()
                .strategy(strategy)
                .theme(theme)
                .area(area)
                .title(req.getTitle())
                .description(req.getDescription())
                .sortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0)
                .createdBy(creator)
                .build();
        goal = goalRepository.save(goal);

        auditService.log(creator, "CREATE_GOAL", "Goal", goal.getId(), strategy,
                "Created goal: " + goal.getTitle());
        return goal;
    }

    public Goal updateGoal(Long goalId, CreateGoalRequest req, Long currentUserId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", goalId));
        permissionService.assertCanEditContent(currentUserId, goal.getStrategy().getId());

        AppUser user = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));

        String oldTitle = goal.getTitle();
        goal.setTitle(req.getTitle());
        if (req.getDescription() != null) goal.setDescription(req.getDescription());
        if (req.getSortOrder() != null) goal.setSortOrder(req.getSortOrder());
        if (req.getThemeId() != null) {
            Theme theme = themeRepository.findById(req.getThemeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Theme", req.getThemeId()));
            goal.setTheme(theme);
        }
        if (req.getVisionAreaId() != null) {
            VisionArea area = visionAreaRepository.findById(req.getVisionAreaId())
                    .orElseThrow(() -> new ResourceNotFoundException("VisionArea", req.getVisionAreaId()));
            goal.setArea(area);
        } else {
            goal.setArea(null);
        }
        goal = goalRepository.save(goal);

        auditService.log(user, "UPDATE_GOAL", "Goal", goalId, goal.getStrategy(),
                oldTitle, goal.getTitle(), "Updated goal");
        return goal;
    }

    public void deleteGoal(Long goalId, Long currentUserId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", goalId));
        permissionService.assertCanEditContent(currentUserId, goal.getStrategy().getId());

        AppUser user = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));
        auditService.log(user, "DELETE_GOAL", "Goal", goalId, goal.getStrategy(),
                "Deleted goal: " + goal.getTitle());

        goalRepository.delete(goal);
    }

    @Transactional(readOnly = true)
    public List<GoalResponse> getGoals(Long strategyId, Long currentUserId) {
        permissionService.assertCanRead(currentUserId, strategyId);
        return goalRepository.findByStrategyIdOrderBySortOrder(strategyId)
                .stream().map(this::toResponse).toList();
    }

    public GoalResponse toResponse(Goal goal) {
        return GoalResponse.builder()
                .id(goal.getId())
                .strategyId(goal.getStrategy().getId())
                .themeId(goal.getTheme() != null ? goal.getTheme().getId() : null)
                .themeName(goal.getTheme() != null ? goal.getTheme().getName() : null)
                .areaId(goal.getArea() != null ? goal.getArea().getId() : null)
                .areaName(goal.getArea() != null ? goal.getArea().getName() : null)
                .title(goal.getTitle())
                .description(goal.getDescription())
                .sortOrder(goal.getSortOrder())
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();
    }
}
