package com.example.FoodWaste.service;

import com.example.FoodWaste.entity.FoodListing;
import com.example.FoodWaste.repository.FoodListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodListingService {

    private final FoodListingRepository foodListingRepository;

    // Create Food Listing
    public FoodListing createListing(FoodListing foodListing) {

        foodListing.setCreatedAt(LocalDateTime.now());

        foodListing.setStatus("ACTIVE");

        return foodListingRepository.save(foodListing);
    }

    // Get All Listings
    public List<FoodListing> getAllListings() {

        return foodListingRepository.findAll();
    }

    // Get Listing By Id
    public FoodListing getListingById(Long id) {

        return foodListingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Food Listing Not Found"));
    }

    // Get Listings By Status
    public List<FoodListing> getListingsByStatus(String status) {

        return foodListingRepository.findByStatus(status);
    }

    // Get Listings By Donor
    public List<FoodListing> getListingsByDonor(Long donorId) {

        return foodListingRepository.findByDonorId(donorId);
    }

    // Delete Listing
    public void deleteListing(Long id) {

        foodListingRepository.deleteById(id);
    }
}
