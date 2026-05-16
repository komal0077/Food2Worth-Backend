package com.example.FoodWaste.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Basic Details
    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    private String phone;

    // DONOR, NGO, VOLUNTEER, ADMIN
    private String role;

    // Address Details
    private String address;

    private Double latitude;

    private Double longitude;

    // Profile Details
    private String profilePhoto;

    // Verification
    private Boolean isVerified;

    private Boolean isApproved;

    // Audit
    private LocalDateTime createdAt;
}