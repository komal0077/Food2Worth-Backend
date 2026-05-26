package com.example.FoodWaste.service;

import com.example.FoodWaste.entity.Review;
import com.example.FoodWaste.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    // Create Review
    public Review createReview(Review review) {

        review.setCreatedAt(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    // Get All Reviews
    public List<Review> getAllReviews() {

        return reviewRepository.findAll();
    }

    // Get Review By Id
    public Review getReviewById(Long id) {

        return reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review Not Found"));
    }

    // Get Reviews Received By User
    public List<Review> getReviewsByReviewee(Long revieweeId) {

        return reviewRepository.findByRevieweeId(revieweeId);
    }

    // Get Reviews Given By User
    public List<Review> getReviewsByReviewer(Long reviewerId) {

        return reviewRepository.findByReviewerId(reviewerId);
    }

    // Delete Review
    public void deleteReview(Long id) {

        reviewRepository.deleteById(id);
    }
}
