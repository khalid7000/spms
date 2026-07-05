package com.rit.spms.service;

import com.rit.spms.domain.*;
import com.rit.spms.domain.enums.StrategyType;
import com.rit.spms.dto.request.CreateAchievementRequest;
import com.rit.spms.dto.response.AchievementResponse;
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
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final MeasurementRepository measurementRepository;
    private final AchievementTypeRepository achievementTypeRepository;
    private final AssessmentPeriodRepository assessmentPeriodRepository;
    private final AppUserRepository appUserRepository;
    private final InitiativeRepository initiativeRepository;
    private final AcademicYearRepository academicYearRepository;
    private final PermissionService permissionService;
    private final AuditService auditService;

    /** Returns the closed/frozen state of the academic year for a given achievement.
     *  Checks the initiative's academic year first; if null (common for seeded data),
     *  falls back to matching the assessment period by name. */
    private boolean isYearFrozen(Achievement a) {
        AcademicYear ay = a.getMeasurement().getInitiative().getAcademicYear();
        if (ay == null && a.getAssessmentPeriod() != null) {
            ay = academicYearRepository.findByName(a.getAssessmentPeriod().getName()).orElse(null);
        }
        return ay != null && Boolean.TRUE.equals(ay.getClosed());
    }

    public Achievement recordAchievement(Long measurementId, CreateAchievementRequest req, Long currentUserId) {
        Measurement measurement = measurementRepository.findById(measurementId)
                .orElseThrow(() -> new ResourceNotFoundException("Measurement", measurementId));

        Strategy strategy = measurement.getInitiative().getObjective().getGoal().getStrategy();

        if (strategy.getStrategyType() == StrategyType.UNIVERSITY) {
            throw new BusinessRuleException(
                    "Achievements cannot be recorded directly on the university strategy. " +
                    "They are aggregated automatically from department strategies.");
        }

        // Block recording against a closed academic year
        AcademicYear academicYear = measurement.getInitiative().getAcademicYear();
        if (academicYear != null && academicYear.getClosed()) {
            throw new BusinessRuleException(
                    "Academic year '" + academicYear.getName() + "' is closed. No new achievements can be recorded.");
        }

        permissionService.assertCanAddAchievement(currentUserId, strategy.getId());

        AppUser author = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));

        AchievementType type = achievementTypeRepository.findById(req.getAchievementTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("AchievementType", req.getAchievementTypeId()));

        if (req.getAssessmentPeriodId() == null) {
            throw new BusinessRuleException("An assessment period must be selected when recording an achievement");
        }
        AssessmentPeriod period = assessmentPeriodRepository.findById(req.getAssessmentPeriodId())
                .orElseThrow(() -> new ResourceNotFoundException("AssessmentPeriod", req.getAssessmentPeriodId()));

        Achievement achievement = Achievement.builder()
                .measurement(measurement)
                .title(req.getTitle())
                .achievementType(type)
                .details(req.getDetails())
                .privateNotes(req.getPrivateNotes())
                .author(author)
                .assessmentPeriod(period)
                .build();
        achievement = achievementRepository.save(achievement);

        auditService.log(author, "RECORD_ACHIEVEMENT", "Achievement", achievement.getId(), strategy,
                "Recorded achievement: " + achievement.getTitle());
        return achievement;
    }

    public Achievement updateAchievement(Long achievementId, CreateAchievementRequest req, Long currentUserId) {
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement", achievementId));

        Strategy strategy = achievement.getMeasurement().getInitiative().getObjective().getGoal().getStrategy();
        Long strategyId = strategy.getId();

        if (isYearFrozen(achievement)) {
            throw new BusinessRuleException("This academic year is frozen. Achievements cannot be edited.");
        }

        boolean isAuthor = achievement.getAuthor().getId().equals(currentUserId);
        boolean isOwner = permissionService.isOwner(currentUserId, strategyId);

        if (!isAuthor && !isOwner) {
            throw new UnauthorizedException("Only the achievement author or strategy Owner can edit this achievement");
        }

        AppUser user = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));

        String oldTitle = achievement.getTitle();
        achievement.setTitle(req.getTitle());
        if (req.getDetails() != null) achievement.setDetails(req.getDetails());
        if (req.getPrivateNotes() != null) achievement.setPrivateNotes(req.getPrivateNotes());

        if (req.getAchievementTypeId() != null) {
            AchievementType type = achievementTypeRepository.findById(req.getAchievementTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("AchievementType", req.getAchievementTypeId()));
            achievement.setAchievementType(type);
        }

        if (req.getAssessmentPeriodId() != null) {
            AssessmentPeriod period = assessmentPeriodRepository.findById(req.getAssessmentPeriodId())
                    .orElseThrow(() -> new ResourceNotFoundException("AssessmentPeriod", req.getAssessmentPeriodId()));
            achievement.setAssessmentPeriod(period);
        }

        achievement = achievementRepository.save(achievement);
        auditService.log(user, "UPDATE_ACHIEVEMENT", "Achievement", achievementId, strategy,
                oldTitle, achievement.getTitle(), "Updated achievement");
        return achievement;
    }

    public void deleteAchievement(Long achievementId, Long currentUserId) {
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement", achievementId));

        Strategy strategy = achievement.getMeasurement().getInitiative().getObjective().getGoal().getStrategy();
        Long strategyId = strategy.getId();

        if (isYearFrozen(achievement)) {
            throw new BusinessRuleException("This academic year is frozen. Achievements cannot be deleted.");
        }

        boolean isAuthor = achievement.getAuthor().getId().equals(currentUserId);
        boolean isOwner = permissionService.isOwner(currentUserId, strategyId);

        if (!isAuthor && !isOwner) {
            throw new UnauthorizedException("Only the achievement author or strategy Owner can delete this achievement");
        }

        AppUser user = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));

        auditService.log(user, "DELETE_ACHIEVEMENT", "Achievement", achievementId, strategy,
                "Deleted achievement: " + achievement.getTitle());
        achievementRepository.delete(achievement);
    }

    @Transactional(readOnly = true)
    public List<AchievementResponse> getAchievements(Long measurementId, Long currentUserId) {
        Measurement measurement = measurementRepository.findById(measurementId)
                .orElseThrow(() -> new ResourceNotFoundException("Measurement", measurementId));
        permissionService.assertCanRead(currentUserId,
                measurement.getInitiative().getObjective().getGoal().getStrategy().getId());

        return achievementRepository.findByMeasurementIdOrderByRecordedAtDesc(measurementId)
                .stream().map(a -> toResponse(a, currentUserId)).toList();
    }

    @Transactional(readOnly = true)
    public List<AchievementResponse> getAggregatedAchievements(Long universityInitiativeId, Long currentUserId) {
        Initiative initiative = initiativeRepository.findById(universityInitiativeId)
                .orElseThrow(() -> new ResourceNotFoundException("Initiative", universityInitiativeId));
        permissionService.assertCanRead(currentUserId,
                initiative.getObjective().getGoal().getStrategy().getId());

        return achievementRepository.findAggregatedByUniversityInitiativeId(universityInitiativeId)
                .stream().map(a -> toResponse(a, currentUserId)).toList();
    }

    public AchievementResponse toResponse(Achievement a, Long viewingUserId) {
        boolean isAuthor = viewingUserId != null && viewingUserId.equals(a.getAuthor().getId());

        boolean yearFrozen = isYearFrozen(a);

        Long strategyId = a.getMeasurement().getInitiative().getObjective().getGoal().getStrategy().getId();
        boolean isOwner = viewingUserId != null && permissionService.isOwner(viewingUserId, strategyId);

        boolean canModify = !yearFrozen && (isOwner || isAuthor);

        return AchievementResponse.builder()
                .id(a.getId())
                .measurementId(a.getMeasurement().getId())
                .title(a.getTitle())
                .achievementTypeId(a.getAchievementType().getId())
                .achievementTypeName(a.getAchievementType().getName())
                .details(a.getDetails())
                .privateNotes(isAuthor ? a.getPrivateNotes() : null)
                .authorId(a.getAuthor().getId())
                .authorName(a.getAuthor().getFname() + " " + a.getAuthor().getLname())
                .assessmentPeriodId(a.getAssessmentPeriod() != null ? a.getAssessmentPeriod().getId() : null)
                .assessmentPeriodName(a.getAssessmentPeriod() != null ? a.getAssessmentPeriod().getName() : null)
                .recordedAt(a.getRecordedAt())
                .updatedAt(a.getUpdatedAt())
                .canEdit(canModify)
                .canDelete(canModify)
                .build();
    }
}
