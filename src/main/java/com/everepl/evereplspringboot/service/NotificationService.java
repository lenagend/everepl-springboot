package com.everepl.evereplspringboot.service;

import com.everepl.evereplspringboot.entity.*;
import com.everepl.evereplspringboot.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Page<Notification> findAllNotificationsByUserId(Long userId, Pageable pageable) {
        return notificationRepository.findByUserId(userId, pageable);
    }

    public void createNotificationForComment(User recipient, Comment comment, String message) {
        Notification notification = new Notification();
        notification.setUserId(recipient.getId());
        notification.setType(Notification.NotificationType.NEW_REPLY);
        notification.setStatus(Notification.NotificationStatus.UNREAD);
        notification.setTarget(new Target(comment.getId(), Target.TargetType.COMMENT));
        notification.setMessage(message);
        notificationRepository.save(notification);
    }
}
