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
    private final PermissionService permissionService;
    private final AuditService auditService;

    public Achievement recordAchievement(Long measurementId, CreateAchievementRequest req, Long currentUserId) {
        Measurement measurement = measurementRepository.findById(measurementId)
                .orElseThrow(() -> new ResourceNotFoundException("Measurement", measurementId));

        Strategy strategy = measurement.getInitiative().getObjective().getGoal().getStrategy();

        if (strategy.getStrategyType() == StrategyType.UNIVERSITY) {
            throw new BusinessRuleException(
                    "Achievements cannot be recorded directly on the university strategy. " +
                    "They are aggregated automatically from department strategies.");
        }

        permissionService.assertCanAddAchievement(currentUserId, strategy.getId());

        AppUser author = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));

        AchievementType type = achievementTypeRepository.findById(req.getAchievementTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("AchievementType", req.getAchievementTypeId()));

        AssessmentPeriod period = null;
        if (req.getAssessmentPeriodId() != null) {
            period = assessmentPeriodRepository.findById(req.getAssessmentPeriodId())
                    .orElseThrow(() -> new ResourceNotFoundException("AssessmentPeriod", req.getAssessmentPeriodId()));
        }

        Achievement achievement = Achievement.builder()
                .measurement(measurement)
                .title(req.getTitle())
                .achievementType(type)
                .details(req.getDetails())
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

    @Transactional(readOnly = true)
    public List<AchievementResponse> getAchievements(Long measurementId, Long currentUserId) {
        Measurement measurement = measurementRepository.findById(measurementId)
                .orElseThrow(() -> new ResourceNotFoundException("Measurement", measurementId));
        permissionService.assertCanRead(currentUserId,
                measurement.getInitiative().getObjective().getGoal().getStrategy().getId());

        return achievementRepository.findByMeasurementIdOrderByRecordedAtDesc(measurementId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AchievementResponse> getAggregatedAchievements(Long universityInitiativeId, Long currentUserId) {
        Initiative initiative = initiativeRepository.findById(universityInitiativeId)
                .orElseThrow(() -> new ResourceNotFoundException("Initiative", universityInitiativeId));
        permissionService.assertCanRead(currentUserId,
                initiative.getObjective().getGoal().getStrategy().getId());

        return achievementRepository.findAggregatedByUniversityInitiativeId(universityInitiativeId)
                .stream().map(this::toResponse).toList();
    }

    public AchievementResponse toResponse(Achievement a) {
        return AchievementResponse.builder()
                .id(a.getId())
                .measurementId(a.getMeasurement().getId())
                .title(a.getTitle())
                .achievementTypeId(a.getAchievementType().getId())
                .achievementTypeName(a.getAchievementType().getName())
                .details(a.getDetails())
                .authorId(a.getAuthor().getId())
                .authorName(a.getAuthor().getFname() + " " + a.getAuthor().getLname())
                .assessmentPeriodId(a.getAssessmentPeriod() != null ? a.getAssessmentPeriod().getId() : null)
                .assessmentPeriodName(a.getAssessmentPeriod() != null ? a.getAssessmentPeriod().getName() : null)
                .recordedAt(a.getRecordedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
