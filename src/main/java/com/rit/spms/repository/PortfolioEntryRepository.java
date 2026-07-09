package com.rit.spms.repository;

import com.rit.spms.domain.PortfolioEntry;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
