package com.example.FoodWaste.controller;

import com.example.FoodWaste.entity.Review;
import com.example.FoodWaste.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // Create Review — any authenticated user
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Review createReview(@Valid @RequestBody Review review) {

        return reviewService.createReview(review);
    }

    // Get All Reviews
    @GetMapping
    public Page<Review> getAllReviews(@PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        return reviewService.getAllReviews(pageable);
    }

    // Get Review By Id
    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable Long id) {

        return reviewService.getReviewById(id);
    }

    // Get Reviews Received By User
    @GetMapping("/reviewee/{revieweeId}")
    public Page<Review> getReviewsByReviewee(
            @PathVariable Long revieweeId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        return reviewService.getReviewsByReviewee(revieweeId, pageable);
    }

    // Get Reviews Given By User
    @GetMapping("/reviewer/{reviewerId}")
    public Page<Review> getReviewsByReviewer(
            @PathVariable Long reviewerId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        return reviewService.getReviewsByReviewer(reviewerId, pageable);
    }

    // Delete Review — admin only
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String deleteReview(@PathVariable Long id) {

        reviewService.deleteReview(id);

        return "Review Deleted Successfully";
    }
}
