package com.everepl.evereplspringboot.service;

import com.everepl.evereplspringboot.dto.CommentResponse;
import com.everepl.evereplspringboot.dto.NotificationRequest;
import com.everepl.evereplspringboot.dto.NotificationResponse;
import com.everepl.evereplspringboot.entity.*;
import com.everepl.evereplspringboot.repository.NotificationRepository;
import com.everepl.evereplspringboot.utils.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Page<NotificationResponse> findAllNotificationsByUserId(Long userId, Pageable pageable) {
        return notificationRepository.findByUserId(userId, pageable).map(this::toDto);
    }

    public NotificationResponse createNotificationForComment(CommentResponse commentResponse, String title) {
        Notification notification = new Notification();
        notification.setUserId(commentResponse.getUser().id());
        notification.setTitle(title);

        String message = "";
        message = StringUtils.truncateText(commentResponse.getText(), 30) + "...";

        notification.setMessage(message);
        notification.setLink(commentResponse.getLink());
        notification.setNotificationType(Notification.NotificationType.NEW_REPLY);
        notification.setNotificationStatus(Notification.NotificationStatus.UNREAD);
        return toDto(notificationRepository.save(notification));
    }

    public NotificationResponse updateNotificationStatus(NotificationRequest notificationRequest) {
        Optional<Notification> notificationOptional = notificationRepository.findById(notificationRequest.notificationId());
        if (notificationOptional.isPresent()) {
            Notification notification = notificationOptional.get();
            notification.setNotificationStatus(notificationRequest.status());
            notification.setUpdatedAt(LocalDateTime.now()); // Update the time of modification
            notificationRepository.save(notification);
            return toDto(notification);
        } else {
            throw new NoSuchElementException("Notification not found");
        }
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
