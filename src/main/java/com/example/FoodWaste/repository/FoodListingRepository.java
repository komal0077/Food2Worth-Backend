package com.example.FoodWaste.repository;

import com.example.FoodWaste.entity.FoodListing;
import com.example.FoodWaste.entity.ListingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodListingRepository extends JpaRepository<FoodListing, Long> {

    Page<FoodListing> findByStatus(ListingStatus status, Pageable pageable);

    Page<FoodListing> findByDonorId(Long donorId, Pageable pageable);
}
