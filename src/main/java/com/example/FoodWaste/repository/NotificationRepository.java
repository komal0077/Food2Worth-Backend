package com.example.FoodWaste.repository;

import com.example.FoodWaste.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserId(Long userId, Pageable pageable);

    Page<Notification> findByIsRead(Boolean isRead, Pageable pageable);

    Page<Notification> findByUserIdAndIsRead(Long userId, Boolean isRead, Pageable pageable);
}
