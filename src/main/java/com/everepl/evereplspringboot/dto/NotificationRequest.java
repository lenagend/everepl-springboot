package com.everepl.evereplspringboot.dto;

import com.everepl.evereplspringboot.dto.validation.UpdateGroup;
import com.everepl.evereplspringboot.entity.Notification;
import com.everepl.evereplspringboot.entity.Target;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;

public record NotificationRequest(
        @NotNull(groups = UpdateGroup.class)
        Long notificationId,
        @NotNull(groups = UpdateGroup.class)
        Notification.NotificationStatus status
) {}