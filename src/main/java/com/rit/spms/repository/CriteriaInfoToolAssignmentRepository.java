package com.rit.spms.repository;

import com.rit.spms.domain.CriteriaInfoToolAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CriteriaInfoToolAssignmentRepository extends JpaRepository<CriteriaInfoToolAssignment, Long> {

    List<CriteriaInfoToolAssignment> findByCriteriaId(Long criteriaId);

    @Query("SELECT a FROM CriteriaInfoToolAssignment a WHERE a.toolCode = :toolCode " +
           "AND a.repositorySourceType = :repositorySourceType AND a.criteria.category.title.id = :titleId")
    Optional<CriteriaInfoToolAssignment> findByToolCodeAndRepositorySourceTypeAndTitleId(
            @Param("toolCode") String toolCode, @Param("repositorySourceType") String repositorySourceType,
            @Param("titleId") Long titleId);

    @Query("SELECT a FROM CriteriaInfoToolAssignment a WHERE a.criteria.category.title.id = :titleId")
    List<CriteriaInfoToolAssignment> findByTitleId(@Param("titleId") Long titleId);
}
