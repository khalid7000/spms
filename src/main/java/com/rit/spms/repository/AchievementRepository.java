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

    long countByMeasurementInitiativeIdAndAssessmentPeriodId(Long initiativeId, Long assessmentPeriodId);

    @Query("SELECT COUNT(a) FROM Achievement a WHERE " +
           "(a.measurement.initiative.id = :baseInitiativeId OR a.measurement.initiative.sourceInitiative.id = :baseInitiativeId) " +
           "AND a.assessmentPeriod.id = :periodId")
    long countByBaseInitiativeIdAcrossYearsAndAssessmentPeriodId(@Param("baseInitiativeId") Long baseInitiativeId,
                                                                  @Param("periodId") Long periodId);

    @Query("SELECT COUNT(a) FROM Achievement a WHERE a.measurement.initiative.id IN " +
           "(SELECT im.deptInitiative.id FROM InitiativeMapping im WHERE im.universityInitiative.id = :universityInitiativeId) " +
           "AND a.assessmentPeriod.name = :periodName")
    long countByPeriodNameForUniversityInitiative(@Param("universityInitiativeId") Long universityInitiativeId,
                                                   @Param("periodName") String periodName);

    @Query("SELECT a FROM Achievement a WHERE a.measurement.initiative.id IN " +
           "(SELECT im.deptInitiative.id FROM InitiativeMapping im WHERE im.universityInitiative.id = :universityInitiativeId) " +
           "ORDER BY a.recordedAt DESC")
    List<Achievement> findAggregatedByUniversityInitiativeId(@Param("universityInitiativeId") Long universityInitiativeId);

    @Query("SELECT a FROM Achievement a WHERE a.measurement.initiative.id = :baseInitiativeId " +
           "OR a.measurement.initiative.sourceInitiative.id = :baseInitiativeId " +
           "ORDER BY a.recordedAt DESC")
    List<Achievement> findByBaseInitiativeIdAcrossYears(@Param("baseInitiativeId") Long baseInitiativeId);

    @Query("SELECT COUNT(a) FROM Achievement a WHERE a.measurement.initiative.id = :baseInitiativeId " +
           "OR a.measurement.initiative.sourceInitiative.id = :baseInitiativeId")
    long countByBaseInitiativeIdAcrossYears(@Param("baseInitiativeId") Long baseInitiativeId);

    @Query("SELECT a FROM Achievement a WHERE " +
           "(a.measurement.initiative.id = :baseInitiativeId OR a.measurement.initiative.sourceInitiative.id = :baseInitiativeId) " +
           "AND a.assessmentPeriod.name = :periodName ORDER BY a.recordedAt DESC")
    List<Achievement> findByBaseInitiativeIdAcrossYearsAndPeriodName(@Param("baseInitiativeId") Long baseInitiativeId,
                                                                      @Param("periodName") String periodName);

    @Query("SELECT COUNT(a) FROM Achievement a WHERE " +
           "(a.measurement.initiative.id = :baseInitiativeId OR a.measurement.initiative.sourceInitiative.id = :baseInitiativeId) " +
           "AND a.assessmentPeriod.name = :periodName")
    long countByBaseInitiativeIdAcrossYearsAndPeriodName(@Param("baseInitiativeId") Long baseInitiativeId,
                                                          @Param("periodName") String periodName);

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
