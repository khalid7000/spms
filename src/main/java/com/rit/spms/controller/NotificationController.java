package com.rit.spms.controller;

import com.rit.spms.domain.Notification;
import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.security.UserPrincipal;
import com.rit.spms.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/** In-app notification inbox (list, unread count, mark-read) -- no email/SMTP involved, see {@link NotificationService}. */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMy(@AuthenticationPrincipal UserPrincipal principal) {
        List<NotificationResponse> notifications = notificationService.getForUser(principal.getId())
                .stream().map(this::map).toList();
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUnreadCount(principal.getId())));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markRead(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markRead(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Notification marked read", null));
    }

    @lombok.Data
    public static class NotificationResponse {
        private Long id;
        private String message;
        private String type;
        private Long entityId;
        private Boolean isRead;
        private LocalDateTime createdAt;
    }

    private NotificationResponse map(Notification n) {
        NotificationResponse resp = new NotificationResponse();
        resp.setId(n.getId());
        resp.setMessage(n.getMessage());
        resp.setType(n.getType().name());
        resp.setEntityId(n.getEntityId());
        resp.setIsRead(n.getIsRead());
        resp.setCreatedAt(n.getCreatedAt());
        return resp;
    }
}
