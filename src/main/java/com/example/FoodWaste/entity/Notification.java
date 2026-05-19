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

    // Types:
    // FOOD_CLAIMED
    // FOOD_DELIVERED
    // NEW_LISTING
    // FOOD_EXPIRED
    private String type;

    // Read Status
    private Boolean isRead;

    // Audit
    private LocalDateTime createdAt;
}