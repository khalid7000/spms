package com.rit.spms.service;

import com.rit.spms.domain.*;
import com.rit.spms.dto.request.CreateVisionAreaRequest;
import com.rit.spms.dto.response.VisionAreaResponse;
import com.rit.spms.exception.BusinessRuleException;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VisionAreaService {

    private final VisionAreaRepository visionAreaRepository;
    private final StrategyRepository strategyRepository;
    private final AppUserRepository appUserRepository;
    private final GoalRepository goalRepository;
    private final PermissionService permissionService;
    private final AuditService auditService;

    public VisionAreaResponse createArea(Long strategyId, CreateVisionAreaRequest req, Long currentUserId) {
        permissionService.assertOwner(currentUserId, strategyId);

        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy", strategyId));
        AppUser creator = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));

        VisionArea area = VisionArea.builder()
                .strategy(strategy)
                .name(req.getName())
                .sortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0)
                .createdBy(creator)
                .build();
        area = visionAreaRepository.save(area);

        auditService.log(creator, "CREATE_VISION_AREA", "VisionArea", area.getId(), strategy,
                "Created vision concentration area: " + area.getName());
        return toResponse(area);
    }

    public VisionAreaResponse updateArea(Long areaId, CreateVisionAreaRequest req, Long currentUserId) {
        VisionArea area = visionAreaRepository.findById(areaId)
                .orElseThrow(() -> new ResourceNotFoundException("VisionArea", areaId));
        permissionService.assertOwner(currentUserId, area.getStrategy().getId());

        AppUser user = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));

        String oldName = area.getName();
        area.setName(req.getName());
        if (req.getSortOrder() != null) area.setSortOrder(req.getSortOrder());
        area = visionAreaRepository.save(area);

        auditService.log(user, "UPDATE_VISION_AREA", "VisionArea", areaId, area.getStrategy(),
                oldName, area.getName(), "Updated vision concentration area");
        return toResponse(area);
    }

    public void deleteArea(Long areaId, Long currentUserId) {
        VisionArea area = visionAreaRepository.findById(areaId)
                .orElseThrow(() -> new ResourceNotFoundException("VisionArea", areaId));
        permissionService.assertOwner(currentUserId, area.getStrategy().getId());

        // An area with goals still assigned can't be deleted out from under them — the owner must
        // move or delete every goal first (previously this silently ungrouped them instead of blocking).
        if (goalRepository.existsByAreaId(areaId)) {
            throw new BusinessRuleException(
                    "Cannot delete an area that still has goals assigned. Move or delete all goals from this area first.");
        }

        AppUser user = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));
        auditService.log(user, "DELETE_VISION_AREA", "VisionArea", areaId, area.getStrategy(),
                "Deleted vision concentration area: " + area.getName());

        visionAreaRepository.delete(area);
    }

    @Transactional(readOnly = true)
    public List<VisionAreaResponse> getAreas(Long strategyId, Long currentUserId) {
        permissionService.assertCanRead(currentUserId, strategyId);
        return visionAreaRepository.findByStrategyIdOrderBySortOrder(strategyId)
                .stream().map(this::toResponse).toList();
    }

    public VisionAreaResponse assignGoalToArea(Long goalId, Long areaId, Long currentUserId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", goalId));
        permissionService.assertOwner(currentUserId, goal.getStrategy().getId());

        VisionArea area = null;
        if (areaId != null) {
            area = visionAreaRepository.findById(areaId)
                    .orElseThrow(() -> new ResourceNotFoundException("VisionArea", areaId));
            if (!area.getStrategy().getId().equals(goal.getStrategy().getId())) {
                throw new BusinessRuleException("Vision area does not belong to the same strategy as this goal");
            }
        }

        AppUser user = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));

        goal.setArea(area);
        goalRepository.save(goal);

        String areaName = area != null ? area.getName() : "(none)";
        auditService.log(user, "ASSIGN_GOAL_AREA", "Goal", goalId, goal.getStrategy(),
                "Moved goal \"" + goal.getTitle() + "\" to concentration area: " + areaName);

        return area != null ? toResponse(area) : null;
    }

    public VisionAreaResponse toResponse(VisionArea area) {
        return VisionAreaResponse.builder()
                .id(area.getId())
                .strategyId(area.getStrategy().getId())
                .name(area.getName())
                .sortOrder(area.getSortOrder())
                .createdAt(area.getCreatedAt())
                .updatedAt(area.getUpdatedAt())
                .build();
    }
}
