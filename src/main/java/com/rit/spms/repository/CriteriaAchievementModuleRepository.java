package com.rit.spms.repository;

import com.rit.spms.domain.CriteriaAchievementModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CriteriaAchievementModuleRepository extends JpaRepository<CriteriaAchievementModule, Long> {

    List<CriteriaAchievementModule> findByCriteriaId(Long criteriaId);

    @Query("SELECT cam FROM CriteriaAchievementModule cam WHERE cam.moduleCode = :moduleCode " +
           "AND cam.criteria.category.title.id = :titleId")
    Optional<CriteriaAchievementModule> findByModuleCodeAndTitleId(
            @Param("moduleCode") String moduleCode, @Param("titleId") Long titleId);

    @Query("SELECT cam FROM CriteriaAchievementModule cam WHERE cam.criteria.category.title.id = :titleId")
    List<CriteriaAchievementModule> findByTitleId(@Param("titleId") Long titleId);

    List<CriteriaAchievementModule> findByCriteriaIdIn(List<Long> criteriaIds);
}
