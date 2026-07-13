package com.example.FoodWaste.controller;

import com.example.FoodWaste.dto.CreateReviewRequest;
import com.example.FoodWaste.entity.Review;
import com.example.FoodWaste.security.AuthenticatedUser;
import com.example.FoodWaste.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // Create Review - requires auth, reviewer identity taken from the logged-in user
    @PostMapping
    public Review createReview(
            @Valid @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {

        return reviewService.createReview(request, principal);
    }

    // Get All Reviews (public)
    @GetMapping
    public List<Review> getAllReviews() {

        return reviewService.getAllReviews();
    }

    // Get Review By Id (public)
    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable Long id) {

        return reviewService.getReviewById(id);
    }

    // Get Reviews Received By User (public)
    @GetMapping("/reviewee/{revieweeId}")
    public List<Review> getReviewsByReviewee(
            @PathVariable Long revieweeId) {

        return reviewService.getReviewsByReviewee(revieweeId);
    }

    // Get Reviews Given By User (public)
    @GetMapping("/reviewer/{reviewerId}")
    public List<Review> getReviewsByReviewer(
            @PathVariable Long reviewerId) {

        return reviewService.getReviewsByReviewer(reviewerId);
    }

    // Delete Review - only the reviewer who wrote it or an admin
    @DeleteMapping("/{id}")
    public String deleteReview(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {

        reviewService.deleteReview(id, principal);

        return "Review Deleted Successfully";
    }
}
