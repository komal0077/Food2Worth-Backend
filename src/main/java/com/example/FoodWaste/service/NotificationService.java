package com.example.FoodWaste.service;

import com.example.FoodWaste.entity.Notification;
import com.example.FoodWaste.exception.NotFoundException;
import com.example.FoodWaste.repository.NotificationRepository;
import com.example.FoodWaste.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // Create Notification - used both internally by other services and via the admin-only endpoint
    public Notification createNotification(Notification notification) {

        notification.setCreatedAt(LocalDateTime.now());

        notification.setIsRead(false);

        return notificationRepository.save(notification);
    }

    // Get All Notifications
    public List<Notification> getAllNotifications() {

        return notificationRepository.findAll();
    }

    // Get Notification By Id
    public Notification getNotificationById(Long id) {

        return notificationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Notification Not Found"));
    }

    // Get Notifications By User - caller must be that user or an admin
    public List<Notification> getNotificationsByUser(Long userId, AuthenticatedUser principal) {

        if (!principal.isSelfOrAdmin(userId)) {
            throw new AccessDeniedException("You can only view your own notifications");
        }

        return notificationRepository.findByUserId(userId);
    }

    // Get Unread Notifications for the logged-in user
    public List<Notification> getUnreadNotifications(AuthenticatedUser principal) {

        return notificationRepository.findByUserIdAndIsRead(principal.getId(), false);
    }

    // Mark Notification As Read - only the owning user or an admin
    public Notification markAsRead(Long id, AuthenticatedUser principal) {

        Notification notification = getNotificationById(id);

        if (!principal.isSelfOrAdmin(notification.getUserId())) {
            throw new AccessDeniedException("You can only update your own notifications");
        }

        notification.setIsRead(true);

        return notificationRepository.save(notification);
    }

    // Delete Notification - only the owning user or an admin
    public void deleteNotification(Long id, AuthenticatedUser principal) {

        Notification notification = getNotificationById(id);

        if (!principal.isSelfOrAdmin(notification.getUserId())) {
            throw new AccessDeniedException("You can only delete your own notifications");
        }

        notificationRepository.deleteById(id);
    }
}
