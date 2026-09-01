package com.example.FoodWaste.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "listingId is required")
    private Long listingId;

    // Volunteer
    private Long volunteerId;

    private String volunteerName;

    private String volunteerPhone;

    // NGO
    private Long ngoId;

    private String ngoName;

    @Enumerated(EnumType.STRING)
    private ClaimStatus status;

    // Pickup Time
    private LocalDateTime pickedUpAt;

    // Delivery Time
    private LocalDateTime deliveredAt;

    // Audit
    private LocalDateTime createdAt;

    // Optimistic locking — prevents lost updates when pickup/delivery status
    // transitions race each other
    @Version
    private Long version;
}
