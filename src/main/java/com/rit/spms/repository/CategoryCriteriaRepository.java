package com.rit.spms.repository;

import com.rit.spms.domain.CategoryCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryCriteriaRepository extends JpaRepository<CategoryCriteria, Long> {
    List<CategoryCriteria> findByCategoryIdOrderBySortOrder(Long categoryId);
}
