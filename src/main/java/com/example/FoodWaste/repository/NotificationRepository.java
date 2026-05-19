package com.example.FoodWaste.repository;

import com.example.FoodWaste.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserId(Long userId);

    List<Notification> findByIsRead(Boolean isRead);
}
