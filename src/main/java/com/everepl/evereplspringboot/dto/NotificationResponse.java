package com.everepl.evereplspringboot.dto;

import com.everepl.evereplspringboot.entity.Notification;
import com.everepl.evereplspringboot.entity.Target;

import java.time.LocalDateTime;

public record NotificationResponse(
       Long id,
       String title,
       String message,
       String link,
       Notification.NotificationType notificationType,
       Notification.NotificationStatus notificationStatus,
       LocalDateTime createdAt,
       LocalDateTime updatedAt
) {}