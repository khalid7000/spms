package com.rit.spms.dto.response;

import com.rit.spms.domain.AuditLog;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private Long id;
    private LocalDateTime createdAt;
    private String action;
    private String entityType;
    private Long entityId;
    private String oldValue;
    private String newValue;
    private String details;
    private Long userId;
    private String userName;
    private Long strategyId;
    private String strategyTitle;

    public static AuditLogResponse from(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .createdAt(log.getCreatedAt())
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .details(log.getDetails())
                .userId(log.getUser() != null ? log.getUser().getId() : null)
                .userName(log.getUser() != null ? log.getUser().getFname() + " " + log.getUser().getLname() : null)
                .strategyId(log.getStrategy() != null ? log.getStrategy().getId() : null)
                .strategyTitle(log.getStrategy() != null ? log.getStrategy().getTitle() : null)
                .build();
    }
}
