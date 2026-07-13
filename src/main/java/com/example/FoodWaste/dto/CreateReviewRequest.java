package com.example.FoodWaste.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateReviewRequest {

    @NotNull(message = "Claim id is required")
    private Long claimId;

    @NotNull(message = "Reviewee id is required")
    private Long revieweeId;

    @NotBlank(message = "Reviewee name is required")
    private String revieweeName;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    @NotBlank(message = "Comment is required")
    @Size(max = 1000, message = "Comment must be at most 1000 characters")
    private String comment;
}
