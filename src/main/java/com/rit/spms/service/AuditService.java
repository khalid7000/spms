package com.rit.spms.service;

import com.rit.spms.domain.AppUser;
import com.rit.spms.domain.AuditLog;
import com.rit.spms.domain.Strategy;
import com.rit.spms.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AppUser user, String action, String entityType, Long entityId,
                    Strategy strategy, String oldValue, String newValue, String details) {
        AuditLog log = AuditLog.builder()
                .user(user)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .strategy(strategy)
                .oldValue(oldValue)
                .newValue(newValue)
                .details(details)
                .build();
        auditLogRepository.save(log);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AppUser user, String action, String entityType, Long entityId, Strategy strategy, String details) {
        log(user, action, entityType, entityId, strategy, null, null, details);
    }
}
