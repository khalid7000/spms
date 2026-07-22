package com.rit.spms.repository;

import com.rit.spms.domain.VsmAuthorGrant;
import com.rit.spms.domain.enums.VsmAuthorGrantStatus;
import com.rit.spms.domain.enums.VsmScopeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VsmAuthorGrantRepository extends JpaRepository<VsmAuthorGrant, Long> {
    boolean existsByEmployeeIdAndScopeTypeAndDepartmentIdAndStatus(
            Long employeeId, VsmScopeType scopeType, Long departmentId, VsmAuthorGrantStatus status);

    boolean existsByEmployeeIdAndScopeTypeAndOrgGroupIdAndStatus(
            Long employeeId, VsmScopeType scopeType, Long orgGroupId, VsmAuthorGrantStatus status);

    List<VsmAuthorGrant> findByRequiredApproverIdAndStatus(Long approverId, VsmAuthorGrantStatus status);

    List<VsmAuthorGrant> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    List<VsmAuthorGrant> findAllByOrderByCreatedAtDesc();
}
