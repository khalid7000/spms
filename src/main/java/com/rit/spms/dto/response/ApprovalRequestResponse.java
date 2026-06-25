package com.rit.spms.dto.response;

import com.rit.spms.domain.StrategyApproval;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApprovalRequestResponse {

    private Long id;
    private Long strategyId;
    private String strategyTitle;
    private String strategyState;
    private String strategyDepartment;
    private String ownerEmail;
    private String ownerName;
    private String approverTitle;
    private Integer approvalOrder;
    private Boolean approved;
    private LocalDateTime approvedAt;
    private LocalDateTime requestedAt;

    public static ApprovalRequestResponse from(StrategyApproval a, String ownerEmail, String ownerName) {
        return ApprovalRequestResponse.builder()
                .id(a.getId())
                .strategyId(a.getStrategy().getId())
                .strategyTitle(a.getStrategy().getTitle())
                .strategyState(a.getStrategy().getState().name())
                .strategyDepartment(a.getStrategy().getDepartment() != null
                        ? a.getStrategy().getDepartment().getName() : null)
                .ownerEmail(ownerEmail)
                .ownerName(ownerName)
                .approverTitle(a.getApproverTitle())
                .approvalOrder(a.getApprovalOrder())
                .approved(a.getApproved())
                .approvedAt(a.getApprovedAt())
                .requestedAt(a.getCreatedAt())
                .build();
    }
}
