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

    // Volunteers aren't required to hold an account, so pickup is tracked as free-text
    // contact info entered by the claiming NGO rather than a User relation.
    private Long volunteerId;

    private String volunteerName;

    private String volunteerPhone;

    // NGO - snapshot of the claiming user's identity at claim time (not a live join to
    // User), so a later name change doesn't rewrite history on past claims.
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
