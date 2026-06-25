package com.rit.spms.repository;

import com.rit.spms.domain.InitiativeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InitiativeMappingRepository extends JpaRepository<InitiativeMapping, Long> {
    Optional<InitiativeMapping> findByDeptInitiativeId(Long deptInitiativeId);
    List<InitiativeMapping> findByUniversityInitiativeId(Long universityInitiativeId);

    @Query("SELECT im FROM InitiativeMapping im WHERE im.deptInitiative.objective.goal.strategy.id = :strategyId")
    List<InitiativeMapping> findByDeptStrategyId(@Param("strategyId") Long strategyId);

    @Query("SELECT im FROM InitiativeMapping im WHERE im.universityInitiative.objective.id IN " +
           "(SELECT om.universityObjective.id FROM ObjectiveMapping om WHERE om.deptObjective.id = :deptObjectiveId)")
    List<InitiativeMapping> findAvailableForDeptObjective(@Param("deptObjectiveId") Long deptObjectiveId);
}
