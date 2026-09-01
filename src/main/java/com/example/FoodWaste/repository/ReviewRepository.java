package com.example.FoodWaste.repository;

import com.example.FoodWaste.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByRevieweeId(Long revieweeId, Pageable pageable);

    Page<Review> findByReviewerId(Long reviewerId, Pageable pageable);

    boolean existsByClaimIdAndReviewerId(Long claimId, Long reviewerId);
}
