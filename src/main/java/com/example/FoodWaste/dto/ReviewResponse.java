package com.example.FoodWaste.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {

    private Long id;

    private Long claimId;

    private Long reviewerId;

    private String reviewerName;

    private Long revieweeId;

    private String revieweeName;

    private Integer rating;

    private String comment;

    private LocalDateTime createdAt;
}
