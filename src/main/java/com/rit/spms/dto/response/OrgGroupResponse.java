package com.rit.spms.dto.response;

import com.rit.spms.domain.OrgGroup;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OrgGroupResponse {

    private Long id;
    private String title;
    private String headTitle;
    private Long parentId;
    private String parentTitle;
    private Long headUserId;
    private String headUserEmail;
    private String headUserName;
    private LocalDateTime createdAt;

    public static OrgGroupResponse from(OrgGroup g) {
        return OrgGroupResponse.builder()
                .id(g.getId())
                .title(g.getTitle())
                .headTitle(g.getHeadTitle())
                .parentId(g.getParent() != null ? g.getParent().getId() : null)
                .parentTitle(g.getParent() != null ? g.getParent().getTitle() : null)
                .headUserId(g.getHead() != null ? g.getHead().getId() : null)
                .headUserEmail(g.getHead() != null ? g.getHead().getEmail() : null)
                .headUserName(g.getHead() != null
                        ? g.getHead().getFname() + " " + g.getHead().getLname() : null)
                .createdAt(g.getCreatedAt())
                .build();
    }
}
