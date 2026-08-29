package com.example.FoodWaste.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
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
    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String category;

    @Positive(message = "Quantity must be greater than zero")
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
    @Future(message = "Expiry time must be in the future")
    private LocalDateTime expiryTime;

    @Enumerated(EnumType.STRING)
    private ListingStatus status;

    // Audit
    private LocalDateTime createdAt;

    // Optimistic locking — prevents two concurrent claims on the same listing
    @Version
    private Long version;
}