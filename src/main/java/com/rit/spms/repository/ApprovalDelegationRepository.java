package com.rit.spms.repository;

import com.rit.spms.domain.ApprovalDelegation;
import com.rit.spms.domain.enums.ApprovalDelegationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ApprovalDelegationRepository extends JpaRepository<ApprovalDelegation, Long> {

    /** The seam {@code PermissionService#resolveEffectiveApprover} queries: is there an ACTIVE
     *  delegation covering this exact date for this nominal approver? */
    Optional<ApprovalDelegation> findFirstByDelegatorIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long delegatorId, ApprovalDelegationStatus status, LocalDate onOrAfter, LocalDate onOrBefore);

    List<ApprovalDelegation> findByDelegatorIdOrderByCreatedAtDesc(Long delegatorId);

    List<ApprovalDelegation> findByDelegateIdOrderByCreatedAtDesc(Long delegateId);

    List<ApprovalDelegation> findByManagerApproverIdAndStatus(Long managerApproverId, ApprovalDelegationStatus status);
}
