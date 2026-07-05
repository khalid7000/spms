package com.rit.spms.repository;

import com.rit.spms.domain.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    List<Achievement> findByMeasurementIdOrderByRecordedAtDesc(Long measurementId);

    @Query("SELECT a FROM Achievement a WHERE a.measurement.initiative.id = :initiativeId ORDER BY a.recordedAt DESC")
    List<Achievement> findByInitiativeId(@Param("initiativeId") Long initiativeId);

    boolean existsByMeasurementInitiativeId(Long initiativeId);

    long countByMeasurementInitiativeId(Long initiativeId);

    @Query("SELECT a FROM Achievement a WHERE a.measurement.initiative.id IN " +
           "(SELECT im.deptInitiative.id FROM InitiativeMapping im WHERE im.universityInitiative.id = :universityInitiativeId) " +
           "ORDER BY a.recordedAt DESC")
    List<Achievement> findAggregatedByUniversityInitiativeId(@Param("universityInitiativeId") Long universityInitiativeId);

    @Query("SELECT COUNT(a) FROM Achievement a WHERE a.measurement.initiative.id IN " +
           "(SELECT im.deptInitiative.id FROM InitiativeMapping im WHERE im.universityInitiative.id = :universityInitiativeId)")
    long countAggregatedByUniversityInitiativeId(@Param("universityInitiativeId") Long universityInitiativeId);

    @Query("SELECT COALESCE(a.assessmentPeriod.name, 'Unassigned'), " +
           "a.measurement.initiative.objective.goal.strategy.department.name, COUNT(a) " +
           "FROM Achievement a " +
           "WHERE a.measurement.initiative.id IN " +
           "(SELECT im.deptInitiative.id FROM InitiativeMapping im WHERE im.universityInitiative.id = :universityInitiativeId) " +
           "GROUP BY COALESCE(a.assessmentPeriod.name, 'Unassigned'), " +
           "a.measurement.initiative.objective.goal.strategy.department.name " +
           "HAVING COUNT(a) > 0 " +
           "ORDER BY COALESCE(a.assessmentPeriod.name, 'Unassigned'), " +
           "a.measurement.initiative.objective.goal.strategy.department.name")
    List<Object[]> countByPeriodAndDepartmentForUniversityInitiative(@Param("universityInitiativeId") Long universityInitiativeId);
}
