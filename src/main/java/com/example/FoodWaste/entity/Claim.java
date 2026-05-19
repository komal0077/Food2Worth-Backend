package com.example.FoodWaste.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "claims")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Food Listing
    private Long listingId;

    // Volunteer
    private Long volunteerId;

    private String volunteerName;

    private String volunteerPhone;

    // NGO
    private Long ngoId;

    private String ngoName;

    // CLAIMED
    // PICKED_UP
    // DELIVERED
    // CANCELLED
    private String status;

    // Pickup Time
    private LocalDateTime pickedUpAt;

    // Delivery Time
    private LocalDateTime deliveredAt;

    // Audit
    private LocalDateTime createdAt;
}
