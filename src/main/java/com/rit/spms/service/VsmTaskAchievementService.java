package com.rit.spms.service;

import com.rit.spms.domain.Achievement;
import com.rit.spms.domain.ImprovementTask;
import com.rit.spms.domain.Measurement;
import com.rit.spms.domain.PortfolioEntry;
import com.rit.spms.domain.RoleAssignment;
import com.rit.spms.domain.Strategy;
import com.rit.spms.domain.enums.RoleType;
import com.rit.spms.domain.enums.StrategyState;
import com.rit.spms.dto.request.CreateAchievementRequest;
import com.rit.spms.dto.request.LogTaskAchievementRequest;
import com.rit.spms.dto.response.AchievableMeasurementResponse;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.repository.ImprovementTaskRepository;
import com.rit.spms.repository.MeasurementRepository;
import com.rit.spms.repository.RoleAssignmentRepository;
import com.rit.spms.repository.StrategyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The "log an achievement to complete an IMPROVEMENT task" step (Phase 4 of the VSM module).
 * Deliberately NOT routed through {@code CustomizableAchievementModule} -- that interface's
 * contract is "an Admin pre-wires one criterion per title, the achievement's category/criteria are
 * then locked forever" (see {@code TeachingEvaluationsAchievementModule}), which is backwards from
 * here: the task-completer freely picks category/criteria/goal, same as a normal achievement.
 * {@link Achievement#getCreatedByModuleCode()} stays null for every achievement this produces, so it
 * remains a fully ordinary, fully editable achievement -- traceability back to the task lives on
 * {@link ImprovementTask#getAchievement()} instead. Reuses {@link PortfolioEntryService
 * #logAchievementWithEvaluation} verbatim (the one existing call that creates both the Achievement
 * and its PortfolioEntry atomically) rather than reimplementing that logic.
 */
@Service
@RequiredArgsConstructor
public class VsmTaskAchievementService {

    private final ImprovementTaskRepository improvementTaskRepository;
    private final ImprovementTaskService improvementTaskService;
    private final PortfolioEntryService portfolioEntryService;
    private final RoleAssignmentRepository roleAssignmentRepository;
    private final StrategyRepository strategyRepository;
    private final MeasurementRepository measurementRepository;

    /**
     * Every Measurement under a DEPLOYED strategy the user owns/edits -- the picker backing this
     * flow, since no "which Initiative can I log against" endpoint existed anywhere before this
     * (every prior achievement-logging entry point already had one measurement in context from the
     * Strategy Tree). An Achievement not traceable to a real Initiative "isn't an Achievement," so
     * this list is deliberately the *only* way to resolve a measurementId for the request below.
     */
    @Transactional(readOnly = true)
    public List<AchievableMeasurementResponse> getAchievableMeasurements(Long userId) {
        Set<Long> strategyIds = new LinkedHashSet<>();
        for (RoleAssignment ra : roleAssignmentRepository.findByUserId(userId)) {
            if ((ra.getRole() == RoleType.OWNER || ra.getRole() == RoleType.EDITOR) && ra.getStrategy() != null) {
                strategyIds.add(ra.getStrategy().getId());
            }
        }

        return strategyIds.stream()
                .map(strategyRepository::findById)
                .flatMap(java.util.Optional::stream)
                .filter(s -> s.getState() == StrategyState.DEPLOYED)
                .flatMap(strategy -> measurementRepository.findByStrategyId(strategy.getId()).stream()
                        .map(m -> toResponse(m, strategy)))
                .toList();
    }

    private AchievableMeasurementResponse toResponse(Measurement m, Strategy strategy) {
        var initiative = m.getInitiative();
        return AchievableMeasurementResponse.builder()
                .measurementId(m.getId())
                .measurementDescription(m.getDescription())
                .initiativeId(initiative.getId())
                .initiativeTitle(initiative.getTitle())
                .strategyId(strategy.getId())
                .strategyTitle(strategy.getTitle())
                .planningCycleId(strategy.getPlanningCycle().getId())
                .build();
    }

    /**
     * Logs the achievement and attaches it to the task -- {@link ImprovementTaskService#complete}
     * will only allow an IMPROVEMENT task to reach DONE once this has been called. Only the person
     * who pulled the task, or an admin, may log it (same rule as starting/completing it).
     */
    @Transactional
    public ImprovementTask logAchievementForTask(Long taskId, LogTaskAchievementRequest req, Long userId) {
        ImprovementTask task = improvementTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("ImprovementTask", taskId));
        improvementTaskService.assertIsPullerOrAdmin(task, userId);

        CreateAchievementRequest achievementReq = new CreateAchievementRequest();
        achievementReq.setTitle(req.getAchievementTitle());
        achievementReq.setAchievementTypeId(req.getAchievementTypeId());
        achievementReq.setCustomTypeName(req.getCustomTypeName());
        achievementReq.setDetails(req.getDetails());
        achievementReq.setPrivateNotes(req.getPrivateNotes());
        achievementReq.setAssessmentPeriodId(req.getAssessmentPeriodId());

        PortfolioEntry entry = portfolioEntryService.logAchievementWithEvaluation(
                req.getMeasurementId(), achievementReq, req.getCategoryId(), req.getCriteriaId(),
                req.getCategoryRating(), req.getGoalId(), req.getEvidenceUrl(), userId);

        Achievement achievement = entry.getAchievement();
        task.setAchievement(achievement);
        if (task.getLinkedInitiative() == null && achievement.getMeasurement() != null) {
            task.setLinkedInitiative(achievement.getMeasurement().getInitiative());
        }
        return improvementTaskRepository.save(task);
    }
}
