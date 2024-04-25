package com.everepl.evereplspringboot.service;

import com.everepl.evereplspringboot.dto.CommentResponse;
import com.everepl.evereplspringboot.dto.NotificationResponse;
import com.everepl.evereplspringboot.entity.*;
import com.everepl.evereplspringboot.repository.NotificationRepository;
import com.everepl.evereplspringboot.utils.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Page<NotificationResponse> findAllNotificationsByUserId(Long userId, Pageable pageable) {
        return notificationRepository.findByUserId(userId, pageable).map(this::toDto);
    }

    public void createNotificationForComment(CommentResponse commentResponse, String title) {
        Notification notification = new Notification();
        notification.setUserId(commentResponse.user().id());
        notification.setTitle(title);

        String message = "";
        message = StringUtils.truncateText(commentResponse.text(), 30) + "...";

        notification.setMessage(message);
        notification.setLink(commentResponse.rootUrl());
        notification.setNotificationType(Notification.NotificationType.NEW_REPLY);
        notification.setNotificationStatus(Notification.NotificationStatus.UNREAD);
        notificationRepository.save(notification);
    }

    public NotificationResponse toDto(Notification notification){
        return new NotificationResponse(
          notification.getId(),
          notification.getTitle(),
          notification.getMessage(),
          notification.getLink(),
          notification.getNotificationType(),
          notification.getNotificationStatus(),
          notification.getCreatedAt(),
          notification.getUpdatedAt()
        );
    }
}
