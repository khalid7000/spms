package com.rit.spms.repository;

import com.rit.spms.domain.EmployeeGoalCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeGoalCycleRepository extends JpaRepository<EmployeeGoalCycle, Long> {
    Optional<EmployeeGoalCycle> findByEmployeeIdAndAcademicYearId(Long employeeId, Long academicYearId);
    List<EmployeeGoalCycle> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);
    List<EmployeeGoalCycle> findByLeaderIdAndAcademicYearIdOrderByCreatedAtDesc(Long leaderId, Long academicYearId);
    List<EmployeeGoalCycle> findByState(EmployeeGoalCycle.CycleState state);
}
