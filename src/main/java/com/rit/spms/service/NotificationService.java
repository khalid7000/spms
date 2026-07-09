package com.rit.spms.service;

import com.rit.spms.domain.AppUser;
import com.rit.spms.domain.Notification;
import com.rit.spms.domain.enums.NotificationType;
import com.rit.spms.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** In-app notification inbox (list, unread count, mark-read) plus dispatch to any extra {@link NotificationChannel}s. */
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final List<NotificationChannel> channels;

    /**
     * REQUIRES_NEW: this is called from AFTER_COMMIT event-trigger listeners (e.g.
     * AnnualEvaluationEventTrigger, StrategyNotificationTrigger), i.e. after the triggering
     * transaction has already committed. With Open-Session-In-View keeping that request's
     * EntityManager alive for the rest of the request, a default-propagation @Transactional call
     * here silently no-ops instead of persisting -- forcing a genuinely new transaction/session is
     * what actually gets the INSERT committed.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void create(AppUser recipient, String message, NotificationType type, Long entityId) {
        notificationRepository.save(Notification.builder()
                .recipient(recipient)
                .message(message)
                .type(type)
                .entityId(entityId)
                .build());
        channels.forEach(channel -> channel.send(recipient, message, type, entityId));
    }

    @Transactional(readOnly = true)
    public List<Notification> getForUser(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    public void markRead(Long notificationId, Long currentUserId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new com.rit.spms.exception.ResourceNotFoundException("Notification", notificationId));
        if (!notification.getRecipient().getId().equals(currentUserId)) {
            throw new com.rit.spms.exception.UnauthorizedException("You can only mark your own notifications as read");
        }
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }
}
