package com.example.FoodWaste.entity;

import jakarta.persistence.*;
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
    private Long claimId;

    // Who gave review
    private Long reviewerId;

    private String reviewerName;

    // Who received review
    private Long revieweeId;

    private String revieweeName;

    // Rating 1-5
    private Integer rating;

    // Review Message
    private String comment;

    // Audit
    private LocalDateTime createdAt;
}