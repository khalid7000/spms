package com.rit.spms.repository;

import com.rit.spms.domain.PortfolioEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioEntryRepository extends JpaRepository<PortfolioEntry, Long> {
    List<PortfolioEntry> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);
    List<PortfolioEntry> findByEmployeeIdAndCategoryIdOrderByCreatedAtDesc(Long employeeId, Long categoryId);
    List<PortfolioEntry> findByEmployeeIdAndGoalIdOrderByCreatedAtDesc(Long employeeId, Long goalId);
    Optional<PortfolioEntry> findByAchievementId(Long achievementId);
    long countByEmployeeId(Long employeeId);
    long countByEmployeeIdAndCategoryId(Long employeeId, Long categoryId);

    // How many achievements this employee has already recorded through a given achievement-module
    // tool, for this criterion, within one academic year -- matched by assessment period NAME (not
    // FK id), same convention as PortfolioEntry.belongsToAcademicYear.
    @Query("SELECT COUNT(pe) FROM PortfolioEntry pe WHERE pe.employee.id = :employeeId "
            + "AND pe.criteria.id = :criteriaId AND pe.achievement.createdByModuleCode = :moduleCode "
            + "AND pe.achievement.assessmentPeriod.name = :periodName")
    long countByEmployeeIdAndCriteriaIdAndModuleCodeAndPeriodName(
            @Param("employeeId") Long employeeId, @Param("criteriaId") Long criteriaId,
            @Param("moduleCode") String moduleCode, @Param("periodName") String periodName);
}
