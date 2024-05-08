package com.everepl.evereplspringboot.controller;

import com.everepl.evereplspringboot.dto.LikeRequest;
import com.everepl.evereplspringboot.dto.NotificationRequest;
import com.everepl.evereplspringboot.dto.NotificationResponse;
import com.everepl.evereplspringboot.dto.validation.UpdateGroup;
import com.everepl.evereplspringboot.entity.Notification;
import com.everepl.evereplspringboot.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getNotificationsByUserId(
            @PathVariable Long userId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<NotificationResponse> notifications = notificationService.findAllNotificationsByUserId(userId, pageable);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/status")
    public ResponseEntity<NotificationResponse> updateNotificationStatus(
            @Validated(UpdateGroup.class) @RequestBody NotificationRequest notificationRequest) {
            NotificationResponse updatedNotification = notificationService.updateNotificationStatus(notificationRequest);
            return ResponseEntity.ok(updatedNotification);

    }
}
