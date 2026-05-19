package com.example.FoodWaste.service;

import com.example.FoodWaste.entity.Notification;
import com.example.FoodWaste.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // Create Notification
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
                .orElseThrow(() -> new RuntimeException("Notification Not Found"));
    }

    // Get Notifications By User
    public List<Notification> getNotificationsByUser(Long userId) {

        return notificationRepository.findByUserId(userId);
    }

    // Get Unread Notifications
    public List<Notification> getUnreadNotifications() {

        return notificationRepository.findByIsRead(false);
    }

    // Mark Notification As Read
    public Notification markAsRead(Long id) {

        Notification notification = getNotificationById(id);

        notification.setIsRead(true);

        return notificationRepository.save(notification);
    }

    // Delete Notification
    public void deleteNotification(Long id) {

        notificationRepository.deleteById(id);
    }
}