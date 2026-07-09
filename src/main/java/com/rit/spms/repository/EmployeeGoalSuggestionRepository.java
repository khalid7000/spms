package com.rit.spms.repository;

import com.rit.spms.domain.EmployeeGoalSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeGoalSuggestionRepository extends JpaRepository<EmployeeGoalSuggestion, Long> {
    List<EmployeeGoalSuggestion> findByCycleIdOrderBySortOrder(Long cycleId);
    void deleteByCycleId(Long cycleId);
    long countByCycleIdAndLeaderActionTypeIsNull(Long cycleId);
}
