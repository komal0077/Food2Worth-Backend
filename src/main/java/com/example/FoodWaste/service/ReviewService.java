package com.example.FoodWaste.service;

import com.example.FoodWaste.entity.Review;
import com.example.FoodWaste.exception.ResourceNotFoundException;
import com.example.FoodWaste.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
    public Page<Review> getAllReviews(Pageable pageable) {

        return reviewRepository.findAll(pageable);
    }

    // Get Review By Id
    public Review getReviewById(Long id) {

        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review Not Found"));
    }

    // Get Reviews Received By User
    public Page<Review> getReviewsByReviewee(Long revieweeId, Pageable pageable) {

        return reviewRepository.findByRevieweeId(revieweeId, pageable);
    }

    // Get Reviews Given By User
    public Page<Review> getReviewsByReviewer(Long reviewerId, Pageable pageable) {

        return reviewRepository.findByReviewerId(reviewerId, pageable);
    }

    // Delete Review
    public void deleteReview(Long id) {

        reviewRepository.deleteById(id);
    }
}
