package com.rit.spms.repository;

import com.rit.spms.domain.RoleAssignment;
import com.rit.spms.domain.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleAssignmentRepository extends JpaRepository<RoleAssignment, Long> {
    Optional<RoleAssignment> findByUserIdAndStrategyId(Long userId, Long strategyId);
    Optional<RoleAssignment> findByStrategyIdAndRole(Long strategyId, RoleType role);
    List<RoleAssignment> findByStrategyId(Long strategyId);
    List<RoleAssignment> findByStrategyIdAndRoleIn(Long strategyId, List<RoleType> roles);
    boolean existsByStrategyIdAndRole(Long strategyId, RoleType role);
    List<RoleAssignment> findByUserId(Long userId);
    boolean existsByUserIdAndRole(Long userId, RoleType role);
    void deleteByStrategyId(Long strategyId);
}
