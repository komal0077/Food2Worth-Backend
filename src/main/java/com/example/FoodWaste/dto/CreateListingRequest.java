package com.example.FoodWaste.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateListingRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotBlank(message = "Quantity unit is required")
    private String quantityUnit;

    private String photoUrl;

    @NotBlank(message = "Pickup address is required")
    private String address;

    private Double latitude;

    private Double longitude;

    @NotNull(message = "Pickup start time is required")
    private LocalDateTime pickupStartTime;

    @NotNull(message = "Pickup end time is required")
    private LocalDateTime pickupEndTime;

    @NotNull(message = "Expiry time is required")
    private LocalDateTime expiryTime;
}
