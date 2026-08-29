package com.example.FoodWaste.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User who receives notification
    private Long userId;

    private String userName;

    // Notification Message
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    // Read Status
    private Boolean isRead;

    // Audit
    private LocalDateTime createdAt;
}