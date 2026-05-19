package com.example.FoodWaste.repository;

import com.example.FoodWaste.entity.FoodListing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodListingRepository extends JpaRepository<FoodListing, Long> {

    List<FoodListing> findByStatus(String status);

    List<FoodListing> findByDonorId(Long donorId);
}
