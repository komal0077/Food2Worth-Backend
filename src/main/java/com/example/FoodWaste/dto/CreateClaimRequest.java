package com.example.FoodWaste.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateClaimRequest {

    @NotNull(message = "Listing id is required")
    private Long listingId;

    @NotBlank(message = "Volunteer name is required")
    private String volunteerName;

    @NotBlank(message = "Volunteer phone is required")
    private String volunteerPhone;
}
