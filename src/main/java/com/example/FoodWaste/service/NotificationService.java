package com.example.FoodWaste.service;

import com.example.FoodWaste.entity.Notification;
import com.example.FoodWaste.exception.ResourceNotFoundException;
import com.example.FoodWaste.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
    public Page<Notification> getAllNotifications(Pageable pageable) {

        return notificationRepository.findAll(pageable);
    }

    // Get Notification By Id
    public Notification getNotificationById(Long id) {

        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification Not Found"));
    }

    // Get Notifications By User
    public Page<Notification> getNotificationsByUser(Long userId, Pageable pageable) {

        return notificationRepository.findByUserId(userId, pageable);
    }

    // Get Unread Notifications
    public Page<Notification> getUnreadNotifications(Pageable pageable) {

        return notificationRepository.findByIsRead(false, pageable);
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