package com.example.FoodWaste.controller;

import com.example.FoodWaste.entity.Notification;
import com.example.FoodWaste.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // Create Notification
    @PostMapping
    public Notification createNotification(@RequestBody Notification notification) {

        return notificationService.createNotification(notification);
    }

    // Get All Notifications
    @GetMapping
    public List<Notification> getAllNotifications() {

        return notificationService.getAllNotifications();
    }

    // Get Notification By Id
    @GetMapping("/{id}")
    public Notification getNotificationById(@PathVariable Long id) {

        return notificationService.getNotificationById(id);
    }

    // Get Notifications By User
    @GetMapping("/user/{userId}")
    public List<Notification> getNotificationsByUser(@PathVariable Long userId) {

        return notificationService.getNotificationsByUser(userId);
    }

    // Get Unread Notifications
    @GetMapping("/unread")
    public List<Notification> getUnreadNotifications() {

        return notificationService.getUnreadNotifications();
    }

    // Mark Notification As Read
    @PutMapping("/{id}/read")
    public Notification markAsRead(@PathVariable Long id) {

        return notificationService.markAsRead(id);
    }

    // Delete Notification
    @DeleteMapping("/{id}")
    public String deleteNotification(@PathVariable Long id) {

        notificationService.deleteNotification(id);

        return "Notification Deleted Successfully";
    }
}
