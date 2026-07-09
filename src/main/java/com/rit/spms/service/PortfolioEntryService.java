package com.rit.spms.service;

import com.rit.spms.domain.*;
import com.rit.spms.dto.request.CreateAchievementRequest;
import com.rit.spms.exception.BusinessRuleException;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.exception.UnauthorizedException;
import com.rit.spms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Logging an achievement here also feeds the annual evaluation portfolio in the same action --
 * {@link #logAchievementWithEvaluation} creates the real strategy {@link Achievement} (via the
 * existing {@link AchievementService}, reusing all of its validation) and this thin evaluation
 * extension row in one transaction, rather than keeping two disconnected records.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PortfolioEntryService {

    private final PortfolioEntryRepository entryRepository;
    private final AppUserRepository userRepository;
    private final PortfolioCategoryRepository categoryRepository;
    private final CategoryCriteriaRepository criteriaRepository;
    private final EmployeeGoalRepository goalRepository;
    private final EmployeeGoalCycleRepository cycleRepository;
    private final AchievementRepository achievementRepository;
    private final AchievementService achievementService;
    private final AcademicYearRepository academicYearRepository;
    private final PermissionService permissionService;
    private final AuditService auditService;
    private final AnnualEvaluationService annualEvaluationService;

    public PortfolioEntry logAchievementWithEvaluation(Long measurementId, CreateAchievementRequest achievementReq,
                                                        Long categoryId, Long criteriaId, Integer categoryRating, Long goalId,
                                                        String evidenceUrl, Long currentUserId) {
        if (categoryRating != null && (categoryRating < 1 || categoryRating > 5)) {
            throw new BusinessRuleException("Category rating must be between 1 and 5");
        }

        Achievement achievement = achievementService.recordAchievement(measurementId, achievementReq, currentUserId);

        PortfolioCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("PortfolioCategory", categoryId));
        CategoryCriteria criteria = resolveCriteria(criteriaId, category);

        EmployeeGoal goal = null;
        if (goalId != null) {
            goal = goalRepository.findById(goalId)
                    .orElseThrow(() -> new ResourceNotFoundException("EmployeeGoal", goalId));
            if (!goal.getCycle().getEmployee().getId().equals(currentUserId)) {
                throw new UnauthorizedException("Goal does not belong to current user");
            }
        }

        PortfolioEntry entry = PortfolioEntry.builder()
                .achievement(achievement)
                .employee(achievement.getAuthor())
                .category(category)
                .criteria(criteria)
                .categoryRating(categoryRating)
                .goal(goal)
                .evidenceUrl(evidenceUrl)
                .build();

        PortfolioEntry saved = entryRepository.save(entry);
        auditService.log(achievement.getAuthor(), "LOG_PORTFOLIO_ENTRY", "PortfolioEntry", saved.getId(),
                null, "Logged evaluation entry for achievement: " + achievement.getTitle());
        annualEvaluationService.clearNothingToReportContradiction(
                achievement.getAuthor().getId(), achievement.getAssessmentPeriod(),
                criteria != null ? criteria.getId() : null, goal != null ? goal.getId() : null);
        return saved;
    }

    public PortfolioEntry updateEntry(Long entryId, Long criteriaId, Integer categoryRating, Long goalId, String evidenceUrl, Long currentUserId) {
        PortfolioEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("PortfolioEntry", entryId));

        if (!entry.getEmployee().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You can only edit your own entries");
        }
        if (categoryRating != null && (categoryRating < 1 || categoryRating > 5)) {
            throw new BusinessRuleException("Category rating must be between 1 and 5");
        }

        entry.setCriteria(resolveCriteria(criteriaId, entry.getCategory()));
        entry.setCategoryRating(categoryRating);
        entry.setEvidenceUrl(evidenceUrl);
        if (goalId != null) {
            EmployeeGoal goal = goalRepository.findById(goalId)
                    .orElseThrow(() -> new ResourceNotFoundException("EmployeeGoal", goalId));
            entry.setGoal(goal);
        } else {
            entry.setGoal(null);
        }

        PortfolioEntry saved = entryRepository.save(entry);
        annualEvaluationService.clearNothingToReportContradiction(
                entry.getEmployee().getId(), entry.getAchievement().getAssessmentPeriod(),
                entry.getCriteria() != null ? entry.getCriteria().getId() : null,
                entry.getGoal() != null ? entry.getGoal().getId() : null);
        return saved;
    }

    public void deleteEntry(Long entryId, Long currentUserId) {
        PortfolioEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("PortfolioEntry", entryId));

        if (!entry.getEmployee().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You can only delete your own entries");
        }

        auditService.log(entry.getEmployee(), "DELETE_PORTFOLIO_ENTRY", "PortfolioEntry", entryId,
                null, "Deleted evaluation entry (achievement record is unaffected)");
        entryRepository.delete(entry);
    }

    public PortfolioEntry getEntryById(Long entryId) {
        return entryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("PortfolioEntry", entryId));
    }

    /** academicYearId is optional -- null returns the full all-time portfolio. */
    public List<PortfolioEntry> getEmployeePortfolio(Long employeeId, Long academicYearId, Long currentUserId) {
        AppUser employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", employeeId));
        permissionService.assertCanViewPortfolioOf(currentUserId, employee);
        List<PortfolioEntry> entries = entryRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
        if (academicYearId == null) {
            return entries;
        }
        AcademicYear year = academicYearRepository.findById(academicYearId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", academicYearId));
        return entries.stream().filter(e -> e.belongsToAcademicYear(year)).toList();
    }

    public List<PortfolioEntry> getEmployeePortfolioByCategory(Long employeeId, Long categoryId, Long currentUserId) {
        AppUser employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", employeeId));
        permissionService.assertCanViewPortfolioOf(currentUserId, employee);
        return entryRepository.findByEmployeeIdAndCategoryIdOrderByCreatedAtDesc(employeeId, categoryId);
    }

    public List<PortfolioEntry> getEmployeePortfolioByGoal(Long employeeId, Long goalId, Long currentUserId) {
        AppUser employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", employeeId));
        permissionService.assertCanViewPortfolioOf(currentUserId, employee);
        return entryRepository.findByEmployeeIdAndGoalIdOrderByCreatedAtDesc(employeeId, goalId);
    }

    public PortfolioSummary getEmployeePortfolioSummary(Long employeeId, Long academicYearId, Long currentUserId) {
        AppUser employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", employeeId));
        permissionService.assertCanViewPortfolioOf(currentUserId, employee);

        AcademicYear year = academicYearRepository.findById(academicYearId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", academicYearId));
        List<PortfolioEntry> entries = entryRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId).stream()
                .filter(e -> e.belongsToAcademicYear(year))
                .toList();
        int deployedGoals = cycleRepository.findByEmployeeIdAndAcademicYearId(employeeId, academicYearId)
                .filter(c -> c.getState() == EmployeeGoalCycle.CycleState.DEPLOYED)
                .map(c -> goalRepository.findByCycleIdOrderBySortOrder(c.getId()).size())
                .orElse(0);

        return PortfolioSummary.builder()
                .employeeId(employeeId)
                .employeeName(employee.getFname() + " " + employee.getLname())
                .totalEntries(entries.size())
                .deployedGoals(deployedGoals)
                .averageRating(entries.stream()
                        .filter(e -> e.getCategoryRating() != null)
                        .mapToInt(PortfolioEntry::getCategoryRating)
                        .average()
                        .orElse(0.0))
                .build();
    }

    // ─── Achievement-linked entries (Strategy Tree's achievement-recording modal) ──────────

    /** Returns the evaluation entry for an achievement, or null if none has been attached yet. */
    public PortfolioEntry getEntryByAchievementId(Long achievementId, Long currentUserId) {
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement", achievementId));
        permissionService.assertCanRead(currentUserId,
                achievement.getMeasurement().getInitiative().getObjective().getGoal().getStrategy().getId());
        return entryRepository.findByAchievementId(achievementId).orElse(null);
    }

    public List<PortfolioEntry> getEntriesByMeasurementId(Long measurementId, Long currentUserId) {
        List<Achievement> achievements = achievementRepository.findByMeasurementIdOrderByRecordedAtDesc(measurementId);
        if (achievements.isEmpty()) return List.of();
        permissionService.assertCanRead(currentUserId,
                achievements.get(0).getMeasurement().getInitiative().getObjective().getGoal().getStrategy().getId());
        return achievements.stream()
                .map(a -> entryRepository.findByAchievementId(a.getId()).orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    /**
     * Creates or updates the evaluation entry for an achievement that already exists (used when
     * editing an achievement from the Strategy Tree, where a portfolio entry may or may not have
     * been attached yet -- e.g. achievements recorded before this feature existed).
     */
    public PortfolioEntry upsertEvaluationForAchievement(Long achievementId, Long categoryId, Long criteriaId, Integer categoryRating,
                                                          Long goalId, String evidenceUrl, Long currentUserId) {
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement", achievementId));

        boolean isAuthor = achievement.getAuthor().getId().equals(currentUserId);
        boolean isOwner = permissionService.isOwner(currentUserId,
                achievement.getMeasurement().getInitiative().getObjective().getGoal().getStrategy().getId());
        if (!isAuthor && !isOwner) {
            throw new UnauthorizedException("Only the achievement author or strategy Owner can set its evaluation category");
        }
        if (categoryRating != null && (categoryRating < 1 || categoryRating > 5)) {
            throw new BusinessRuleException("Category rating must be between 1 and 5");
        }

        PortfolioCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("PortfolioCategory", categoryId));
        CategoryCriteria criteria = resolveCriteria(criteriaId, category);

        EmployeeGoal goal = null;
        if (goalId != null) {
            goal = goalRepository.findById(goalId)
                    .orElseThrow(() -> new ResourceNotFoundException("EmployeeGoal", goalId));
            if (!goal.getCycle().getEmployee().getId().equals(achievement.getAuthor().getId())) {
                throw new BusinessRuleException("Goal does not belong to the achievement's author");
            }
        }

        PortfolioEntry entry = entryRepository.findByAchievementId(achievementId)
                .orElseGet(() -> PortfolioEntry.builder()
                        .achievement(achievement)
                        .employee(achievement.getAuthor())
                        .build());
        entry.setCategory(category);
        entry.setCriteria(criteria);
        entry.setCategoryRating(categoryRating);
        entry.setGoal(goal);
        entry.setEvidenceUrl(evidenceUrl);

        PortfolioEntry saved = entryRepository.save(entry);
        auditService.log(achievement.getAuthor(), "UPDATE_PORTFOLIO_ENTRY", "PortfolioEntry", saved.getId(),
                null, "Updated evaluation entry for achievement: " + achievement.getTitle());
        annualEvaluationService.clearNothingToReportContradiction(
                achievement.getAuthor().getId(), achievement.getAssessmentPeriod(),
                criteria != null ? criteria.getId() : null, goal != null ? goal.getId() : null);
        return saved;
    }

    public void linkEntryToGoal(Long entryId, Long goalId, Long currentUserId) {
        PortfolioEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("PortfolioEntry", entryId));
        if (!entry.getEmployee().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You can only link your own entries");
        }
        EmployeeGoal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeGoal", goalId));
        if (!goal.getCycle().getEmployee().getId().equals(entry.getEmployee().getId())) {
            throw new BusinessRuleException("Entry and goal must belong to same employee");
        }
        entry.setGoal(goal);
        entryRepository.save(entry);
        annualEvaluationService.clearNothingToReportContradiction(
                entry.getEmployee().getId(), entry.getAchievement().getAssessmentPeriod(), null, goal.getId());
    }

    /** Null-safe; validates the criteria (if given) actually belongs to the entry's category. */
    private CategoryCriteria resolveCriteria(Long criteriaId, PortfolioCategory category) {
        if (criteriaId == null) {
            return null;
        }
        CategoryCriteria criteria = criteriaRepository.findById(criteriaId)
                .orElseThrow(() -> new ResourceNotFoundException("CategoryCriteria", criteriaId));
        if (!criteria.getCategory().getId().equals(category.getId())) {
            throw new BusinessRuleException("Criteria '" + criteria.getCriteriaName() + "' does not belong to category '" + category.getCategoryName() + "'");
        }
        return criteria;
    }

    @lombok.Data
    @lombok.Builder
    public static class PortfolioSummary {
        private Long employeeId;
        private String employeeName;
        private int totalEntries;
        private int deployedGoals;
        private double averageRating;
    }
}
