package com.rit.spms.service;

import com.rit.spms.domain.*;
import com.rit.spms.domain.enums.StrategyState;
import com.rit.spms.domain.enums.StrategyType;
import com.rit.spms.dto.request.CreateInitiativeRequest;
import com.rit.spms.dto.request.SuggestMeasurementRequest;
import com.rit.spms.dto.response.InitiativeResponse;
import com.rit.spms.dto.response.MeasurementResponse;
import com.rit.spms.exception.BusinessRuleException;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.exception.UnauthorizedException;
import com.rit.spms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InitiativeService {

    private final InitiativeRepository initiativeRepository;
    private final ObjectiveRepository objectiveRepository;
    private final AppUserRepository appUserRepository;
    private final InitiativeMappingRepository initiativeMappingRepository;
    private final ObjectiveMappingRepository objectiveMappingRepository;
    private final MeasurementRepository measurementRepository;
    private final AchievementRepository achievementRepository;
    private final AcademicYearRepository academicYearRepository;
    private final AssessmentPeriodRepository assessmentPeriodRepository;
    private final PermissionService permissionService;
    private final AuditService auditService;
    private final MeasurementSuggestionGenerator measurementSuggestionGenerator;

    public MeasurementSuggestionGenerator.SuggestedMeasurementDto suggestMeasurement(
            Long objectiveId, SuggestMeasurementRequest req, Long currentUserId) {
        Objective objective = objectiveRepository.findById(objectiveId)
                .orElseThrow(() -> new ResourceNotFoundException("Objective", objectiveId));

        Long strategyId = objective.getGoal().getStrategy().getId();
        if (!permissionService.canAddInitiative(currentUserId, strategyId)) {
            throw new UnauthorizedException("Cannot add initiatives in the current strategy state");
        }

        return measurementSuggestionGenerator.suggestMeasurement(
                objective.getTitle(), objective.getDescription(),
                req.getInitiativeTitle(), req.getInitiativeDescription());
    }

    public Initiative createInitiative(Long objectiveId, CreateInitiativeRequest req, Long currentUserId) {
        Objective objective = objectiveRepository.findById(objectiveId)
                .orElseThrow(() -> new ResourceNotFoundException("Objective", objectiveId));

        Strategy strategy = objective.getGoal().getStrategy();
        Long strategyId = strategy.getId();

        if (!permissionService.canAddInitiative(currentUserId, strategyId)) {
            throw new UnauthorizedException("Cannot add initiatives in the current strategy state");
        }

        if (objective.getFrozen() && !permissionService.isOwner(currentUserId, strategyId)) {
            throw new UnauthorizedException("Only the Owner can add initiatives to a frozen objective");
        }

        // Resolve academic year (null = base initiative)
        AcademicYear academicYear = null;
        if (req.getAcademicYearId() != null) {
            academicYear = academicYearRepository.findById(req.getAcademicYearId())
                    .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", req.getAcademicYearId()));
            if (academicYear.getClosed()) {
                throw new BusinessRuleException("Cannot add initiatives to a closed academic year");
            }
        }

        boolean isDeptStrategy = strategy.getStrategyType() != StrategyType.UNIVERSITY;

        if (isDeptStrategy && academicYear == null) {
            long mappingCount = objectiveMappingRepository.countByDeptObjectiveId(objectiveId);
            if (mappingCount == 0) {
                throw new BusinessRuleException(
                        "Department objective must be mapped to at least one university objective before adding initiatives");
            }
            if (req.getUniversityInitiativeId() == null) {
                throw new BusinessRuleException("Department initiative must map to exactly one university initiative");
            }
            validateUniversityInitiativeForObjective(objectiveId, req.getUniversityInitiativeId());
        }

        AssessmentPeriod assessmentPeriod = null;
        if (req.getAssessmentPeriodId() != null) {
            assessmentPeriod = assessmentPeriodRepository.findById(req.getAssessmentPeriodId())
                    .orElseThrow(() -> new ResourceNotFoundException("AssessmentPeriod", req.getAssessmentPeriodId()));
        }

        AppUser creator = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));

        Initiative initiative = Initiative.builder()
                .objective(objective)
                .title(req.getTitle())
                .description(req.getDescription())
                .sortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0)
                .createdBy(creator)
                .academicYear(academicYear)
                .assessmentPeriod(assessmentPeriod)
                .build();
        initiative = initiativeRepository.save(initiative);

        if (isDeptStrategy && academicYear == null && req.getUniversityInitiativeId() != null) {
            Initiative univInit = initiativeRepository.findById(req.getUniversityInitiativeId())
                    .orElseThrow(() -> new ResourceNotFoundException("University Initiative", req.getUniversityInitiativeId()));
            InitiativeMapping mapping = InitiativeMapping.builder()
                    .deptInitiative(initiative)
                    .universityInitiative(univInit)
                    .build();
            initiativeMappingRepository.save(mapping);
        }

        auditService.log(creator, "CREATE_INITIATIVE", "Initiative", initiative.getId(), strategy,
                "Created initiative: " + initiative.getTitle());

        // Optional measurement created in this same transaction -- both rows or neither, rather
        // than the caller making two separate requests that could leave the initiative with no
        // measurement if the second one failed (the exact gap that blocked achievements on 19
        // 2022-2027 initiatives; see V83__fix_missing_2022_2027_measurements.sql). Built inline
        // rather than via MeasurementService.createMeasurement, which would re-check permissions
        // against a different rule (assertCanEditContent) than the one already enforced above.
        if (req.getMeasurement() != null) {
            Measurement measurement = Measurement.builder()
                    .initiative(initiative)
                    .description(req.getMeasurement().getDescription())
                    .unit(req.getMeasurement().getUnit())
                    .targetValue(req.getMeasurement().getTargetValue())
                    .actualValue(req.getMeasurement().getActualValue())
                    .sortOrder(req.getMeasurement().getSortOrder() != null ? req.getMeasurement().getSortOrder() : 0)
                    .build();
            measurement = measurementRepository.save(measurement);
            auditService.log(creator, "CREATE_MEASUREMENT", "Measurement", measurement.getId(), strategy,
                    "Created measurement: " + measurement.getDescription());
        }

        return initiative;
    }

    public Initiative updateInitiative(Long initiativeId, CreateInitiativeRequest req, Long currentUserId) {
        Initiative initiative = initiativeRepository.findById(initiativeId)
                .orElseThrow(() -> new ResourceNotFoundException("Initiative", initiativeId));

        Strategy strategy = initiative.getObjective().getGoal().getStrategy();
        Long strategyId = strategy.getId();
        StrategyState state = strategy.getState();

        // Check frozen-by-achievement: once achievements exist the initiative is locked
        if (achievementRepository.existsByMeasurementInitiativeId(initiativeId)) {
            throw new BusinessRuleException("This initiative has recorded achievements and cannot be edited");
        }

        // Check academic year not closed
        if (initiative.getAcademicYear() != null && initiative.getAcademicYear().getClosed()) {
            throw new BusinessRuleException("Cannot edit initiatives in a closed academic year");
        }

        if (state == StrategyState.DEPLOYED || state == StrategyState.FROZEN) {
            if (!permissionService.isOwner(currentUserId, strategyId)) {
                throw new UnauthorizedException("Plan content cannot be edited in " + state + " state");
            }
        } else {
            if (!permissionService.canEdit(currentUserId, strategyId)) {
                throw new UnauthorizedException("You do not have edit access to this strategy");
            }
        }

        if (initiative.getObjective().getFrozen() && !permissionService.isOwner(currentUserId, strategyId)) {
            throw new UnauthorizedException("Only the Owner can modify initiatives on a frozen objective");
        }

        // Same mapping requirement as createInitiative -- only for base (non-year-specific)
        // department initiatives. Update rather than delete-then-recreate the InitiativeMapping
        // row (it's a 1:1 on deptInitiative) so there's no equivalent of the delete/insert
        // flush-ordering hazard that hit ObjectiveMapping.
        boolean isDeptStrategy = strategy.getStrategyType() != StrategyType.UNIVERSITY;
        boolean isBaseInitiative = initiative.getAcademicYear() == null;
        if (isDeptStrategy && isBaseInitiative) {
            if (req.getUniversityInitiativeId() == null) {
                throw new BusinessRuleException("Department initiative must map to exactly one university initiative");
            }
            validateUniversityInitiativeForObjective(initiative.getObjective().getId(), req.getUniversityInitiativeId());
        }

        AppUser user = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));

        String oldTitle = initiative.getTitle();
        initiative.setTitle(req.getTitle());
        if (req.getDescription() != null) initiative.setDescription(req.getDescription());
        if (req.getSortOrder() != null) initiative.setSortOrder(req.getSortOrder());
        if (req.getAssessmentPeriodId() != null) {
            AssessmentPeriod period = assessmentPeriodRepository.findById(req.getAssessmentPeriodId())
                    .orElseThrow(() -> new ResourceNotFoundException("AssessmentPeriod", req.getAssessmentPeriodId()));
            initiative.setAssessmentPeriod(period);
        }
        initiative = initiativeRepository.save(initiative);

        if (isDeptStrategy && isBaseInitiative) {
            InitiativeMapping mapping = initiativeMappingRepository.findByDeptInitiativeId(initiativeId).orElse(null);
            if (mapping == null || !mapping.getUniversityInitiative().getId().equals(req.getUniversityInitiativeId())) {
                Initiative univInit = initiativeRepository.findById(req.getUniversityInitiativeId())
                        .orElseThrow(() -> new ResourceNotFoundException("University Initiative", req.getUniversityInitiativeId()));
                if (mapping == null) {
                    mapping = InitiativeMapping.builder().deptInitiative(initiative).universityInitiative(univInit).build();
                } else {
                    mapping.setUniversityInitiative(univInit);
                }
                initiativeMappingRepository.save(mapping);
            }
        }

        auditService.log(user, "UPDATE_INITIATIVE", "Initiative", initiativeId, strategy,
                oldTitle, initiative.getTitle(), "Updated initiative");
        return initiative;
    }

    public void deleteInitiative(Long initiativeId, Long currentUserId) {
        Initiative initiative = initiativeRepository.findById(initiativeId)
                .orElseThrow(() -> new ResourceNotFoundException("Initiative", initiativeId));

        Strategy strategy = initiative.getObjective().getGoal().getStrategy();
        Long strategyId = strategy.getId();

        // Cannot delete if achievements exist
        if (achievementRepository.existsByMeasurementInitiativeId(initiativeId)) {
            throw new BusinessRuleException("This initiative has recorded achievements and cannot be deleted");
        }

        // Cannot delete in closed academic year
        if (initiative.getAcademicYear() != null && initiative.getAcademicYear().getClosed()) {
            throw new BusinessRuleException("Cannot delete initiatives in a closed academic year");
        }

        if (strategy.getState() == StrategyState.REVIEW) {
            throw new BusinessRuleException("Initiatives cannot be deleted in REVIEW state");
        }

        permissionService.assertCanEditContent(currentUserId, strategyId);

        if (initiative.getObjective().getFrozen() && !permissionService.isOwner(currentUserId, strategyId)) {
            throw new UnauthorizedException("Only the Owner can delete initiatives on a frozen objective");
        }

        AppUser user = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));

        auditService.log(user, "DELETE_INITIATIVE", "Initiative", initiativeId, strategy,
                "Deleted initiative: " + initiative.getTitle());
        initiativeRepository.delete(initiative);
    }

    @Transactional(readOnly = true)
    public List<InitiativeResponse> getInitiatives(Long objectiveId, Long academicYearId, Long currentUserId) {
        Objective objective = objectiveRepository.findById(objectiveId)
                .orElseThrow(() -> new ResourceNotFoundException("Objective", objectiveId));
        permissionService.assertCanRead(currentUserId, objective.getGoal().getStrategy().getId());

        List<Initiative> initiatives = (academicYearId != null)
                ? initiativeRepository.findByObjectiveIdAndAcademicYearIdOrderBySortOrder(objectiveId, academicYearId)
                : initiativeRepository.findByObjectiveIdAndAcademicYearIsNullOrderBySortOrder(objectiveId);

        return initiatives.stream().map(this::toResponse).toList();
    }

    public InitiativeResponse toResponse(Initiative initiative) {
        Initiative univInit = initiativeMappingRepository.findByDeptInitiativeId(initiative.getId())
                .map(im -> im.getUniversityInitiative())
                .orElse(null);
        Long univInitId = univInit != null ? univInit.getId() : null;
        String univInitTitle = univInit != null ? univInit.getTitle() : null;

        long achievementCount = achievementRepository.countByMeasurementInitiativeId(initiative.getId());
        boolean hasAchievements = achievementCount > 0;

        List<MeasurementResponse> measurements = measurementRepository
                .findByInitiativeIdOrderBySortOrder(initiative.getId())
                .stream().map(m -> MeasurementResponse.builder()
                        .id(m.getId())
                        .initiativeId(m.getInitiative().getId())
                        .description(m.getDescription())
                        .unit(m.getUnit())
                        .targetValue(m.getTargetValue())
                        .actualValue(m.getActualValue())
                        .sortOrder(m.getSortOrder())
                        .academicYearId(m.getAcademicYear() != null ? m.getAcademicYear().getId() : null)
                        .createdAt(m.getCreatedAt())
                        .updatedAt(m.getUpdatedAt())
                        .build()).toList();

        return InitiativeResponse.builder()
                .id(initiative.getId())
                .objectiveId(initiative.getObjective().getId())
                .title(initiative.getTitle())
                .description(initiative.getDescription())
                .sortOrder(initiative.getSortOrder())
                .universityInitiativeId(univInitId)
                .universityInitiativeTitle(univInitTitle)
                .academicYearId(initiative.getAcademicYear() != null ? initiative.getAcademicYear().getId() : null)
                .assessmentPeriodId(initiative.getAssessmentPeriod() != null ? initiative.getAssessmentPeriod().getId() : null)
                .assessmentPeriodName(initiative.getAssessmentPeriod() != null ? initiative.getAssessmentPeriod().getName() : null)
                .hasAchievements(hasAchievements)
                .achievementCount(achievementCount)
                .createdAt(initiative.getCreatedAt())
                .updatedAt(initiative.getUpdatedAt())
                .measurements(measurements)
                .build();
    }

    private void validateUniversityInitiativeForObjective(Long deptObjectiveId, Long univInitiativeId) {
        List<Long> univObjIds = objectiveMappingRepository.findByDeptObjectiveId(deptObjectiveId)
                .stream().map(om -> om.getUniversityObjective().getId()).toList();

        if (univObjIds.isEmpty()) {
            throw new BusinessRuleException("Department objective has no university objective mappings");
        }

        Initiative univInit = initiativeRepository.findById(univInitiativeId)
                .orElseThrow(() -> new ResourceNotFoundException("University Initiative", univInitiativeId));

        Long univInitObjId = univInit.getObjective().getId();
        if (!univObjIds.contains(univInitObjId)) {
            throw new BusinessRuleException(
                    "The selected university initiative does not belong to any of the mapped university objectives");
        }
    }
}
