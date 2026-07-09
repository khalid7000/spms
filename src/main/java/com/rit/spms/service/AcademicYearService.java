package com.rit.spms.service;

import com.rit.spms.domain.*;
import com.rit.spms.domain.enums.RoleType;
import com.rit.spms.domain.enums.StrategyState;
import com.rit.spms.domain.enums.StrategyType;
import com.rit.spms.exception.BusinessRuleException;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.exception.UnauthorizedException;
import com.rit.spms.repository.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Academic year lifecycle: create (copies base initiatives/measurements per deployed strategy and
 * auto-seeds a DRAFT Annual Evaluation for every eligible active user), lock/unlock, close.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AcademicYearService {

    private final AcademicYearRepository academicYearRepository;
    private final StrategyRepository strategyRepository;
    private final InitiativeRepository initiativeRepository;
    private final MeasurementRepository measurementRepository;
    private final RoleAssignmentRepository roleAssignmentRepository;
    private final AppUserRepository appUserRepository;
    private final AnnualEvaluationService annualEvaluationService;

    public AcademicYear create(String name, LocalDate startDate, LocalDate endDate, Long universityStrategyId) {
        if (academicYearRepository.existsByName(name)) {
            throw new BusinessRuleException("Academic year '" + name + "' already exists");
        }
        Strategy universityStrategy = strategyRepository.findById(universityStrategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy", universityStrategyId));
        if (universityStrategy.getStrategyType() != StrategyType.UNIVERSITY) {
            throw new BusinessRuleException("An academic year must be associated with a university-level strategy");
        }

        AcademicYear year = academicYearRepository.save(
                AcademicYear.builder().name(name).startDate(startDate).endDate(endDate)
                        .universityStrategy(universityStrategy).build());

        // Copy base initiatives + measurements for every deployed strategy in the SAME planning
        // cycle as the selected university strategy (itself plus its department strategies for
        // that era) -- not every deployed strategy system-wide, which would pull in unrelated cycles.
        strategyRepository.findByPlanningCycleIdAndState(universityStrategy.getPlanningCycle().getId(), StrategyState.DEPLOYED)
                .forEach(s -> copyForStrategy(s, year));

        // Open a DRAFT annual evaluation for every active user whose title has portfolio
        // categories configured (skips users with no resolvable head or no configured title,
        // same gating PortfolioCategoryService already applies elsewhere)
        appUserRepository.findByActiveTrue()
                .forEach(user -> annualEvaluationService.autoCreateForNewAcademicYear(user, year));

        return year;
    }

    /**
     * A strategy deployed AFTER an academic year already exists never got a year-specific copy of
     * its initiatives/measurements at creation time (create() only copies into DEPLOYED strategies
     * that existed then) -- so any achievement recorded on its base initiatives becomes invisible
     * the instant a specific year is selected in the Strategy Detail page (the tree only ever shows
     * either the base plan OR that year's copies, never both/a union). Call this the moment a
     * strategy transitions to DEPLOYED to backfill copies for every academic year that already
     * exists in its planning cycle, closing that gap symmetrically with create()'s own loop.
     */
    public void backfillInitiativeCopiesForNewlyDeployedStrategy(Strategy strategy) {
        if (strategy.getPlanningCycle() == null) return;
        for (AcademicYear year : academicYearRepository.findByUniversityStrategy_PlanningCycle_Id(strategy.getPlanningCycle().getId())) {
            if (!initiativeRepository.existsByStrategyIdAndAcademicYearId(strategy.getId(), year.getId())) {
                copyForStrategy(strategy, year);
            }
        }
    }

    private void copyForStrategy(Strategy strategy, AcademicYear year) {
        for (Initiative base : initiativeRepository.findBaseByStrategyId(strategy.getId())) {
            Initiative copy = initiativeRepository.save(Initiative.builder()
                    .objective(base.getObjective())
                    .title(base.getTitle())
                    .description(base.getDescription())
                    .sortOrder(base.getSortOrder())
                    .createdBy(base.getCreatedBy())
                    .academicYear(year)
                    .sourceInitiative(base)
                    .build());

            for (Measurement bm : measurementRepository.findByInitiativeIdOrderBySortOrder(base.getId())) {
                measurementRepository.save(Measurement.builder()
                        .initiative(copy)
                        .description(bm.getDescription())
                        .unit(bm.getUnit())
                        .targetValue(bm.getTargetValue())
                        .actualValue(bm.getActualValue())
                        .sortOrder(bm.getSortOrder())
                        .academicYear(year)
                        .sourceMeasurement(bm)
                        .build());
            }
        }
    }

    public AcademicYear close(Long id) {
        AcademicYear year = academicYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", id));
        if (year.getClosed()) {
            throw new BusinessRuleException("Academic year is already closed");
        }
        year.setClosed(true);
        return academicYearRepository.save(year);
    }

    public AcademicYear lock(Long id, Long requestingUserId) {
        assertOwnerOrAdmin(requestingUserId);
        AcademicYear year = academicYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", id));
        year.setClosed(true);
        return academicYearRepository.save(year);
    }

    public AcademicYear unlock(Long id, Long requestingUserId) {
        assertOwnerOrAdmin(requestingUserId);
        AcademicYear year = academicYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", id));
        year.setClosed(false);
        return academicYearRepository.save(year);
    }

    private void assertOwnerOrAdmin(Long userId) {
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (!isAdmin && !roleAssignmentRepository.existsByUserIdAndRole(userId, RoleType.OWNER)) {
            throw new UnauthorizedException("Only strategy owners or admins can lock/unlock academic years");
        }
    }

    @Transactional(readOnly = true)
    public List<AcademicYear> getAll() {
        return academicYearRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public AcademicYear getById(Long id) {
        return academicYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", id));
    }
}
