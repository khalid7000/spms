package com.rit.spms.repository;

import com.rit.spms.domain.RepositoryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositoryRecordRepository extends JpaRepository<RepositoryRecord, Long> {

    List<RepositoryRecord> findBySourceTypeAndEmployeeEmailAndSecondaryKeyIn(
            String sourceType, String employeeEmail, List<String> secondaryKeys);

    @Query("SELECT DISTINCT r.secondaryKey, r.secondaryKeyLabel FROM RepositoryRecord r " +
           "WHERE r.sourceType = :sourceType AND r.employeeEmail = :employeeEmail")
    List<Object[]> findDistinctSecondaryKeysForEmployee(
            @Param("sourceType") String sourceType, @Param("employeeEmail") String employeeEmail);

    void deleteBySourceTypeAndSecondaryKey(String sourceType, String secondaryKey);

    @Query("SELECT DISTINCT r.sourceType FROM RepositoryRecord r ORDER BY r.sourceType")
    List<String> findDistinctSourceTypes();

    @Query("SELECT r.sourceType, r.secondaryKey, r.secondaryKeyLabel, COUNT(r) FROM RepositoryRecord r " +
           "GROUP BY r.sourceType, r.secondaryKey, r.secondaryKeyLabel ORDER BY r.sourceType, r.secondaryKey")
    List<Object[]> summarize();
}
