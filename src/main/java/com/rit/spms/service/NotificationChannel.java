package com.rit.spms.service;

import com.rit.spms.domain.AppUser;
import com.rit.spms.domain.enums.NotificationType;

/**
 * An additional delivery channel for a notification, beyond the always-persisted in-app inbox row
 * (see {@link NotificationService#create}). No implementations exist yet -- everything today is
 * in-app only -- but adding email/SMS/WhatsApp later is just a new {@code @Component} implementing
 * this interface; Spring auto-collects every bean of this type, so no call site needs to change.
 */
public interface NotificationChannel {
    void send(AppUser recipient, String message, NotificationType type, Long entityId);
}
