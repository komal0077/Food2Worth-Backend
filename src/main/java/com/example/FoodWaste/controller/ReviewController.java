package com.example.FoodWaste.controller;

import com.example.FoodWaste.entity.Review;
import com.example.FoodWaste.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // Create Review
    @PostMapping
    public Review createReview(@RequestBody Review review) {

        return reviewService.createReview(review);
    }

    // Get All Reviews
    @GetMapping
    public List<Review> getAllReviews() {

        return reviewService.getAllReviews();
    }

    // Get Review By Id
    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable Long id) {

        return reviewService.getReviewById(id);
    }

    // Get Reviews Received By User
    @GetMapping("/reviewee/{revieweeId}")
    public List<Review> getReviewsByReviewee(
            @PathVariable Long revieweeId) {

        return reviewService.getReviewsByReviewee(revieweeId);
    }

    // Get Reviews Given By User
    @GetMapping("/reviewer/{reviewerId}")
    public List<Review> getReviewsByReviewer(
            @PathVariable Long reviewerId) {

        return reviewService.getReviewsByReviewer(reviewerId);
    }

    // Delete Review
    @DeleteMapping("/{id}")
    public String deleteReview(@PathVariable Long id) {

        reviewService.deleteReview(id);

        return "Review Deleted Successfully";
    }
}
