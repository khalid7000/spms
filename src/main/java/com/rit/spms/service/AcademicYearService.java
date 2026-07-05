package com.rit.spms.service;

import com.rit.spms.domain.*;
import com.rit.spms.domain.enums.RoleType;
import com.rit.spms.domain.enums.StrategyState;
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

@Service
@RequiredArgsConstructor
@Transactional
public class AcademicYearService {

    private final AcademicYearRepository academicYearRepository;
    private final StrategyRepository strategyRepository;
    private final InitiativeRepository initiativeRepository;
    private final MeasurementRepository measurementRepository;
    private final RoleAssignmentRepository roleAssignmentRepository;

    public AcademicYear create(String name, LocalDate startDate, LocalDate endDate) {
        if (academicYearRepository.existsByName(name)) {
            throw new BusinessRuleException("Academic year '" + name + "' already exists");
        }
        AcademicYear year = academicYearRepository.save(
                AcademicYear.builder().name(name).startDate(startDate).endDate(endDate).build());

        // Copy base initiatives + measurements for every deployed strategy
        strategyRepository.findByState(StrategyState.DEPLOYED)
                .forEach(s -> copyForStrategy(s, year));

        return year;
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
