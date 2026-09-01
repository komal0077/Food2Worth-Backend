package com.example.FoodWaste.service;

import com.example.FoodWaste.dto.CreateReviewRequest;
import com.example.FoodWaste.dto.ReviewResponse;
import com.example.FoodWaste.entity.Claim;
import com.example.FoodWaste.entity.ClaimStatus;
import com.example.FoodWaste.entity.Review;
import com.example.FoodWaste.entity.User;
import com.example.FoodWaste.exception.ResourceNotFoundException;
import com.example.FoodWaste.repository.ClaimRepository;
import com.example.FoodWaste.repository.ReviewRepository;
import com.example.FoodWaste.repository.UserRepository;
import com.example.FoodWaste.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    private final ClaimRepository claimRepository;

    private final UserRepository userRepository;

    // Create Review — the client only submits claimId + rating + comment.
    // Reviewer/reviewee identity is always derived server-side: only a real
    // participant (volunteer/ngo) of a DELIVERED claim may leave a review,
    // the reviewee is automatically the other participant, and each
    // participant may only review a given claim once.
    public ReviewResponse createReview(CreateReviewRequest request) {

        User currentUser = userRepository.findByEmail(SecurityUtils.getCurrentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));

        Claim claim = claimRepository.findById(request.getClaimId())
                .orElseThrow(() -> new ResourceNotFoundException("Claim Not Found"));

        if (claim.getStatus() != ClaimStatus.DELIVERED) {
            throw new IllegalStateException("You can only review a claim after it has been delivered");
        }

        Long revieweeId;
        String revieweeName;

        if (currentUser.getId().equals(claim.getVolunteerId())) {
            revieweeId = claim.getNgoId();
            revieweeName = claim.getNgoName();
        } else if (currentUser.getId().equals(claim.getNgoId())) {
            revieweeId = claim.getVolunteerId();
            revieweeName = claim.getVolunteerName();
        } else {
            throw new AccessDeniedException("You did not participate in this claim");
        }

        if (revieweeId == null) {
            throw new IllegalStateException("This claim has no other participant to review yet");
        }

        if (reviewRepository.existsByClaimIdAndReviewerId(claim.getId(), currentUser.getId())) {
            throw new IllegalStateException("You have already reviewed this claim");
        }

        Review review = Review.builder()
                .claimId(claim.getId())
                .reviewerId(currentUser.getId())
                .reviewerName(currentUser.getName())
                .revieweeId(revieweeId)
                .revieweeName(revieweeName)
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        return toResponse(reviewRepository.save(review));
    }

    private ReviewResponse toResponse(Review review) {

        return ReviewResponse.builder()
                .id(review.getId())
                .claimId(review.getClaimId())
                .reviewerId(review.getReviewerId())
                .reviewerName(review.getReviewerName())
                .revieweeId(review.getRevieweeId())
                .revieweeName(review.getRevieweeName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
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
