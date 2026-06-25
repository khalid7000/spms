package com.rit.spms.repository;

import com.rit.spms.domain.ObjectiveMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObjectiveMappingRepository extends JpaRepository<ObjectiveMapping, Long> {
    List<ObjectiveMapping> findByDeptObjectiveId(Long deptObjectiveId);
    List<ObjectiveMapping> findByUniversityObjectiveId(Long universityObjectiveId);
    boolean existsByDeptObjectiveIdAndUniversityObjectiveId(Long deptObjectiveId, Long universityObjectiveId);
    void deleteByDeptObjectiveIdAndUniversityObjectiveId(Long deptObjectiveId, Long universityObjectiveId);
    long countByDeptObjectiveId(Long deptObjectiveId);

    @Query("SELECT om FROM ObjectiveMapping om WHERE om.deptObjective.goal.strategy.id = :strategyId")
    List<ObjectiveMapping> findByDeptStrategyId(@Param("strategyId") Long strategyId);
}
