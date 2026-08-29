package com.example.FoodWaste.controller;

import com.example.FoodWaste.entity.Notification;
import com.example.FoodWaste.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // Create Notification — system/admin use only; regular flows create
    // notifications server-side (see ClaimService)
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Notification createNotification(@RequestBody Notification notification) {

        return notificationService.createNotification(notification);
    }

    // Get All Notifications — admin only; users see their own via /user/{userId}
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<Notification> getAllNotifications(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        return notificationService.getAllNotifications(pageable);
    }

    // Get Notification By Id
    @GetMapping("/{id}")
    public Notification getNotificationById(@PathVariable Long id) {

        return notificationService.getNotificationById(id);
    }

    // Get Notifications By User
    @GetMapping("/user/{userId}")
    public Page<Notification> getNotificationsByUser(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        return notificationService.getNotificationsByUser(userId, pageable);
    }

    // Get Unread Notifications
    @GetMapping("/unread")
    public Page<Notification> getUnreadNotifications(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        return notificationService.getUnreadNotifications(pageable);
    }

    // Mark Notification As Read
    @PutMapping("/{id}/read")
    public Notification markAsRead(@PathVariable Long id) {

        return notificationService.markAsRead(id);
    }

    // Delete Notification — admin only
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String deleteNotification(@PathVariable Long id) {

        notificationService.deleteNotification(id);

        return "Notification Deleted Successfully";
    }
}
