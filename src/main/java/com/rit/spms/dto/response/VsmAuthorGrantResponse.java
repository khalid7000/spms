package com.rit.spms.dto.response;

import com.rit.spms.domain.VsmAuthorGrant;
import com.rit.spms.domain.enums.VsmAuthorGrantStatus;
import com.rit.spms.domain.enums.VsmScopeType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class VsmAuthorGrantResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private Long grantedByAdminId;
    private String grantedByAdminName;
    private VsmScopeType scopeType;
    private Long departmentId;
    private String departmentName;
    private Long orgGroupId;
    private String orgGroupName;
    private Long requiredApproverId;
    private String approverTitle;
    private VsmAuthorGrantStatus status;
    private LocalDateTime decidedAt;
    private LocalDateTime createdAt;

    public static VsmAuthorGrantResponse from(VsmAuthorGrant grant) {
        return VsmAuthorGrantResponse.builder()
                .id(grant.getId())
                .employeeId(grant.getEmployee().getId())
                .employeeName(grant.getEmployee().getFname() + " " + grant.getEmployee().getLname())
                .grantedByAdminId(grant.getGrantedByAdmin().getId())
                .grantedByAdminName(grant.getGrantedByAdmin().getFname() + " " + grant.getGrantedByAdmin().getLname())
                .scopeType(grant.getScopeType())
                .departmentId(grant.getDepartment() != null ? grant.getDepartment().getId() : null)
                .departmentName(grant.getDepartment() != null ? grant.getDepartment().getName() : null)
                .orgGroupId(grant.getOrgGroup() != null ? grant.getOrgGroup().getId() : null)
                .orgGroupName(grant.getOrgGroup() != null ? grant.getOrgGroup().getTitle() : null)
                .requiredApproverId(grant.getRequiredApprover().getId())
                .approverTitle(grant.getApproverTitle())
                .status(grant.getStatus())
                .decidedAt(grant.getDecidedAt())
                .createdAt(grant.getCreatedAt())
                .build();
    }
}
