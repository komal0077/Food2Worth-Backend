package com.example.FoodWaste.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Claim Reference
    @NotNull(message = "claimId is required")
    private Long claimId;

    // Who gave review
    @NotNull(message = "reviewerId is required")
    private Long reviewerId;

    private String reviewerName;

    // Who received review
    @NotNull(message = "revieweeId is required")
    private Long revieweeId;

    private String revieweeName;

    // Rating 1-5
    @NotNull(message = "rating is required")
    @Min(value = 1, message = "rating must be between 1 and 5")
    @Max(value = 5, message = "rating must be between 1 and 5")
    private Integer rating;

    // Review Message
    private String comment;

    // Audit
    private LocalDateTime createdAt;
}