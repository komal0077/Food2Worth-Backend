package com.example.FoodWaste.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "food_listings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Food Details
    private String title;

    private String description;

    private String category;

    private Integer quantity;

    private String quantityUnit;

    // Food Image
    private String photoUrl;

    // Donor Information
    private Long donorId;

    private String donorName;

    private String donorPhone;

    // Pickup Location
    private String address;

    private Double latitude;

    private Double longitude;

    // Pickup Time
    private LocalDateTime pickupStartTime;

    private LocalDateTime pickupEndTime;

    // Food Expiry Time
    private LocalDateTime expiryTime;

    // ACTIVE
    // CLAIMED
    // COMPLETED
    // EXPIRED
    // CANCELLED
    private String status;

    // Audit
    private LocalDateTime createdAt;
}