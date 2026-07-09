package com.rit.spms.repository;

import com.rit.spms.domain.PortfolioCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioCategoryRepository extends JpaRepository<PortfolioCategory, Long> {
    List<PortfolioCategory> findByTitleIdOrderBySortOrder(Long titleId);
    List<PortfolioCategory> findByTitleIdAndIsSystemDefaultTrue(Long titleId);
    Optional<PortfolioCategory> findByTitleIdAndCategoryName(Long titleId, String categoryName);
    List<PortfolioCategory> findByIsSystemDefaultTrue();
    boolean existsByTitleId(Long titleId);
}
