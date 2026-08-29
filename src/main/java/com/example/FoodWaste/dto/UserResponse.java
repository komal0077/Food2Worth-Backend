package com.example.FoodWaste.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Safe, outward-facing view of a User — never exposes the password
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;

    private String name;

    private String email;

    private String phone;

    private String role;

    private String address;

    private Double latitude;

    private Double longitude;

    private String profilePhoto;

    private Boolean isVerified;

    private Boolean isApproved;

    private LocalDateTime createdAt;
}
