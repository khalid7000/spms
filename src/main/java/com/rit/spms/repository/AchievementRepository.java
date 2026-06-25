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

    @Query("SELECT a FROM Achievement a WHERE a.measurement.initiative.id IN " +
           "(SELECT im.deptInitiative.id FROM InitiativeMapping im WHERE im.universityInitiative.id = :universityInitiativeId) " +
           "ORDER BY a.recordedAt DESC")
    List<Achievement> findAggregatedByUniversityInitiativeId(@Param("universityInitiativeId") Long universityInitiativeId);
}
