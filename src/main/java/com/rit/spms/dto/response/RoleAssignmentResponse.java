package com.rit.spms.dto.response;

import com.rit.spms.domain.enums.RoleType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoleAssignmentResponse {
    private Long id;
    private Long strategyId;
    private String strategyTitle;
    private Long userId;
    private String userEmail;
    private String userName;
    private RoleType role;
}
