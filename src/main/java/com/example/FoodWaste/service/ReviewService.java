package com.example.FoodWaste.service;

import com.example.FoodWaste.dto.CreateReviewRequest;
import com.example.FoodWaste.entity.Review;
import com.example.FoodWaste.exception.NotFoundException;
import com.example.FoodWaste.repository.ClaimRepository;
import com.example.FoodWaste.repository.ReviewRepository;
import com.example.FoodWaste.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    private final ClaimRepository claimRepository;

    // Create Review - reviewer identity always comes from the authenticated user, never the request body
    public Review createReview(CreateReviewRequest request, AuthenticatedUser principal) {

        if (!claimRepository.existsById(request.getClaimId())) {
            throw new NotFoundException("Claim Not Found");
        }

        Review review = Review.builder()
                .claimId(request.getClaimId())
                .reviewerId(principal.getId())
                .reviewerName(principal.getFullName())
                .revieweeId(request.getRevieweeId())
                .revieweeName(request.getRevieweeName())
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        return reviewRepository.save(review);
    }

    // Get All Reviews
    public List<Review> getAllReviews() {

        return reviewRepository.findAll();
    }

    // Get Review By Id
    public Review getReviewById(Long id) {

        return reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Review Not Found"));
    }

    // Get Reviews Received By User
    public List<Review> getReviewsByReviewee(Long revieweeId) {

        return reviewRepository.findByRevieweeId(revieweeId);
    }

    // Get Reviews Given By User
    public List<Review> getReviewsByReviewer(Long reviewerId) {

        return reviewRepository.findByReviewerId(reviewerId);
    }

    // Delete Review - only the reviewer who wrote it or an admin
    public void deleteReview(Long id, AuthenticatedUser principal) {

        Review review = getReviewById(id);

        if (!principal.isSelfOrAdmin(review.getReviewerId())) {
            throw new AccessDeniedException("You can only delete your own reviews");
        }

        reviewRepository.deleteById(id);
    }
}
