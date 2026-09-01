package com.example.FoodWaste.service;

import com.example.FoodWaste.entity.Notification;
import com.example.FoodWaste.entity.Role;
import com.example.FoodWaste.entity.User;
import com.example.FoodWaste.exception.ResourceNotFoundException;
import com.example.FoodWaste.repository.NotificationRepository;
import com.example.FoodWaste.repository.UserRepository;
import com.example.FoodWaste.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private final UserRepository userRepository;

    // Create Notification
    public Notification createNotification(Notification notification) {

        notification.setId(null);
        notification.setCreatedAt(LocalDateTime.now());

        notification.setIsRead(false);

        return notificationRepository.save(notification);
    }

    // Get All Notifications
    public Page<Notification> getAllNotifications(Pageable pageable) {

        return notificationRepository.findAll(pageable);
    }

    // Get Notification By Id — only the recipient or an admin may view it
    public Notification getNotificationById(Long id) {

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification Not Found"));

        requireOwnerOrAdmin(notification);

        return notification;
    }

    // Get Notifications By User — callers may only list their own notifications
    public Page<Notification> getNotificationsByUser(Long userId, Pageable pageable) {

        User currentUser = currentUser();

        if (!currentUser.getId().equals(userId) && currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You do not have permission to view these notifications");
        }

        return notificationRepository.findByUserId(userId, pageable);
    }

    // Get Unread Notifications — scoped to the calling user
    public Page<Notification> getUnreadNotifications(Pageable pageable) {

        return notificationRepository.findByUserIdAndIsRead(currentUser().getId(), false, pageable);
    }

    // Mark Notification As Read — only the recipient or an admin may mark it
    public Notification markAsRead(Long id) {

        Notification notification = getNotificationById(id);

        notification.setIsRead(true);

        return notificationRepository.save(notification);
    }

    // Delete Notification
    public void deleteNotification(Long id) {

        notificationRepository.deleteById(id);
    }

    private void requireOwnerOrAdmin(Notification notification) {

        User currentUser = currentUser();

        boolean isOwner = currentUser.getId().equals(notification.getUserId());

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to access this notification");
        }
    }

    private User currentUser() {

        return userRepository.findByEmail(SecurityUtils.getCurrentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
    }
}