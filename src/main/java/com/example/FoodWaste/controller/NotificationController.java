package com.example.FoodWaste.controller;

import com.example.FoodWaste.entity.Notification;
import com.example.FoodWaste.security.AuthenticatedUser;
import com.example.FoodWaste.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // Create Notification - admin only, this is a system-generated record
    @PostMapping
    public Notification createNotification(
            @RequestBody Notification notification,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {

        requireAdmin(principal);

        return notificationService.createNotification(notification);
    }

    // Get All Notifications - admin only, exposes every user's notifications
    @GetMapping
    public List<Notification> getAllNotifications(@AuthenticationPrincipal AuthenticatedUser principal) {

        requireAdmin(principal);

        return notificationService.getAllNotifications();
    }

    // Get Notification By Id
    @GetMapping("/{id}")
    public Notification getNotificationById(@PathVariable Long id) {

        return notificationService.getNotificationById(id);
    }

    // Get Notifications By User - self or admin
    @GetMapping("/user/{userId}")
    public List<Notification> getNotificationsByUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {

        return notificationService.getNotificationsByUser(userId, principal);
    }

    // Get the logged-in user's own unread notifications
    @GetMapping("/unread")
    public List<Notification> getUnreadNotifications(@AuthenticationPrincipal AuthenticatedUser principal) {

        return notificationService.getUnreadNotifications(principal);
    }

    // Mark Notification As Read - only the owning user or an admin
    @PutMapping("/{id}/read")
    public Notification markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {

        return notificationService.markAsRead(id, principal);
    }

    // Delete Notification - only the owning user or an admin
    @DeleteMapping("/{id}")
    public String deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {

        notificationService.deleteNotification(id, principal);

        return "Notification Deleted Successfully";
    }

    private void requireAdmin(AuthenticatedUser principal) {
        if (!principal.isAdmin()) {
            throw new AccessDeniedException("Admin access required");
        }
    }
}
