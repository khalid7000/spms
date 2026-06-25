package com.rit.spms.dto.response;

import com.rit.spms.domain.Department;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DepartmentResponse {

    private Long id;
    private String name;
    private String code;
    private Boolean active;
    private String headTitle;
    private Long headUserId;
    private String headUserEmail;
    private String headUserName;
    private Long orgGroupId;
    private String orgGroupTitle;
    private LocalDateTime createdAt;

    public static DepartmentResponse from(Department d) {
        return DepartmentResponse.builder()
                .id(d.getId())
                .name(d.getName())
                .code(d.getCode())
                .active(d.getActive())
                .headTitle(d.getHeadTitle())
                .headUserId(d.getHead() != null ? d.getHead().getId() : null)
                .headUserEmail(d.getHead() != null ? d.getHead().getEmail() : null)
                .headUserName(d.getHead() != null
                        ? d.getHead().getFname() + " " + d.getHead().getLname() : null)
                .orgGroupId(d.getOrgGroup() != null ? d.getOrgGroup().getId() : null)
                .orgGroupTitle(d.getOrgGroup() != null ? d.getOrgGroup().getTitle() : null)
                .createdAt(d.getCreatedAt())
                .build();
    }
}
