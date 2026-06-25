package com.rit.spms.service;

import com.rit.spms.domain.*;
import com.rit.spms.domain.enums.StrategyType;
import com.rit.spms.dto.response.CoverageReportResponse;
import com.rit.spms.dto.response.InitiativeResponse;
import com.rit.spms.dto.response.ObjectiveResponse;
import com.rit.spms.exception.BusinessRuleException;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MappingService {

    private final StrategyRepository strategyRepository;
    private final GoalRepository goalRepository;
    private final ObjectiveRepository objectiveRepository;
    private final ObjectiveMappingRepository objectiveMappingRepository;
    private final InitiativeRepository initiativeRepository;
    private final InitiativeMappingRepository initiativeMappingRepository;
    private final PlanningCycleRepository planningCycleRepository;
    private final PermissionService permissionService;

    public CoverageReportResponse getCoverageReport(Long universityStrategyId, Long currentUserId) {
        permissionService.assertCanRead(currentUserId, universityStrategyId);

        Strategy strategy = strategyRepository.findById(universityStrategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy", universityStrategyId));

        if (strategy.getStrategyType() != StrategyType.UNIVERSITY) {
            throw new BusinessRuleException("Coverage report is only available for university strategies");
        }

        List<ObjectiveMapping> allObjMappings = objectiveMappingRepository
                .findByDeptStrategyId(strategy.getId());
        List<InitiativeMapping> allInitMappings = initiativeMappingRepository
                .findByDeptStrategyId(strategy.getId());

        // Actually we need university-side objectives for this strategy
        List<Goal> univGoals = goalRepository.findByStrategyIdOrderBySortOrder(universityStrategyId);

        List<CoverageReportResponse.ObjectiveCoverage> objectiveCoverages = new ArrayList<>();

        for (Goal goal : univGoals) {
            List<Objective> univObjectives = objectiveRepository.findByGoalIdOrderBySortOrder(goal.getId());

            for (Objective univObj : univObjectives) {
                List<ObjectiveMapping> mappingsToThisObj = objectiveMappingRepository
                        .findByUniversityObjectiveId(univObj.getId());

                List<Long> mappedDeptObjIds = mappingsToThisObj.stream()
                        .map(om -> om.getDeptObjective().getId()).toList();

                List<Initiative> univInits = initiativeRepository
                        .findByObjectiveIdOrderBySortOrder(univObj.getId());

                List<CoverageReportResponse.InitiativeCoverage> initiativeCoverages = new ArrayList<>();

                for (Initiative univInit : univInits) {
                    List<InitiativeMapping> mappingsToThisInit = initiativeMappingRepository
                            .findByUniversityInitiativeId(univInit.getId());

                    List<Long> mappedDeptInitIds = mappingsToThisInit.stream()
                            .map(im -> im.getDeptInitiative().getId()).toList();

                    initiativeCoverages.add(CoverageReportResponse.InitiativeCoverage.builder()
                            .universityInitiativeId(univInit.getId())
                            .universityInitiativeTitle(univInit.getTitle())
                            .hasCoverage(!mappedDeptInitIds.isEmpty())
                            .mappedDeptInitiativeIds(mappedDeptInitIds)
                            .build());
                }

                objectiveCoverages.add(CoverageReportResponse.ObjectiveCoverage.builder()
                        .universityObjectiveId(univObj.getId())
                        .universityObjectiveTitle(univObj.getTitle())
                        .hasCoverage(!mappedDeptObjIds.isEmpty())
                        .mappedDeptObjectiveIds(mappedDeptObjIds)
                        .initiatives(initiativeCoverages)
                        .build());
            }
        }

        return CoverageReportResponse.builder()
                .strategyId(strategy.getId())
                .strategyTitle(strategy.getTitle())
                .objectives(objectiveCoverages)
                .build();
    }

    public List<ObjectiveResponse> getAvailableUniversityObjectives(Long planningCycleId, Long currentUserId) {
        Strategy univStrategy = strategyRepository.findByPlanningCycleIdAndDepartmentIsNull(planningCycleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "University strategy for planning cycle " + planningCycleId));

        permissionService.assertCanRead(currentUserId, univStrategy.getId());

        List<Goal> goals = goalRepository.findByStrategyIdOrderBySortOrder(univStrategy.getId());
        List<ObjectiveResponse> result = new ArrayList<>();

        for (Goal goal : goals) {
            List<Objective> objectives = objectiveRepository.findByGoalIdOrderBySortOrder(goal.getId());
            for (Objective obj : objectives) {
                result.add(ObjectiveResponse.builder()
                        .id(obj.getId())
                        .goalId(goal.getId())
                        .title(obj.getTitle())
                        .description(obj.getDescription())
                        .sortOrder(obj.getSortOrder())
                        .frozen(obj.getFrozen())
                        .universityObjectiveIds(List.of())
                        .build());
            }
        }
        return result;
    }

    public List<InitiativeResponse> getAvailableUniversityInitiatives(Long deptObjectiveId, Long currentUserId) {
        Objective deptObj = objectiveRepository.findById(deptObjectiveId)
                .orElseThrow(() -> new ResourceNotFoundException("Objective", deptObjectiveId));

        permissionService.assertCanRead(currentUserId, deptObj.getGoal().getStrategy().getId());

        List<Long> univObjIds = objectiveMappingRepository.findByDeptObjectiveId(deptObjectiveId)
                .stream().map(om -> om.getUniversityObjective().getId()).toList();

        if (univObjIds.isEmpty()) {
            return List.of();
        }

        List<InitiativeResponse> result = new ArrayList<>();
        for (Long univObjId : univObjIds) {
            List<Initiative> initiatives = initiativeRepository.findByObjectiveIdOrderBySortOrder(univObjId);
            for (Initiative init : initiatives) {
                result.add(InitiativeResponse.builder()
                        .id(init.getId())
                        .objectiveId(univObjId)
                        .title(init.getTitle())
                        .description(init.getDescription())
                        .sortOrder(init.getSortOrder())
                        .measurements(List.of())
                        .build());
            }
        }
        return result;
    }
}
