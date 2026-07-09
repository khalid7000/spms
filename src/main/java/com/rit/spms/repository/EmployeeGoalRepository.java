package com.rit.spms.repository;

import com.rit.spms.domain.EmployeeGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeGoalRepository extends JpaRepository<EmployeeGoal, Long> {
    List<EmployeeGoal> findByCycleIdOrderBySortOrder(Long cycleId);
    long countByCycleIdAndEmployeeActionTypeIsNull(Long cycleId);
    void deleteByCycleId(Long cycleId);
}
