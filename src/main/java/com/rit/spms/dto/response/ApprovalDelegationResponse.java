package com.rit.spms.dto.response;

import com.rit.spms.domain.ApprovalDelegation;
import com.rit.spms.domain.enums.ApprovalDelegationStatus;
import com.rit.spms.domain.enums.DelegationScopeType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ApprovalDelegationResponse {
    private Long id;
    private Long delegatorId;
    private String delegatorName;
    private Long delegateId;
    private String delegateName;
    private DelegationScopeType scopeType;
    private Long departmentId;
    private String departmentName;
    private Long orgGroupId;
    private String orgGroupName;
    private LocalDate startDate;
    private LocalDate endDate;
    private ApprovalDelegationStatus status;
    private boolean requiresManagerApproval;
    private Long managerApproverId;
    private String managerApproverName;
    private LocalDateTime decidedAt;
    private LocalDateTime createdAt;

    public static ApprovalDelegationResponse from(ApprovalDelegation d) {
        return ApprovalDelegationResponse.builder()
                .id(d.getId())
                .delegatorId(d.getDelegator().getId())
                .delegatorName(d.getDelegator().getFname() + " " + d.getDelegator().getLname())
                .delegateId(d.getDelegate().getId())
                .delegateName(d.getDelegate().getFname() + " " + d.getDelegate().getLname())
                .scopeType(d.getScopeType())
                .departmentId(d.getDepartment() != null ? d.getDepartment().getId() : null)
                .departmentName(d.getDepartment() != null ? d.getDepartment().getName() : null)
                .orgGroupId(d.getOrgGroup() != null ? d.getOrgGroup().getId() : null)
                .orgGroupName(d.getOrgGroup() != null ? d.getOrgGroup().getTitle() : null)
                .startDate(d.getStartDate())
                .endDate(d.getEndDate())
                .status(d.getStatus())
                .requiresManagerApproval(d.isRequiresManagerApproval())
                .managerApproverId(d.getManagerApprover() != null ? d.getManagerApprover().getId() : null)
                .managerApproverName(d.getManagerApprover() != null
                        ? d.getManagerApprover().getFname() + " " + d.getManagerApprover().getLname() : null)
                .decidedAt(d.getDecidedAt())
                .createdAt(d.getCreatedAt())
                .build();
    }
}
