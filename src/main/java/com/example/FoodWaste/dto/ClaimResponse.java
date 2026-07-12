package com.example.FoodWaste.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimResponse {

    private Long claimId;

    private String status;

    private String ngoName;

    private String volunteerName;

    private String volunteerPhone;

    // Food Details
    private Long listingId;

    private String title;

    private String description;

    private String photoUrl;

    private String category;

    private Integer quantity;

    private String quantityUnit;

    private String address;
}